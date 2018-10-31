/**
 * Created by alhua on 18-10-17.
 */

import Interface.IDirectory;
import Interface.IServeur;

import java.rmi.AccessException;
import java.rmi.ConnectException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;
import java.util.Map;

public class Serveur implements IServeur {
    public static void main(String[] args) {

        if (args.length < 3) {
            System.out.println("Missing parameters");
            return;
        }

        String serverDirectoryHostname = args[0];
        int serverCapacity = Integer.parseInt(args[1]);
        int serverMaliciousRate = Integer.parseInt(args[2]);


        Serveur server = new Serveur(serverDirectoryHostname,serverCapacity, serverMaliciousRate);    //TEMPORARY DEFAULT VALUES
        server.run();
    }

    private int capacity;
    private float maliciousRate;
    private IDirectory serverDirectoryStub;

    public Serveur(String hostname, int capacity, int maliciousRate) {
        serverDirectoryStub = serverDirectoryStub(hostname);
        try {
            serverDirectoryStub.addServer();
        } catch(RemoteException e) {
            System.out.println("Erreur: " + e.getMessage());
        }

        this.capacity = capacity;
        this.maliciousRate = maliciousRate/100f;
    }

    private IDirectory serverDirectoryStub(String hostname) {
        IDirectory stub = null;

        try {
            Registry registry = LocateRegistry.getRegistry(hostname);
            stub = (IDirectory) registry.lookup("serverdirectory");
        } catch (NotBoundException e) {
            System.out.println("Erreur: Le nom '" + e.getMessage() + "' n'est pas défini dans le registre");

        } catch(AccessException e){
            System.out.println("Erreur: " + e.getMessage());
        } catch (RemoteException e){
            System.out.println("Erreur: " + e.getMessage());
        }

        return stub;
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
    private int executeOperation(String operation, int operand) throws Exception {
        int result = 0;
        switch(operation.toLowerCase()) {
            case "pell":
                result = Operations.pell(operand);
                break;
            case "prime":
                result = Operations.prime(operand);
                break;
            default:
                throw new Exception("Erreur: operation non reconnue");
        }
        return result;
    }

    @Override
    public int doTask(List<Map.Entry<String, Integer>> tasks) throws RemoteException {
        Integer result = 0;

        try {
            for (Map.Entry<String, Integer> entry : tasks) {
                result += executeOperation(entry.getKey(), entry.getValue());
                result %= 4000;
            }
        } catch(Exception e) {
            throw new RemoteException(e.getMessage());
        }

        return result;
    }

    /**
     * Evaluate if the task should be approved or rejected based on the server resource
     * @param nTask number of operation of the task received
     * @return True if approved, False if rejected
     */
    @Override
    public boolean isTaskApproved(int nTask) throws RemoteException {
        if (nTask <= capacity) {
            return true;
        } else {
            float threshold = (nTask-capacity)/(4*capacity);
            return Math.random() > threshold;
        }
    }
}
