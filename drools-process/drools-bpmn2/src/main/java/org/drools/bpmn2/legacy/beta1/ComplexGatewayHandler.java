package org.drools.bpmn2.legacy.beta1;

import org.drools.workflow.core.Node;
import org.drools.workflow.core.node.Join;
import org.drools.workflow.core.node.Split;
import org.xml.sax.Attributes;

public class ComplexGatewayHandler extends AbstractNodeHandler {
    
    protected Node createNode(Attributes attrs) {
        final String type = attrs.getValue("gatewayDirection");
        if ("converging".equals(type)) {
        	Join join = new Join();
        	join.setType(Join.TYPE_UNDEFINED);
        	return join;
        } else if ("diverging".equals(type)) {
        	Split split = new Split();
        	split.setType(Split.TYPE_UNDEFINED);
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
