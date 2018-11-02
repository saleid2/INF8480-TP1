package Interface;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Map;
import java.util.Set;

public interface IDirectory extends Remote {
    void addRepartiteur(String username, String password) throws RemoteException;
    void addServer(int capacity) throws RemoteException;
    void removeServer() throws RemoteException;

    /**
     * Verify the validity of the credential of the distributor
     * @param username
     * @param password
     * @return
     */
    boolean verifyDistributor(String username, String password) throws RemoteException;
    Set<Map.Entry<String, Integer>> listServers(String username, String password) throws RemoteException;
}
