/**
 * Created by alhua on 18-10-17.
 */

import Interface.IServeur;

import java.rmi.ConnectException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class Serveur implements IServeur {
    public static void main(String[] args) {
        Serveur server = new Serveur(0, 0);    //TEMPORARY DEFAULT VALUES
        server.run();
    }

    private int capacity;
    private float maliciousRate;

    public Serveur(int capacity, int maliciousRate) {
        this.capacity = capacity;
        this.maliciousRate = maliciousRate/100f;
    }

    private void run() {
        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new SecurityManager());
        }

        try {
            IServeur stub = (IServeur) UnicastRemoteObject
                    .exportObject(this, 0);

            Registry registry = LocateRegistry.getRegistry();
            registry.rebind("server", stub);
            System.out.println("Server ready.");
        } catch (ConnectException e) {
            System.err
                    .println("Impossible de se connecter au registre RMI. Est-ce que rmiregistry est lancé ?");
            System.err.println();
            System.err.println("Erreur: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Erreur: " + e.getMessage());
        }
    }

    /**
     * Execute the operation and return its result
     * @param operation the operation to do (pell or prime)
     * @param operand the value passed to the operation
     * @return the result of the operation
     */
    public int executeOperation(String operation, int operand) throws RemoteException {
        int result = 0;
        switch(operation.toLowerCase()) {
            case "pell":
                result = Operations.pell(operand);
                break;
            case "prime":
                result = Operations.prime(operand);
                break;
            default:
                throw new RemoteException("Erreur: operation non reconnue");
        }
        return result;
    }

    /**
     * Evaluate if the task should be approved or rejected based on the server resource
     * @param nTask number of operation of the task received
     * @return True if approved, False if rejected
     */
    public boolean isTaskApproved(int nTask) throws RemoteException {
        if (nTask <= capacity) {
            return true;
        } else {
            float threshold = (nTask-capacity)/(4*capacity);
            return Math.random() > threshold;
        }
    }
}
