package Interface;

/**
 * Created by alhua on 18-10-17.
 */

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;

public interface IServeur extends Remote {
    int doTask(List<Map.Entry<String, Integer>> tasks) throws RemoteException;
}

