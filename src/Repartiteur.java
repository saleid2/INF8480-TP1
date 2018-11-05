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
import java.util.concurrent.*;

public class Repartiteur implements IRepartiteur {
    private int RMI_REGISTER_PORT = 5001;
    private int port = 5002;

    private IDirectory directoryStub;
    private List<Map.Entry<IServeur, Integer>> serverStubs;
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


        if (args.length < 4) {
            taux = 0.1f;
            isSecuredMode = true;
        } else {
            taux = Float.parseFloat(args[4]);
            isSecuredMode = Boolean.parseBoolean(args[5]);
        }

        Repartiteur distributor = new Repartiteur(directoryHostname, username, password);
        distributor.run();

        List<Map.Entry<String, Integer>> task = distributor.readTaskFile(pathToFile);

        long startTime = System.nanoTime();
        int finalResult = distributor.distributeTask(task, taux, isSecuredMode);
        long endTime = System.nanoTime();

        long duration = (endTime - startTime) / 1000;  //divide by 1000000 to get milliseconds.

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
    public void updateServerList() throws RemoteException {
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
                serverList.add(new AbstractMap.SimpleEntry<IServeur, Integer>(server, entry.getValue() ));
            }
            serverStubs = serverList;
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
            stub = (IDirectory) registry.lookup("serverdirectory");
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
                Map.Entry<String, Integer> entry = new AbstractMap.SimpleEntry<>(operation, argument);
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
     * @return the result of the given taskMap.Entry<IServeur, Integer>
     */
    private int distributeTask(List<Map.Entry<String, Integer>> task, float taux, boolean isSecuredMode) {
        int result;

        if (isSecuredMode) {
            result = securedMode(task, taux);
        } else {
            result = unsecuredMode(task, taux);
        }

        return result;
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
            newServerIndex = rand.nextInt(serverStubs.size());
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
        return sendOperationToServer(task, serverStubs.get(serverIndex).getKey());
    }

    /**
     * Call remote server function to perform a given task
     * @param task the given task
     * @param server the server to execute task
     * @return the result of the task
     * @throws RemoteException
     */
    private int sendOperationToServer(List<Map.Entry<String, Integer>> task, IServeur server) throws RemoteException {
        return server.doTask(task, username, password);
    }

    /**
     * Return the number of operation in a task based on the server capacity and server acceptance rate.
     * @param c capacity
     * @param t acceptance rate
     * @return the number of operation per task
     */
    private int calculateOperationPerTask(int c, float t) {
        int n = (int)Math.floor(t * (4*c) + c);
        return n;
    }

    /**
     * Run in secured mode
     * @param task task to complete
     * @param taux tolerated server rejection rate
     * @return result of the task
     */
    private int securedMode(List<Map.Entry<String, Integer>> task, float taux) {
        int finalResult = 0;
        int j = 0;  // server index
        int i = 0; // task index

        ExecutorService executorService = Executors.newFixedThreadPool(serverStubs.size());
        List<Future<Integer>> futures = new ArrayList<Future<Integer>>();

        do {
            try {
                if (serverStubs.get(j).getKey().isServerFree()) {
                    // Necessary to use j in inner function
                    final int server_index = j;


                    // prepare subtask
                    List<Map.Entry<String, Integer>> sublist;
                    int operationPerTask = calculateOperationPerTask(serverStubs.get(server_index).getValue(), taux);
                    if (i + operationPerTask >= task.size()) {
                        sublist = task.subList(i, task.size());
                        i = task.size();
                    } else {
                        sublist = task.subList(i, i + operationPerTask);
                        i += operationPerTask;
                    }

                    List<Map.Entry<String, Integer>> subTask = new ArrayList<>();
                    subTask.addAll(sublist);

                    futures.add(executorService.submit(new Callable<Integer>() {
                        @Override
                        public Integer call() throws Exception {

                            // loop until result is found and try another server in case a server failed to accept the task
                            int requestResult;
                            do {
                                try {
                                    requestResult = sendOperationToServer(subTask, serverStubs.get(server_index).getKey());
                                } catch (RemoteException e) {
                                    requestResult = -1;
                                }
                            } while (requestResult == -1);

                            return requestResult;
                        }
                    }));

                    j = ( j + 1 )%serverStubs.size(); // Increment j in round-robin
                }
            } catch (RemoteException e) {
                System.out.println("Erreur: " + e.getMessage());
            }
        } while(i < task.size());

        for (Future<Integer> future : futures) {
            int result = 0;
            try {
                result = future.get();
            } catch (ExecutionException | InterruptedException e) {
                System.out.println("Erreur: " + e.getMessage());
            }
            finalResult += result;
            finalResult %= 4000;
        }

        return finalResult;
    }

    private int unsecuredMode(final List<Map.Entry<String, Integer>> task, float taux) {
        int finalResult = 0;
        int j = 0;  // server index
        int i = 0; // task index

        ExecutorService executorService = Executors.newCachedThreadPool();
        List<Future<Integer>> futures = new ArrayList<Future<Integer>>();

        do {
            try {
                if (serverStubs.get(j).getKey().isServerFree()) {
                    // Necessary to use j in inner function
                    final int first_server_index = j;

                    int k = j;
                    while (!serverStubs.get(k).getKey().isServerFree() || j == k) {
                        k = (k + 1) % serverStubs.size();
                    }

                    final int second_server_index = k;


                    int lowest_capacity_server_index = serverStubs.get(j).getValue() < serverStubs.get(k).getValue() ? j : k;

                    // prepare subtask
                    List<Map.Entry<String, Integer>> sublist;
                    int operationPerTask = calculateOperationPerTask(serverStubs.get(lowest_capacity_server_index).getValue(), taux);
                    if (i + operationPerTask >= task.size()) {
                        sublist = task.subList(i, task.size());
                        i = task.size();
                    } else {
                        sublist = task.subList(i, i + operationPerTask);
                        i += operationPerTask;
                    }

                    List<Map.Entry<String, Integer>> subTask = new ArrayList<>();
                    subTask.addAll(sublist);

                    futures.add(executorService.submit(new Callable<Integer>() {
                        @Override
                        public Integer call() throws Exception {

                            // loop until result is found and try another server in case a server failed to accept the task
                            int requestResult1;
                            int requestResult2;

                            do {
                                try {
                                    requestResult1 = sendOperationToServer(subTask, serverStubs.get(first_server_index).getKey());
                                    requestResult2 = sendOperationToServer(subTask, serverStubs.get(second_server_index).getKey());
                                } catch (RemoteException e) {
                                    requestResult1 = -1;
                                    requestResult2 = -1;
                                }
                            } while (requestResult1 == -1 || requestResult2 == -1 || requestResult1 != requestResult2);


                            return requestResult1;
                        }
                    }));

                    j = ( j + 1 )%serverStubs.size(); // Increment j in round-robin
                }
            } catch (RemoteException e) {
                System.out.println("Erreur: " + e.getMessage());
            }

        } while(i < task.size());

        for (Future<Integer> future : futures) {
            int result = 0;
            try {
                result = future.get();
            } catch (ExecutionException | InterruptedException e) {
                System.out.println("Erreur: " + e.getMessage());
            }
            finalResult += result;
            finalResult %= 4000;
        }

        return finalResult;
    }
}
