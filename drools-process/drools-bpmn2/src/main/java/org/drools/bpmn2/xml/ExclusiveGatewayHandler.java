package org.drools.bpmn2.xml;

import org.drools.workflow.core.Node;
import org.drools.workflow.core.node.Join;
import org.drools.workflow.core.node.Split;
import org.xml.sax.Attributes;

public class ExclusiveGatewayHandler extends AbstractNodeHandler {
    
    protected Node createNode(Attributes attrs) {
        final String type = attrs.getValue("gatewayDirection");
        if ("Converging".equals(type)) {
        	Join join = new Join();
        	join.setType(Join.TYPE_XOR);
        	return join;
        } else if ("Diverging".equals(type)) {
        	Split split = new Split();
        	split.setType(Split.TYPE_XOR);
        	String isDefault = attrs.getValue("default");
        	split.setMetaData("Default", isDefault);
        	return split;
        } else {
        	throw new IllegalArgumentException(
    			"Unknown gateway direction: " + type);
        }
    }
    
    @SuppressWarnings("unchecked")
	public Class generateNodeFor() {
        return Node.class;
    }

	public void writeNode(Node node, StringBuilder xmlDump, boolean includeMeta) {
		throw new IllegalArgumentException("Writing out should be handled by split / join handler");
	}

}
