package ca.polymtl.inf8480.tp1.partie2.ifileserver;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IFileServer extends Remote {
    boolean create(String username, String password, String filename) throws RemoteException;
    String lock(String username, String password, String filename, String checksum) throws RemoteException;
}
