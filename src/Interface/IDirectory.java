package Interface;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Set;

public interface IDirectory extends Remote {
    void addServer(String hostname) throws RemoteException;
    Set<String> listServers(String username, String password) throws RemoteException;
}
