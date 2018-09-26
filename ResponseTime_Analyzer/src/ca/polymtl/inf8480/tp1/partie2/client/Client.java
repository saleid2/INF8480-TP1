package ca.polymtl.inf8480.tp1.partie2.client;

import ca.polymtl.inf8480.tp1.partie2.iauthserver.IAuthServer;
import ca.polymtl.inf8480.tp1.partie2.ifileserver.IFileServer;

import javax.xml.bind.DatatypeConverter;
import java.io.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.List;


public class Client {
	public static void main(String[] args) {
		String distantHostname = null;

		if(args.length < 4) {
			System.out.print("Missing params");
			return;
		}

		distantHostname = args[0];

		Client client = new Client(distantHostname);


		String command = args[1];
		String username = args[2];
		String password = args[3];

		String file = null;
		if (args.length > 4) {
			file = args[4];
		}

		switch(command) {
			case "newuser":
				client.createNewUser(username, password);
				break;
			case "create":
				client.createNewFile(username, password, file);
				break;
			case "list":
				client.listFiles(username, password);
				break;
			case "syncLocalDirectory":
				client.syncLocalDirectory(username, password);
				break;
			case "get":
				client.getFile(username, password, file);
				break;
			case "lock":
				client.lockFile(username, password, file);
				break;
			case "push":
				client.pushFile(username, password, file);
				break;
			default:

		}
	}

	private void createNewUser(String username, String password) {
		try {
			authServerStub.newUser(username, password);
		}
		catch (RemoteException e) {
			System.err.println("Erreur: " + e.getMessage());
		}
	}

	private void createNewFile(String username, String password, String filename) {
		try {
			boolean success = fileServerStub.create(username, password, filename);
			if (success) {
				File file = new File(FILES_ROOT + filename);
				file.createNewFile();
				System.out.println("File successfully created");
			} else {
				System.out.println("File already exists");
			}
			// TODO: Get file
		}
		catch (RemoteException e) {
			System.err.println("Erreur: " + e.getMessage());
		} catch (Exception e) {
			// ignore
		}
	}

	private void listFiles(String username, String password) {
		try {
			List<String> files = fileServerStub.list(username, password);
			System.out.println("Files in directory :");
			for (String file: files) {
				System.out.println(file);
			}
		}
		catch (RemoteException e) {
			System.err.println("Erreur: " + e.getMessage());
		}
	}

	private void syncLocalDirectory(String username, String password) {
		try {
			HashMap<String, byte[]> files = fileServerStub.syncLocalDirectory(username, password);

			if(files == null) throw new RemoteException("Unauthorized");

			for(String key : files.keySet()){
				try {
					Files.write(Paths.get(FILES_ROOT + key), files.get(key));
				} catch(IOException e) {
					System.out.println("Erreur: " + e.getMessage());
				}
			}
			System.out.println("Directory sync complete");


		} catch (RemoteException e) {
			System.err.println("Erreur: " + e.getMessage());
		}
	}

	private void getFile(String username, String password, String filename) {
		try {
			String checksum = getFileMd5Checksum(filename);

			byte[] file = fileServerStub.get(username, password, filename, checksum);

			if(file != null){
				Files.write(Paths.get(FILES_ROOT + filename), file);
				System.out.println("File updated");
			} else {
				System.out.println("File is already up to date");
			}

		} catch (RemoteException e) {
			System.err.println("Erreur: " + e.getMessage());
		} catch (IOException e) {
			System.err.println("Erreur: " + e.getMessage());
		}
	}

	private void lockFile(String username, String password, String filename) {
		try {
			String checksum = getFileMd5Checksum(filename);

			String lockHolder = fileServerStub.lock(username, password, filename, checksum);

			if (lockHolder.equals(username)) {
				System.out.println("File " + filename + " successfully locked");
			} else {
				System.out.println("File " + filename + " is already locked by " + lockHolder);
			}

			getFile(username, password, filename);
		} catch (RemoteException e) {
			System.err.println("Erreur: " + e.getMessage());
		}
	}

	private void pushFile(String username, String password, String filename) {
		try {
			byte[] filecontent = Files.readAllBytes(Paths.get(FILES_ROOT + filename));
			boolean success = fileServerStub.push(username, password, filename, filecontent);
			if(success) {
				System.out.println("File pushed successfully");
			} else {
				System.err.println("File access unauthorized");
			}
		} catch (RemoteException e) {
			System.err.println("Erreur: " + e.getMessage());
		} catch (IOException e) {
			System.err.println("Erreur: " + e.getMessage());
		}
	}

	private IAuthServer authServerStub;
	private IFileServer fileServerStub;
	private static final String FILES_ROOT = "./files/";


	public Client(String distantServerHostname) {
		super();

		if (System.getSecurityManager() == null) {
			System.setSecurityManager(new SecurityManager());
		}

		fileServerStub = fileServerStub("127.0.0.1");

		authServerStub = authServerStub(distantServerHostname);
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

	private IFileServer fileServerStub(String hostname) {
		IFileServer stub = null;

		try {
			Registry registry = LocateRegistry.getRegistry(hostname);
			stub = (IFileServer) registry.lookup("server");
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
