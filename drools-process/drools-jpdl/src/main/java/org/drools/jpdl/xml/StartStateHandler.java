package org.drools.jpdl.xml;

import org.drools.compiler.xml.processes.AbstractNodeHandler;
import org.drools.jpdl.core.node.StartState;
import org.drools.workflow.core.Node;

public class StartStateHandler extends AbstractNodeHandler {
    
    protected Node createNode() {
        return new StartState();
    }
    
    public Class generateNodeFor() {
        return StartState.class;
    }

	public void writeNode(Node node, StringBuilder xmlDump, boolean includeMeta) {
		StartState startNode = (StartState) node;
		writeNode("start", startNode, xmlDump, includeMeta);
        endNode(xmlDump);
	}

}
