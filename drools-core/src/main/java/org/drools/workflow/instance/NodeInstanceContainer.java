package org.drools.workflow.instance;

import java.util.Collection;

import org.drools.knowledge.definitions.process.Node;
import org.drools.knowledge.definitions.process.NodeContainer;
import org.drools.process.instance.NodeInstance;

/**
 * 
 * @author <a href="mailto:kris_verlaenen@hotmail.com">Kris Verlaenen</a>
 */
public interface NodeInstanceContainer extends org.drools.process.instance.NodeInstanceContainer {

    Collection<NodeInstance> getNodeInstances(boolean recursive);

    NodeInstance getFirstNodeInstance(long nodeId);

    NodeInstance getNodeInstance(Node node);

    void addNodeInstance(NodeInstance nodeInstance);

    void removeNodeInstance(NodeInstance nodeInstance);
    
    NodeContainer getNodeContainer();

}
