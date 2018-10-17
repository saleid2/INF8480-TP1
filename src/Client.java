/**
 * Created by alhua on 18-10-17.
 */

import Interface.IRepartiteur;

import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class Client {
    public static void main(String[] args) {
        String pathToFile = args[0];		// chemin au fichier de calcul

        Client client = new Client();
        client.sendOperations(pathToFile);
    }

    IRepartiteur distributor = null;

    public Client() {
        distributor = distributorStub("127.0.0.1");    //TEMPORARY IP ADDRESS
    }

    private void sendOperations(String path) {
        //TODO send operations data to distributor and wait for the result
    }

    /**
     * Connect to a distributor and return its stub
     * @param hostname IP address of the distributor
     * @return Stub object of the distributor
     */
    private IRepartiteur distributorStub(String hostname) {
        IRepartiteur stub = null;

        try {
            Registry registry = LocateRegistry.getRegistry(hostname);
            stub = (IRepartiteur) registry.lookup("distributor");
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
}
