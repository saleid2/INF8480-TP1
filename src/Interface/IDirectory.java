package Interface;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Map;
import java.util.Set;

public interface IDirectory extends Remote {
    /**
     * Register distributor in the directory server
     * @param username username of the distributor
     * @param password password of the distributor
     * @throws RemoteException
     */
    void addRepartiteur(String username, String password) throws RemoteException;

    /**
     * Register a server in the directory server
     * @param capacity capacity of the server
     * @throws RemoteException
     */
    void addServer(int capacity) throws RemoteException;

    /**
     * Unresgister a server in directory server
     * @throws RemoteException
     */
    void removeServer() throws RemoteException;

    /**
     * Verify the validity of the credential of the distributor
     * @param username
     * @param password
     * @return
     */
    boolean verifyDistributor(String username, String password) throws RemoteException;

    /**
     * Return a list of available server if the distributor has been registered
     * @param username username of the distributor
     * @param password password of the distributor
     * @return list of available server
     * @throws RemoteException
     */
    Set<Map.Entry<String, Integer>> listServers(String username, String password) throws RemoteException;
}
