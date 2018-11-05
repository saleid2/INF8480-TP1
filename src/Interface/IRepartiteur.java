package Interface;

/**
 * Created by alhua on 18-10-17.
 */
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IRepartiteur extends Remote {
    /**
     * Ask directory server for an updated list of running servers
     */
    void updateServerList() throws RemoteException;
}

