package Interface;

/**
 * Created by alhua on 18-10-17.
 */

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;

public interface IServeur extends Remote {
    /**
     * Execute task if the given credential has been registered in the directory server
     * @param tasks given task
     * @param username username of the distributor
     * @param password password of the distributor
     * @return if task accepted return result of task else return error code
     * @throws RemoteException
     */
    int doTask(List<Map.Entry<String, Integer>> tasks, String username, String password) throws RemoteException;

    /**
     * Indicate if the server is free to do task
     * @return True if server is free, False if server is occupied
     * @throws RemoteException
     */
    boolean isServerFree() throws RemoteException;
}

