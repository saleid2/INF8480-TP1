import Interface.IDirectory;
import Interface.IRepartiteur;

import java.rmi.AccessException;
import java.rmi.ConnectException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.RemoteServer;
import java.rmi.server.ServerNotActiveException;
import java.rmi.server.UnicastRemoteObject;
import java.util.AbstractMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Directory implements IDirectory {
    private int RMI_REGISTER_PORT = 5001;
    private int port = 5004;

    private Set<Map.Entry<String, Integer>> servers;
    private String repartiteurHostName;
    private String repartiteurUsername;
    private String repartiteurPassword;
    private IRepartiteur repartiteur;

    public static void main(String[] args) {
        Directory directoryServer = new Directory();
        directoryServer.run();
    }

    public Directory() {
        servers = new HashSet<>();
    }

    @Override
    public void addRepartiteur(String username, String password) throws RemoteException {
        String repartiteurHostname = null;
        try {
            repartiteurUsername = username;
            repartiteurPassword = password;
            repartiteurHostname = RemoteServer.getClientHost();
        } catch (ServerNotActiveException e) {
            // Do nothing. The server that sent the request isn't active
        }
        if (repartiteurHostname != null) {
            repartiteurHostName = repartiteurHostname;
            repartiteur = repartiteurStub(repartiteurHostname);
        }
    }

    @Override
    public void addServer(int capacity) throws RemoteException {
        String serverHostname = null;
        try {
            serverHostname = RemoteServer.getClientHost();
        } catch (ServerNotActiveException e) {
            // Do nothing. The server that sent the request isn't active
        }
        if (serverHostname != null) {
            servers.add(new AbstractMap.SimpleEntry(serverHostname, capacity));
        }
    }

    @Override
    public void removeServer() throws RemoteException {
        String serverHostname = null;
        try {
            serverHostname = RemoteServer.getClientHost();
        } catch (ServerNotActiveException e) {
            // Do nothing. The server that sent the request isn't active
        }
        if (serverHostname != null) {
            servers.remove(serverHostname);
            notifyChange();
        }
    }

    @Override
    public boolean verifyDistributor(String username, String password) {
        if (username.equals(repartiteurUsername) && password.equals(repartiteurPassword)) {
            return true;
        }
        return false;
    }

    @Override
    public Set<Map.Entry<String, Integer>> listServers(String username, String password) throws RemoteException {
        boolean isAuthorized = verifyDistributor(username, password);
        if(isAuthorized) {
            return servers;
        } else {
            throw new RemoteException("Invalid credentials");
        }
    }

    /**
     * Register the directory to JAVA RMI to enable remote call
     */
    private void run(){
        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new SecurityManager());
        }

        try {
            IDirectory stub = (IDirectory) UnicastRemoteObject.exportObject(this, port);
            Registry registry = LocateRegistry.getRegistry(RMI_REGISTER_PORT);
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

    /**
     * Connect to a distributor and return its stub
     * @param hostname IP address of the distributor
     * @return Stub object of the distributor
     */
    private IRepartiteur repartiteurStub(String hostname) {
        IRepartiteur stub = null;

        try {
            Registry registry = LocateRegistry.getRegistry(hostname, RMI_REGISTER_PORT);
            stub = (IRepartiteur) registry.lookup("distributor");
        } catch (NotBoundException e) {
            System.out.println("Erreur: Le nom '" + e.getMessage() + "' n'est pas défini dans le registre.");
        } catch (AccessException e) {
            System.out.println("Erreur: " + e.getMessage());
        } catch (RemoteException e) {
            System.out.println("Erreur: " + e.getMessage());
        }

        return stub;
    }

    /**
     * Notify its distributor that an update of the server list is needed
     */
    private void notifyChange() {
        try {
            repartiteur.updateServerList();
        } catch (RemoteException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}
