package ca.polymtl.inf8480.tp1.partie2.authserver;

import java.rmi.ConnectException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class AuthServer implements Remote {

	public static void main(String[] args) {
		AuthServer server = new AuthServer();
		server.run();
	}

	public AuthServer() {
		super();
	}

	private void run() {
		if (System.getSecurityManager() == null) {
			System.setSecurityManager(new SecurityManager());
		}

		try {
			AuthServer stub = (AuthServer) UnicastRemoteObject
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

	public boolean newUser(String user, String password) {
	    // TODO: Implement functions
	    return false;
    }

    public boolean verify(String user, String password) {
	    // TODO: Implement functions
	    return false;
    }
}
