package org.drools.bpmn2.core;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Lane implements Serializable {
    
	private static final long serialVersionUID = 4L;
	
    private String id;
    private String name;
    private List<String> flowElementIds = new ArrayList<String>();
    
    public Lane(String id) {
        this.id = id;
    }
    
    public String getId() {
        return id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public List<String> getFlowElements() {
        return flowElementIds;
    }
    
    public void addFlowElement(String id) {
        flowElementIds.add(id);
    }
    
}
