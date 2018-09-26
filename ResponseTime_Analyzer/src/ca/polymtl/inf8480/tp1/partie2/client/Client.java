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
		distantHostname = args[0];		// addresse ip du serveur distant
		String command = args[1];		// commande à exécuter
		String username = args[2];		// nom d'utilisateur pour l'authentification
		String password = args[3];		// mot de passe  pour l'authentification

		if(args.length < 4) {
			System.out.print("Paramètres manquants");
			return;
		}

		String filename = null;
		if (args.length > 4) {
			filename = args[4];			// nom du fichier à traiter
		}

		Client client = new Client(distantHostname);

		switch(command) {
			case "newuser":
				client.createNewUser(username, password);
				break;
			case "create":
				client.createNewFile(username, password, filename);
				break;
			case "list":
				client.listFiles(username, password);
				break;
			case "syncLocalDirectory":
				client.syncLocalDirectory(username, password);
				break;
			case "get":
				client.getFile(username, password, filename);
				break;
			case "lock":
				client.lockFile(username, password, filename);
				break;
			case "push":
				client.pushFile(username, password, filename);
				break;
			default:

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

		// the file server is our machine on the localhost
		fileServerStub = fileServerStub("127.0.0.1");

		authServerStub = authServerStub(distantServerHostname);
	}

	private void createNewUser(String username, String password) {
		try {
			boolean success = authServerStub.newUser(username, password);

			if (success) {
				System.out.println("Nouvelle utilisateur créée");
			} else {
				System.out.println("Utilisateur déjà existante");
			}
		}
		catch (RemoteException e) {
			System.err.println("Erreur: " + e.getMessage());
		}
	}

	private void createNewFile(String username, String password, String filename) {
		try {
			boolean success = fileServerStub.create(username, password, filename);
			if (success) {
				System.out.println(filename + " ajouté.");
			} else {
				System.out.println("Fichier déjà existant");
			}
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
			for (String file: files) {
				System.out.println(file);
			}
			System.out.println(files.size() + " fichier(s)");
		}
		catch (RemoteException e) {
			System.err.println("Erreur: " + e.getMessage());
		}
	}

	private void syncLocalDirectory(String username, String password) {
		try {
			HashMap<String, byte[]> files = fileServerStub.syncLocalDirectory(username, password);

			if(files == null) throw new RemoteException("Autorisation refusée");

			for(String key : files.keySet()){
				try {
					// Create and write files received in the directory
					Files.write(Paths.get(FILES_ROOT + key), files.get(key));
				} catch(IOException e) {
					System.out.println("Erreur: " + e.getMessage());
				}
			}
			System.out.println("Synchronization terminée.");


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
				System.out.println(filename + " synchronisé.");
			} else {
				System.out.println(filename + " est déjà à jour.");
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
				System.out.println(filename + "verrouillé.");
			} else {
				System.out.println(filename + " est déjà verrouillé par " + lockHolder);
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
				System.out.println(filename + " a été envoyé au serveur.");
			} else {
				System.err.println("opération refusée : vous devez verrouiller d'abord le fichier.");
			}
		} catch (RemoteException e) {
			System.err.println("Erreur: " + e.getMessage());
		} catch (IOException e) {
			System.err.println("Erreur: " + e.getMessage());
		}
	}

	/**
	 * Connect to an authentification server and return its stub
	 * @param hostname IP address of the authentification server
	 * @return Stub object of the server
	 */
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

	/**
	 * Connect to an file server and return its stub
	 * @param hostname IP address of the file server
	 * @return Stub object of the server
	 */
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
