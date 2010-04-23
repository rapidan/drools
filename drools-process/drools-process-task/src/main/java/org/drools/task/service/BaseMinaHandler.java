package org.drools.task.service;

import java.util.HashMap;
import java.util.Map;

import org.apache.mina.core.service.IoHandlerAdapter;

public class BaseMinaHandler extends IoHandlerAdapter
{       
    protected Map<Integer, ResponseHandler> responseHandlers;
    
    public BaseMinaHandler() {
        responseHandlers = new HashMap<Integer, ResponseHandler>();
    }
    
    public void addResponseHandler(int id, ResponseHandler responseHandler) {
        responseHandlers.put( id, responseHandler );
    }
    
}