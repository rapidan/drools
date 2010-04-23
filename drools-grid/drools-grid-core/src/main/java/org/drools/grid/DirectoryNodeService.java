/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.drools.grid;

import java.rmi.RemoteException;
import java.util.Map;
import org.drools.KnowledgeBase;
import org.drools.grid.generic.GenericNodeConnector;


/**
 *
 * @author salaboy
 */

public interface DirectoryNodeService {
    public void register(String executorId, String nodeServiceId) throws RemoteException;
    public GenericNodeConnector lookup(String executorId) throws RemoteException;
    public void registerKBase(String kbaseId, String nodeServiceId) throws RemoteException;
    public KnowledgeBase lookupKBase(String kbaseId) throws RemoteException;
    public void addService(GenericNodeConnector service);
    public Map<String, String> getDirectoryMap() throws RemoteException;
    public String getId() throws RemoteException;
}
