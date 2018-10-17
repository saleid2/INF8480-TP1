/**
 * Created by alhua on 18-10-17.
 */

import Interface.IRepartiteur;

import java.rmi.ConnectException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class Repartiteur implements IRepartiteur {
    public static void main(String[] args) {
        Repartiteur distributor = new Repartiteur();
        distributor.run();
    }

    public Repartiteur() {

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
            System.err
                    .println("Impossible de se connecter au registre RMI. Est-ce que rmiregistry est lancé ?");
            System.err.println();
            System.err.println("Erreur: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Erreur: " + e.getMessage());
        }
    }
}
