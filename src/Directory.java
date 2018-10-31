import Interface.IDirectory;

import java.rmi.ConnectException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.RemoteServer;
import java.rmi.server.ServerNotActiveException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashSet;
import java.util.Set;

public class Directory implements IDirectory {
    private Set<String> servers;

    public static void main(String[] args) {
        Directory directoryServer = new Directory();
        directoryServer.run();
    }

    public Directory() {
        servers = new HashSet<>();
    }

    @Override
    public void addServer() throws RemoteException {
        String serverHostname = null;
        try {
            serverHostname = RemoteServer.getClientHost();
        } catch (ServerNotActiveException e) {
            // Do nothing. The server that sent the request isn't active
        }
        if (serverHostname != null) {
            servers.add(serverHostname);
        }
    }

    @Override
    public Set<String> listServers(String username, String password) throws RemoteException {
        boolean isAuthorized = authenticateUser(username, password);
        if(isAuthorized) {
            return servers;
        } else {
            throw new RemoteException("Invalid credentials");
        }
    }

    private void run(){
        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new SecurityManager());
        }

        try {
            IDirectory stub = (IDirectory) UnicastRemoteObject.exportObject(this, 0);
            Registry registry = LocateRegistry.getRegistry();
            registry.rebind("serverdirectory", stub);
            System.out.println("Directory ready.");
        } catch(ConnectException e){
            System.err.println("Could not connect to RMI registry. Is rmiregistry running?");
            System.err.println();
            System.err.println("Error: " + e.getMessage());
        } catch(Exception e ){
            System.err.println("Error: " + e.getMessage());
        }
    }

    private boolean authenticateUser(String username, String password){
        /*
         * In a real-world scenario, the user credentials would be validated against a database
         * For simplicity's sake, we will be validating against hard-coded credentials.
         * This would, and should, never be used in a real world application.
         */
        return username.equals("inf8480-as") && password.equals("password12345");
    }
}
