package ca.polymtl.inf8480.tp1.partie2.iauthserver;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IAuthServer extends Remote {
    boolean newUser(String user, String password) throws RemoteException;
    boolean verify(String user, String password) throws RemoteException;
}
