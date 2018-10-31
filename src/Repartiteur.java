/**
 * Created by alhua on 18-10-17.
 */

import Interface.IRepartiteur;
import Interface.IServeur;
import Interface.IDirectory;

import java.util.Random;
import java.util.AbstractMap;
import java.util.Map;
import java.io.*;
import java.util.Scanner;
import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.ConnectException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;

public class Repartiteur implements IRepartiteur {
    public static void main(String[] args) {
        String pathToFile = args[0];		                        // chemin au fichier de calcul
        String directoryHostname = args[1];		                    // addresse ip du serveur de service de nom
        boolean isSecuredMode = Boolean.parseBoolean(args[2]);		// mode de securite
        int capacity = Integer.parseInt(args[3]);                   // capacite de chaque serveur de calcul
        float taux = Float.parseFloat(args[4]);                     // TODO test parameter only, REMOVE BEFORE REMISE

        Repartiteur distributor = new Repartiteur(directoryHostname);
        distributor.run();

        List<Map.Entry<String, Integer>> task = distributor.readTaskFile(pathToFile);
        int operationPerTask = distributor.calculateOperationPerTask(capacity, taux);
        int finalResult = distributor.distributeTask(task, operationPerTask, isSecuredMode);

        System.out.println("Cette tache donne un resultat de " + finalResult);
    }
    private IDirectory directoryStub;
    private IServeur[] serverStubs;

    public Repartiteur(String directoryHostname) {
        directoryStub = directoryStub(directoryHostname);
    }

    private void run() {
        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new SecurityManager());
        }

        try {
            IRepartiteur stub = (IRepartiteur) UnicastRemoteObject
                    .exportObject(this, 0);

            Registry registry = LocateRegistry.getRegistry();
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
     * Connect to a distributor and return its stub
     * @param hostname IP address of the distributor
     * @return Stub object of the distributor
     */
    private IDirectory directoryStub(String hostname) {
        IDirectory stub = null;

        try {
            Registry registry = LocateRegistry.getRegistry(hostname);
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

    private IServeur[] getListOfServer() {
        // TODO call directory server function to get list of server
    }

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

    private int distributeTask(List<Map.Entry<String, Integer>> task, int operationPerTask, boolean isSecuredMode) {
        int allResult = 0;

        int i = 0;  // task index
        int j = 0;  // server index
        do {
            List<Map.Entry<String, Integer>> subTask;
            if (i+operationPerTask >= task.size()) {
                subTask = task.subList(i, task.size());
                i = task.size();
            } else {
                subTask = task.subList(i, i + operationPerTask);
                i += operationPerTask;
            }

            try {
                if (isSecuredMode) {
                    int result;
                    do {
                        result = sendOperationToServer(subTask, serverStubs[j]);
                        allResult += result;
                    } while(result == -1);
                } else {
                    // Loop until a result is found.
                    int result1, result2;
                    int index = j;
                    do {
                        result1 = sendOperationToServer(subTask, serverStubs[index]);
                        result2 = sendOperationToServer(subTask, getRandomServer(index));
                        index++;
                        index %= serverStubs.length;
                    } while(result1 != result2 && (result1 == -1 || result2 == -1));

                    allResult += result1;
                }

                j++;
                j %= serverStubs.length;
            } catch (RemoteException e) {
                System.out.println("Erreur: " + e.getMessage());
            }
            allResult %= 4000;
        } while(i < task.size());

        return allResult;
    }

    private IServeur getRandomServer(int exception) {
        Random rand = new Random();
        int newServerIndex;

        do {
            newServerIndex = rand.nextInt(serverStubs.length);
        } while(newServerIndex == exception);

        return serverStubs[newServerIndex];
    }

    private int sendOperationToServer(List<Map.Entry<String, Integer>> task, IServeur server) throws RemoteException {
        // TODO call dotask from IServeur
        return server.doTask(task);
    }

    private int calculateOperationPerTask(int c, float t) {
        int n = (int)Math.floor(t * (4*c) + c);
        return n;
    }
}
