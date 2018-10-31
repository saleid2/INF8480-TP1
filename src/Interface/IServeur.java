package Interface;

/**
 * Created by alhua on 18-10-17.
 */
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IServeur extends Remote {
    int executeOperation(String operation, int operand) throws RemoteException;
    boolean isTaskApproved(int nTask) throws RemoteException;
}

