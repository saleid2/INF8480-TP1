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
    private int RMI_REGISTER_PORT = 5001;
    private int port = 5003;

    private int capacity;
    private float maliciousRate;
    private boolean isFree = true;
    private IDirectory serverDirectoryStub;

    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Missing parameters");
            return;
        }

        String serverDirectoryHostname = args[0];
        int serverMaliciousRate = Integer.parseInt(args[1]);
        int serverCapacity;

        if (args.length < 3) {
            serverCapacity = 4;
        } else {
            serverCapacity = Integer.parseInt(args[2]);
        }


        Serveur server = new Serveur(serverDirectoryHostname, serverCapacity, serverMaliciousRate);
        server.run();
    }

    public Serveur(String hostname, int capacity, int maliciousRate) {
        // Server crash handler, unregister its hostname from the directory server
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutdown Hook is running !");
            try {
                serverDirectoryStub.removeServer();
            } catch(RemoteException e) {
                System.out.println("Erreur: " + e.getMessage());
            }
        }));

        serverDirectoryStub = serverDirectoryStub(hostname);
        try {
            serverDirectoryStub.addServer(capacity);
        } catch(RemoteException e) {
            System.out.println("Erreur: " + e.getMessage());
        }

        this.capacity = capacity;


        // If server is launched with a malicious rate above 100, set it to 100.
        if (maliciousRate > 100) maliciousRate = 100;

        this.maliciousRate = maliciousRate/100f;
    }

    private IDirectory serverDirectoryStub(String hostname) {
        IDirectory stub = null;

        try {
            Registry registry = LocateRegistry.getRegistry(hostname, RMI_REGISTER_PORT);
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
                    .exportObject(this, port);

            Registry registry = LocateRegistry.getRegistry(RMI_REGISTER_PORT);
            registry.rebind("server", stub);
            System.out.println("Server ready.");
        } catch (ConnectException e) {
            System.err.println("Impossible de se connecter au registre RMI. Est-ce que rmiregistry est lancé ?");
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

        double p = Math.random();

        // probability of returning defected result
        if (p < maliciousRate) {
            result = (int)(Math.random() * 1000) + 1;
        }

        return result;
    }

    @Override
    public int doTask(List<Map.Entry<String, Integer>> tasks, String username, String password) throws RemoteException {
        if (!isAuthenticated(username, password)) {
            return -1;
        }

        isFree = false;
        if (!isTaskApproved(tasks.size())) {
            isFree = true;
            return -1;
        }

        int result = 0;

        try {
            for (Map.Entry<String, Integer> entry : tasks) {
                result += executeOperation(entry.getKey(), entry.getValue());
                result %= 4000;
            }
        } catch(Exception e) {
            throw new RemoteException(e.getMessage());
        } finally {
            isFree = true;
        }

        return result;
    }

    @Override
    public boolean isServerFree() throws RemoteException {
        return isFree;
    }

    /**
     * Evaluate if the task should be approved or rejected based on the server resource
     * @param nTask number of operation of the task received
     * @return True if approved, False if rejected
     */
    private boolean isTaskApproved(int nTask) {
        if (nTask <= capacity) {
            return true;
        } else {
            float threshold = (nTask-capacity)/(4*capacity);
            return Math.random() > threshold;
        }
    }

    /**
     * Call a remote function in the directory server to verify the authenticity of the distributor
     * @param username username of the distributor
     * @param password password of the distributor
     * @return True if the credential exist, otherwise False
     */
    private boolean isAuthenticated(String username, String password) {
        try {
            return serverDirectoryStub.verifyDistributor(username, password);
        } catch (RemoteException e) {
            System.out.println("Erreur: " + e.getMessage());
            return false;
        }
    }
}
