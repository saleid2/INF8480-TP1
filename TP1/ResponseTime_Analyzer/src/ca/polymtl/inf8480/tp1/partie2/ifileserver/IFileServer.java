package ca.polymtl.inf8480.tp1.partie2.ifileserver;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.List;

public interface IFileServer extends Remote {
    boolean create(String username, String password, String filename) throws RemoteException;
    List<String> list(String username, String password) throws RemoteException;
    HashMap<String, byte[]> syncLocalDirectory(String username, String password) throws RemoteException;
    byte[] get(String username, String password, String filename, String checksum) throws RemoteException;
    String lock(String username, String password, String filename, String checksum) throws RemoteException;
    boolean push(String username, String password, String filename, byte[] fileContent) throws RemoteException;
}
