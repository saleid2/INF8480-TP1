package ca.polymtl.inf8480.tp1.partie2.client;

import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.lang.Math;

import ca.polymtl.inf8480.tp1.partie2.authserver.AuthServer;
import ca.polymtl.inf8480.tp1.partie2.fileserver.FileServer;


public class Client {
	public static void main(String[] args) {
		String distantHostname = null;

		if (args.length > 0) {
			distantHostname = args[0];
		}

		Client client = new Client(distantHostname);
	}

	private AuthServer authServerStub = null;
	private FileServer fileServerStub = null;

	public Client(String distantServerHostname) {
		super();

		if (System.getSecurityManager() == null) {
			System.setSecurityManager(new SecurityManager());
		}

		fileServerStub = fileServerStub("127.0.0.1");

		if (distantServerHostname != null) {
			authServerStub = authServerStub(distantServerHostname);
		}
	}

	private AuthServer authServerStub(String hostname) {
		AuthServer stub = null;

		try {
			Registry registry = LocateRegistry.getRegistry(hostname);
			stub = (AuthServer) registry.lookup("server");
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

	private FileServer fileServerStub(String hostname) {
		FileServer stub = null;

		try {
			Registry registry = LocateRegistry.getRegistry(hostname);
			stub = (FileServer) registry.lookup("server");
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
}
