package ca.polymtl.inf8480.tp1.partie2.ifileserver;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface IFileServer extends Remote {
    boolean create(String username, String password, String filename) throws RemoteException;
    String lock(String username, String password, String filename, String checksum) throws RemoteException;
    List<String> list(String username, String password) throws RemoteException;
}
