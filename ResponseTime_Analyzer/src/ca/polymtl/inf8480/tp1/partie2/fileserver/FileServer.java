package ca.polymtl.inf8480.tp1.partie2.fileserver;

import ca.polymtl.inf8480.tp1.partie2.iauthserver.IAuthServer;
import ca.polymtl.inf8480.tp1.partie2.ifileserver.IFileServer;

import javax.xml.bind.DatatypeConverter;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.rmi.AccessException;
import java.rmi.ConnectException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class FileServer implements IFileServer {

	public static void main(String[] args) {
		// TODO: Test that this works
		String authServerHostname = null;

		if(args.length > 0) {
			authServerHostname = args[1];
		}

		System.out.println("File server: " + args[0]);
		System.out.println("Auth server: " + args[1]);


		FileServer server = new FileServer();
		server.run(authServerHostname);
	}

	private static final String FILES_ROOT = "./.file_store/";
	private static final String FILE_LOCK_PATH = "./.locks.dump";

	private HashMap<String, String> lockedFiles; // <FileName, User>

	private IAuthServer authServerStub;

	public FileServer() {
		super();
	}


	/**
	 * Creates a file on the FileServer
	 * @param username Username
	 * @param password Password
	 * @param filename Name of the file to create.
	 * @return True if the file was created. False if the file already exists or user is unauthorised
	 * @throws RemoteException
	 */
	@Override
	public boolean create(String username, String password, String filename) throws RemoteException {
		if (!authenticate(username, password)) return false;

		try {
			File file = new File(FILES_ROOT + filename);
			return file.createNewFile();
		} catch (IOException e) {
			throw new RemoteException(e.getMessage());
		}
	}


	/**
	 * Allows the user to lock the file. If the current user owns the file, client should call get()
	 * to update their local version of the file.
	 * @param username User's username
	 * @param password User's password
	 * @param filename Name of the file to lock
	 * @param checksum File checksum to avoid downloading the current version of the file
	 * @return Username of the owner of the lock.
	 * @throws RemoteException
	 */
	@Override
	public String lock(String username, String password, String filename, String checksum) throws RemoteException {
		if (!authenticate(username, password)) return null;

		return lockFile(filename, username);
	}


	/**
	 * Fetches the most recent version of a file
	 * @param username User's username
	 * @param password User's password
	 * @param filename Name of file to fetch
	 * @param checksum File checksum to avoid downloading the current version of the file
	 * @return File if checksum is different. Returns null if user is unauthorized or file is up to date
	 * @throws RemoteException
	 */
	@Override
	public byte[] get(String username, String password, String filename, String checksum) throws RemoteException {
		if (!authenticate(username, password)) throw new RemoteException("Unauthorized");

		byte[] file = null;

		System.out.println(checksum);
		System.out.println(getFileMd5Checksum(filename));

		if (checksum == null || !checksum.equals(getFileMd5Checksum(filename))) {
			try {
				file = Files.readAllBytes(Paths.get(FILES_ROOT + filename));
			} catch (Exception e) {
				// ignore
			}
		}

		return file;
	}

	/**
	 * Upload changes to a locked file. User must be authenticated and have previously locked the file to
	 * be allowed to modify it. File is unlocked following an update.
	 * @param username User's uesrname
	 * @param password User's password
	 * @param filename Name of file to be updated
	 * @param fileContent Content of files to write to disk
	 * @return False if unauthorized. True if file was updated.
	 * @throws RemoteException
	 */
	@Override
	public boolean push(String username, String password, String filename, byte[] fileContent) throws RemoteException {
		if (!authenticate(username, password)) return false;
		if (!userLockedFile(username, filename)) return false;

		try {
			Files.write(Paths.get(FILES_ROOT + filename), fileContent);
			unlockFile(filename, username);
			return true;
		} catch (IOException e) {
			throw new RemoteException(e.getMessage());
		}
	}

	/**
	 * Outputs a list of files and the lock owner associated to each file, if exists
	 * @param username Username
	 * @param password Password
	 * @return List of files and their associated locks, if any
	 * @throws RemoteException
	 */
	@Override
	public List<String> list(String username, String password) throws RemoteException {
		File folder = new File(FILES_ROOT);
		File[] fileList = folder.listFiles();

		List<String> outFileList = new ArrayList<>();

		for(File file : fileList) {
			if(file.isFile()) {
				String fileName = file.getName();
				String lockOwner = lockedFiles.get(fileName);

				outFileList.add("* " + fileName + "\t" + (lockOwner != null ? "verrouillé par " + lockOwner : "non verrouillé"));
			}
		}

		return outFileList;
	}

	/**
	 * Fetch all files from server
	 * @param username User's username
	 * @param password User's password
	 * @return Hashmap containing <Filename, Content>
	 * @throws RemoteException
	 */
	@Override
	public HashMap<String, byte[]> syncLocalDirectory(String username, String password) throws RemoteException {
		if (!authenticate(username, password)) return null;

		File folder = new File(FILES_ROOT);
		File[] fileList = folder.listFiles();

		HashMap<String, byte[]> files = new HashMap<>();

		for (File file: fileList) {
			if (file.isFile()) {
				String filename = file.getName();
				try {
					files.put(filename, Files.readAllBytes(Paths.get(FILES_ROOT + filename)));
				} catch (Exception e) {
					// ignore
				}
			}
		}

		return files;
	}

	private void run(String hostname) {
		if (System.getSecurityManager() == null) {
			System.setSecurityManager(new SecurityManager());
		}

		try {
			IFileServer stub = (IFileServer) UnicastRemoteObject
					.exportObject(this, 0);

			Registry registry = LocateRegistry.getRegistry();
			registry.rebind("server", stub);
			System.out.println("Server ready.");

			authServerStub = authServerStub(hostname);

			lockedFiles = getFileLocks();
		} catch (ConnectException e) {
			System.err
					.println("Impossible de se connecter au registre RMI. Est-ce que rmiregistry est lancé ?");
			System.err.println();
			System.err.println("Erreur: " + e.getMessage());
		} catch (Exception e) {
			System.err.println("Erreur: " + e.getMessage());
		}
	}

	private IAuthServer authServerStub(String hostname) {
		IAuthServer stub = null;

		try {
			Registry registry = LocateRegistry.getRegistry(hostname);
			stub = (IAuthServer) registry.lookup("server");
		} catch (NotBoundException e) {
			System.out.println("Erreur: Le nom '" + e.getMessage()
					+ "' n'est pas défini dans le registre.");
		} catch (AccessException e) {
			System.out.println("Erreur: " + e.getMessage());
		} catch (RemoteException e) {
			System.out.println("Erreur: " + e.getMessage());
		}

		return stub;
	}

	private boolean authenticate(String user, String password) throws RemoteException {
		return authServerStub.verify(user,password);
	}

	/**
	 * Assigns a lock to the user passed as parameter
	 * @param filename File to lock
	 * @param user User to which the lock should be assigned
	 * @return Username of the owner of the lock
	 */
	private String lockFile(String filename, String user) {
		String lockOwner = lockedFiles.get(filename);

		if(lockOwner == null) {
			lockedFiles.put(filename, user);
			updateLocks(lockedFiles);
			return user;
		}

		return lockOwner;
	}

	/**
	 * Should only be called after a push to release the lock.
	 * @param filename File to unlock
	 * @param user User who currently owns the lock
	 */
	private void unlockFile(String filename, String user) {
		String lockOwner = lockedFiles.get(filename);

		if(lockOwner == null) return;

		lockedFiles.remove(filename);
		updateLocks(lockedFiles);
	}

	/**
	 * Persists locks to disk
	 * @param locked_files
	 * @return
	 */
	private boolean updateLocks(HashMap<String, String> locked_files) {
		try {
			FileOutputStream fileOut = new FileOutputStream(FILE_LOCK_PATH);
			ObjectOutputStream out = new ObjectOutputStream(fileOut);
			out.writeObject(locked_files);
			out.close();
			fileOut.close();
		} catch (IOException i) {
			// ignore
		}
		return false;
	}

	/**
	 * Reads locks from disk
	 * @return
	 */
	private HashMap<String, String> getFileLocks() {
		try {
			FileInputStream fileIn = new FileInputStream(FILE_LOCK_PATH);
			ObjectInputStream in = new ObjectInputStream(fileIn);
			HashMap<String, String> locks = (HashMap<String, String>) in.readObject();
			in.close();
			fileIn.close();

			return locks;
		} catch (Exception e) {
			return new HashMap<String, String>();
		}
	}

	private boolean userLockedFile(String username, String filename) {
		return username.equals(lockedFiles.get(filename));
	}

	/**
	 * Calculate MD5 checksum for a file
	 * @param filename Filename
	 * @return MD5 checksum as String
	 */
	private String getFileMd5Checksum(String filename){
		try {
			byte[] fileBytes = Files.readAllBytes(Paths.get(FILES_ROOT + filename));
			byte[] fileHash = MessageDigest.getInstance("MD5").digest(fileBytes);

			return DatatypeConverter.printHexBinary(fileHash);
		} catch (IOException e) {
			// TODO: Handle file doesn't exist
			return "";
		} catch (Exception e) {
			return "";
		}
	}
}
