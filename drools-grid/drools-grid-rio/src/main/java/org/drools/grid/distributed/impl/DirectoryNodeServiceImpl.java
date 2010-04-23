/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.drools.grid.distributed.impl;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.drools.KnowledgeBase;
import org.drools.command.KnowledgeContextResolveFromContextCommand;
import org.drools.command.runtime.GetKnowledgeBaseCommand;
import org.drools.grid.generic.Message;
import org.drools.grid.ExecutionNodeService;
import org.drools.grid.DirectoryNodeService;
import org.drools.grid.generic.GenericNodeConnector;


/**
 *
 * @author salaboy
 */
public class DirectoryNodeServiceImpl implements DirectoryNodeService{

    private String id;

    private Map<String, String> directoryMap = new HashMap<String, String>();

    private Map<String, String> kbaseDirectoryMap = new HashMap<String, String>();

    private Iterable<ExecutionNodeService> nodeServices;

    public DirectoryNodeServiceImpl() {
        nodeServices = new ArrayList<ExecutionNodeService>();
        this.id = UUID.randomUUID().toString();
    }

    @Override
    public String getId() throws RemoteException {
        return id;
    }
    
    @Override
    public void register(String sessionId, String nodeServiceId) throws RemoteException {
        //System.out.println("Registering: "+  sessionId + " -- "+nodeServiceId);
        directoryMap.put(sessionId, nodeServiceId);
        for(ExecutionNodeService node : nodeServices){
            if(node.getId().equals(nodeServiceId)){
                node.incrementKsessionCounter();
            }
        }
    }

    @Override
    public GenericNodeConnector lookup(String sessionId) throws RemoteException {
        ExecutionNodeService nodeService = null;
        String sessionServiceId = (String)directoryMap.get(sessionId);
        //System.out.println("Registry = "+ directoryMap.toString());
        //System.out.println("Nodes Services = "+nodeServices);
        for(ExecutionNodeService ss : nodeServices){
            //System.out.println("Session Service id = "+ss.getId() + "needs to match with ="+sessionServiceId);
            if(ss.getId().equals(sessionServiceId)){
                nodeService = ss;
            }
        }     

        return nodeService;
    }


    public void setNodeServices(Iterable<ExecutionNodeService> nodeServices) {
        this.nodeServices = nodeServices;
    }

    @Override
    public void registerKBase(String kbaseId, String nodeServiceId) throws RemoteException {
        //System.out.println("Registering KnowledgeBase = "+kbaseId +" -in NS=" +nodeServiceId);
        kbaseDirectoryMap.put(kbaseId, nodeServiceId);
    }

    @Override
    public KnowledgeBase lookupKBase(String kbaseId) throws RemoteException{
        ExecutionNodeService nodeService = null;
        String nodeServiceId = (String)kbaseDirectoryMap.get(kbaseId);
        //System.out.println("Kbase Registry = "+ kbaseDirectoryMap.toString());
        //System.out.println("Session Services = "+nodeService);
        for(ExecutionNodeService ns : nodeServices){
            //System.out.println("Node Service id = "+ns.getId() + "needs to match with ="+nodeServiceId);
            if(ns.getId().equals(nodeServiceId)){
                nodeService = ns;
            }
        }
        //System.out.println("Node Service = "+nodeService);
        
        try {
            //@TODO: This is a bad hack.. I need to improve this a lot
            Message msg = nodeService.write(new Message(999,1000,false, new KnowledgeContextResolveFromContextCommand( new GetKnowledgeBaseCommand(), null, kbaseId, null, null)));
            //System.out.println("MSG returned by nodeService = "+msg);
            if (msg.getPayload() instanceof KnowledgeBase) {
                //System.out.println("Kbase in the payload: "+(KnowledgeBase)msg.getPayload());
                return (KnowledgeBase)msg.getPayload();
            }
            return null;
        } catch ( Exception e ) {
            throw new RuntimeException( "Unable to execute message",
                                        e );
        }
    }

    @Override
    public void addService(GenericNodeConnector service) {
        ((List)nodeServices).add(service);
    }

    @Override
    public Map<String, String> getDirectoryMap()  throws RemoteException{
        return directoryMap;
    }


}
