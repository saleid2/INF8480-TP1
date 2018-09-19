package ca.polymtl.inf8480.tp1.partie2.fileserver;

import java.rmi.ConnectException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class FileServer implements Remote {

	public static void main(String[] args) {
		// TODO: Test that this works
		String authServerHostname = null;

		if(args.length > 0) {
			authServerHostname = args[0];
		}


		FileServer server = new FileServer(authServerHostname);
		server.run();
	}

	public FileServer(String hostname) {
		super();

		if (System.getSecurityManager() == null){
			System.setSecurityManager(new SecurityManager());
		}

		// Create AuthServerStub
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
		} catch (ConnectException e) {
			System.err
					.println("Impossible de se connecter au registre RMI. Est-ce que rmiregistry est lancé ?");
			System.err.println();
			System.err.println("Erreur: " + e.getMessage());
		} catch (Exception e) {
			System.err.println("Erreur: " + e.getMessage());
		}
	}

	private boolean authenticate(String user, String password) throws RemoteException {
		// TODO: Create reference to AuthServer
		return authServerStub.verify(user,password);
		return false;
	}
}
