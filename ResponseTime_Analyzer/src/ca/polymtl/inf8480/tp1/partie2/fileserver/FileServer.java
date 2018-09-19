package ca.polymtl.inf8480.tp1.partie2.fileserver;

import ca.polymtl.inf8480.tp1.partie2.iauthserver.IAuthServer;
import ca.polymtl.inf8480.tp1.partie2.ifileserver.IFileServer;

import java.io.*;
import java.rmi.AccessException;
import java.rmi.ConnectException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;

public class FileServer implements IFileServer {

	public static void main(String[] args) {
		// TODO: Test that this works
		String authServerHostname = null;

		if(args.length > 0) {
			authServerHostname = args[0];
		}


		FileServer server = new FileServer(authServerHostname);
		server.run();
	}

	private static final String FILES_ROOT = "./.file_store/";
	private static final String FILE_LOCK_PATH = "./.locks.dump";

	private HashMap<String, String> lockedFiles; // <FileName, User>

	private IAuthServer authServerStub;

	public FileServer(String hostname) {
		super();

		if (System.getSecurityManager() == null){
			System.setSecurityManager(new SecurityManager());
		}

		authServerStub = authServerStub(hostname);
	}


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

	private void run() {
		if (System.getSecurityManager() == null) {
			System.setSecurityManager(new SecurityManager());
		}

		try {
			FileServer stub = (FileServer) UnicastRemoteObject
					.exportObject(this, 0);

			Registry registry = LocateRegistry.getRegistry();
			registry.rebind("server", stub);
			System.out.println("Server ready.");

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




	private String lockFile(String filename, String user) {
		String lockOwner = lockedFiles.getOrDefault(filename, null);

		if(lockOwner == null) {
			lockedFiles.put(filename, user);
			updateLocks(lockedFiles);
			return user;
		}

		return lockOwner;
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
}
