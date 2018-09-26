package ca.polymtl.inf8480.tp1.partie2.client;

import ca.polymtl.inf8480.tp1.partie2.authserver.AuthServer;
import ca.polymtl.inf8480.tp1.partie2.fileserver.FileServer;
import ca.polymtl.inf8480.tp1.partie2.iauthserver.IAuthServer;
import ca.polymtl.inf8480.tp1.partie2.ifileserver.IFileServer;

import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.security.MessageDigest;


public class Client {
	public static void main(String[] args) {
		String distantHostname = null;

		if (args.length > 0) {
			distantHostname = args[0];
		}

		Client client = new Client(distantHostname);
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
