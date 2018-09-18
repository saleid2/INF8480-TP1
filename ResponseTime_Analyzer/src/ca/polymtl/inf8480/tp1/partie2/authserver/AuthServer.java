package ca.polymtl.inf8480.tp1.partie2.authserver;

import java.io.*;
import java.rmi.ConnectException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;

public class AuthServer implements Remote {

	private final static String USERS_FILE_PATH = "/users.dump";

	public static void main(String[] args) {
		AuthServer server = new AuthServer();
		server.run();
	}

	public AuthServer() {
		super();
	}

	private HashMap<String, String> savedUsers;

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

			savedUsers = readUsersFromDisk();
			System.out.println("Users loaded from disk");
		} catch (ConnectException e) {
			System.err
					.println("Impossible de se connecter au registre RMI. Est-ce que rmiregistry est lancé ?");
			System.err.println();
			System.err.println("Erreur: " + e.getMessage());
		} catch (Exception e) {
			System.err.println("Erreur: " + e.getMessage());
		}
	}

	public boolean newUser(String user, String password) throws RemoteException {
	    if (savedUsers.get(user) != null){
	    	throw new RemoteException("User " + user + " already exists");
		} else {
	    	savedUsers.put(user, password);
	    	try {
				writeUsersToDisk(savedUsers);
			} catch (IOException i) {
	    		throw new RemoteException(i.getMessage());
			}
		}
	    return true;
    }

    public boolean verify(String user, String password) throws RemoteException {
	    return savedUsers.get(user).equals(password);
    }

    private void writeUsersToDisk(HashMap<String, String> users) throws IOException {
		try {
			FileOutputStream fileOut = new FileOutputStream(USERS_FILE_PATH);
			ObjectOutputStream out = new ObjectOutputStream(fileOut);
			out.writeObject(users);
			out.close();
			fileOut.close();
		} catch (IOException i){
			throw i;
		}
	}

	private HashMap<String, String> readUsersFromDisk() {
		try {
			FileInputStream fileIn = new FileInputStream(USERS_FILE_PATH);
			ObjectInputStream in = new ObjectInputStream(fileIn);
			HashMap<String, String> users = (HashMap<String, String>) in.readObject();
			in.close();
			fileIn.close();

			return users;
		} catch (ClassNotFoundException e) {
			// Should never happen
			return new HashMap<String, String>();
		}
		catch (IOException i){
			// File doesn't exist yet
			return new HashMap<String, String>();
		}
	}
}
