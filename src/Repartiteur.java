/**
 * Created by alhua on 18-10-17.
 */

import Interface.IRepartiteur;
import Interface.IServeur;
import Interface.IDirectory;

import java.util.*;
import java.io.*;
import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.ConnectException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class Repartiteur implements IRepartiteur {
    private int RMI_REGISTER_PORT = 5001;
    private int port = 5002;

    private IDirectory directoryStub;
    private Map.Entry<IServeur, Integer>[] serverStubs;
    private String username;
    private String password;

    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Missing parameters");
            return;
        }

        String username = args[0];
        String password = args[1];
        String pathToFile = args[2];		                        // chemin au fichier de calcul
        String directoryHostname = args[3];		                    // addresse ip du serveur de service de nom
        boolean isSecuredMode;                                      // mode de securite
        float taux;                                                 // TODO test parameter only, REMOVE BEFORE REMISE

        if (args.length < 5) {
            taux = 0.1f;
            isSecuredMode = true;
        } else {
            isSecuredMode = Boolean.parseBoolean(args[4]);
            taux = Float.parseFloat(args[5]);
        }

        Repartiteur distributor = new Repartiteur(directoryHostname, username, password);
        distributor.run();

        List<Map.Entry<String, Integer>> task = distributor.readTaskFile(pathToFile);

        long startTime = System.nanoTime();
        int finalResult = distributor.distributeTask(task, taux, isSecuredMode);
        long endTime = System.nanoTime();

        long duration = (endTime - startTime);  //divide by 1000000 to get milliseconds.

        System.out.println("Cette tache donne un resultat de " + finalResult);
        System.out.println("Le temps d'execution de cette tache est de " + duration + "ms");
    }

    public Repartiteur(String directoryHostname, String username, String password) {
        this.username = username;
        this.password = password;

        // connect to directory
        directoryStub = directoryStub(directoryHostname);

        try {
            directoryStub.addRepartiteur(username, password);
        } catch (RemoteException e) {
            System.out.println("Erreur: " + e.getMessage());
        }

        // get list of server from directory and connect to all servers
        getServerList(username, password);
    }

    @Override
    public void updateServerList() {
        getServerList(username, password);
    }

    /**
     * Get the list of available servers from the directory server
     * @param username distributor username
     * @param password distributor password
     */
    private void getServerList(String username, String password) {
        try {
            Set<Map.Entry<String, Integer>> serversHostname = directoryStub.listServers(username, password);
            List<Map.Entry<IServeur, Integer>> serverList = new ArrayList<>();
            for (Map.Entry<String, Integer> entry : serversHostname) {
                IServeur server = serverStub(entry.getKey());
                serverList.add(new AbstractMap.SimpleEntry(server, entry.getValue()));
            }
            serverStubs = (Map.Entry<IServeur, Integer>[]) serverList.toArray();
        } catch (RemoteException e) {
            System.out.println("Erreur: " + e.getMessage());
        }
    }

    /**
     * Register the distributor to JAVA RMI to enable remote call
     */
    private void run() {
        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new SecurityManager());
        }

        try {
            IRepartiteur stub = (IRepartiteur) UnicastRemoteObject
                    .exportObject(this, port);

            Registry registry = LocateRegistry.getRegistry(RMI_REGISTER_PORT);
            registry.rebind("distributor", stub);
            System.out.println("Distributor ready.");
        } catch (ConnectException e) {
            System.err.println("Impossible de se connecter au registre RMI. Est-ce que rmiregistry est lancé ?");
            System.err.println();
            System.err.println("Erreur: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Erreur: " + e.getMessage());
        }
    }

    /**
     * Connect to a directory and return its stub
     * @param hostname IP address of the directory
     * @return Stub object of the directory
     */
    private IDirectory directoryStub(String hostname) {
        IDirectory stub = null;

        try {
            Registry registry = LocateRegistry.getRegistry(hostname, RMI_REGISTER_PORT);
            stub = (IDirectory) registry.lookup("directory");
        } catch (NotBoundException e) {
            System.out.println("Erreur: Le nom '" + e.getMessage()
                    + "' n'est pas défini dans le registre.");
        } catch (AccessException e) {
            System.out.println("Erreur: " + e.getMessage());
        } catch (RemoteException e) {
            System.out.println("Erreur: " + e.getMessage());
        }

        return stub;
    }

    /**
     * Connect to a server and return its stub
     * @param hostname IP address of the server
     * @return Stub object of the server
     */
    private IServeur serverStub(String hostname) {
        IServeur stub = null;

        try {
            Registry registry = LocateRegistry.getRegistry(hostname, RMI_REGISTER_PORT);
            stub = (IServeur) registry.lookup("server");
        } catch (NotBoundException e) {
            System.out.println("Erreur: Le nom '" + e.getMessage()
                    + "' n'est pas défini dans le registre.");
        } catch (AccessException e) {
            System.out.println("Erreur: " + e.getMessage());
        } catch (RemoteException e) {
            System.out.println("Erreur: " + e.getMessage());
        }

        return stub;
    }

    /**
     * Read the task file
     * @param pathToFile the task file
     * @return list of operations in the task
     */
    private List<Map.Entry<String, Integer>> readTaskFile(String pathToFile) {
        List<Map.Entry<String, Integer>> operations = new ArrayList<>();

        try {
            File file = new File(pathToFile);
            Scanner input = new Scanner(file);

            while (input.hasNext()) {
                String operation  = input.next();
                int argument  = Integer.parseInt(input.next());
                Map.Entry<String, Integer> entry = new AbstractMap.SimpleEntry(operation, argument);
                operations.add(entry);
            }
        } catch (IOException e) {
            System.out.println("Erreur: " + e.getMessage());
        }

        return operations;
    }

    /**
     * Distribute the workload of a task evenly among the available server and get the result
     * @param task the given task
     * @param taux the tolerated rate of rejection of a task
     * @param isSecuredMode the security mode of the distributor
     * @return the result of the given task
     */
    private int distributeTask(List<Map.Entry<String, Integer>> task, float taux, boolean isSecuredMode) {
        int allResult = 0;

        int i = 0;  // task index
        int j = 0;  // server index
        do {
            // prepare subtask
            List<Map.Entry<String, Integer>> subTask;
            int operationPerTask = calculateOperationPerTask(serverStubs[j].getValue(), taux);
            if (i+operationPerTask >= task.size()) {
                subTask = task.subList(i, task.size());
                i = task.size();
            } else {
                subTask = task.subList(i, i + operationPerTask);
                i += operationPerTask;
            }

            try {
                int result;
                int index = j;
                if (isSecuredMode) {
                    // loop until result is found and try another server in case a server failed to accept the task
                    do {
                        result = sendOperationToServer(subTask, index);
                        index++;
                        index %= serverStubs.length;
                    } while(result == -1);
                } else {
                    // Loop until a result is found, every time sending the same task to 2 servers and verify that both result match before accepting it
                    int result1, result2;
                    do {
                        result1 = sendOperationToServer(subTask, index);
                        result2 = sendOperationToServer(subTask, getRandomServerIndex(index));
                        index++;
                        index %= serverStubs.length;
                    } while(result1 != result2 && (result1 == -1 || result2 == -1));

                    result = result1;
                }
                allResult += result;

                j++;
                j %= serverStubs.length;
            } catch (RemoteException e) {
                System.out.println("Erreur: " + e.getMessage());
            }
            allResult %= 4000;
        } while(i < task.size());

        return allResult;
    }

    /**
     * Return a random index identifying the server
     * @param exception the undesired index
     * @return index of a server
     */
    private int getRandomServerIndex(int exception) {
        Random rand = new Random();
        int newServerIndex;

        do {
            newServerIndex = rand.nextInt(serverStubs.length);
        } while(newServerIndex == exception);

        return newServerIndex;
    }

    /**
     * Call remote server function to perform a given task
     * @param task the given task
     * @param serverIndex the index of the server
     * @return the result of the task
     * @throws RemoteException
     */
    private int sendOperationToServer(List<Map.Entry<String, Integer>> task, int serverIndex) throws RemoteException {
        return serverStubs[serverIndex].getKey().doTask(task, username, password);
    }

    /**
     * THIS FUNCTION IS FOR TEST PURPOSE ONLY
     * TODO REMOVE THIS FUNCTION AFTER DONE USING IT
     * @param c capcity
     * @param t acceptance rate
     * @return the number of operation per task
     */
    private int calculateOperationPerTask(int c, float t) {
        int n = (int)Math.floor(t * (4*c) + c);
        return n;
    }
}
