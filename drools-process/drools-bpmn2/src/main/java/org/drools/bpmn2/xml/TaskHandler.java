package org.drools.bpmn2.xml;

import java.util.HashMap;
import java.util.Map;

import org.drools.process.core.Work;
import org.drools.process.core.impl.WorkImpl;
import org.drools.workflow.core.Node;
import org.drools.workflow.core.node.WorkItemNode;
import org.drools.xml.ExtensibleXmlParser;
import org.w3c.dom.Element;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class TaskHandler extends AbstractNodeHandler {
    
    protected Node createNode(Attributes attrs) {
        return new WorkItemNode();
    }
    
    @SuppressWarnings("unchecked")
	public Class generateNodeFor() {
        return Node.class;
    }

    protected void handleNode(final Node node, final Element element, final String uri, 
            final String localName, final ExtensibleXmlParser parser) throws SAXException {
    	super.handleNode(node, element, uri, localName, parser);
    	WorkItemNode workItemNode = (WorkItemNode) node;
        String name = getTaskName(element);
        Work work = new WorkImpl();
        work.setName(name);
    	workItemNode.setWork(work);
    	Map<String, String> dataInputs = new HashMap<String, String>();
    	Map<String, String> dataOutputs = new HashMap<String, String>();
    	org.w3c.dom.Node xmlNode = element.getFirstChild();
        while (xmlNode != null) {
        	String nodeName = xmlNode.getNodeName();
        	if ("ioSpecification".equals(nodeName)) {
        		readIoSpecification(xmlNode, dataInputs, dataOutputs);
        	} else if ("dataInputAssociation".equals(nodeName)) {
        		readDataInputAssociation(xmlNode, workItemNode, dataInputs);
        	} else if ("dataOutputAssociation".equals(nodeName)) {
        		readDataOutputAssociation(xmlNode, workItemNode, dataOutputs);
        	}
    		xmlNode = xmlNode.getNextSibling();
        }
	}
    
    protected String getTaskName(final Element element) {
        return element.getAttribute("taskName");
    }
    
    protected void readIoSpecification(org.w3c.dom.Node xmlNode, Map<String, String> dataInputs, Map<String, String> dataOutputs) {
    	org.w3c.dom.Node subNode = xmlNode.getFirstChild();
		while (subNode instanceof Element) {
			String subNodeName = subNode.getNodeName();
        	if ("dataInput".equals(subNodeName)) {
        		String id = ((Element) subNode).getAttribute("id");
        		String inputName = ((Element) subNode).getAttribute("name");
        		dataInputs.put(id, inputName);
        	}
        	if ("dataOutput".equals(subNodeName)) {
        		String id = ((Element) subNode).getAttribute("id");
        		String outputName = ((Element) subNode).getAttribute("name");
        		dataOutputs.put(id, outputName);
        	}
        	subNode = subNode.getNextSibling();
		}
    }
    
    protected void readDataInputAssociation(org.w3c.dom.Node xmlNode, WorkItemNode workItemNode, Map<String, String> dataInputs) {
		// sourceRef
		org.w3c.dom.Node subNode = xmlNode.getFirstChild();
		if ("assignment".equals(subNode.getNodeName())) {
			org.w3c.dom.Node subSubNode = subNode.getFirstChild();
			String from = subSubNode.getTextContent();
			subNode = subNode.getNextSibling();
			subNode = subNode.getNextSibling();
    		String to = subNode.getTextContent();
    		workItemNode.getWork().setParameter(dataInputs.get(to), from);
		} else {
    		String from = subNode.getTextContent();
    		// targetRef
    		subNode = subNode.getNextSibling();
    		String to = subNode.getTextContent();
    		workItemNode.addInMapping(
				dataInputs.get(to),
				from);
		}
    }
    
    protected void readDataOutputAssociation(org.w3c.dom.Node xmlNode, WorkItemNode workItemNode, Map<String, String> dataOutputs) {
		// sourceRef
		org.w3c.dom.Node subNode = xmlNode.getFirstChild();
		String from = subNode.getTextContent();
		// targetRef
		subNode = subNode.getNextSibling();
		String to = subNode.getTextContent();
		workItemNode.addOutMapping(dataOutputs.get(from), to);
    }

    @Override
    public void writeNode(Node node, StringBuilder xmlDump, boolean includeMeta) {
        throw new IllegalArgumentException(
            "Writing out should be handled by the WorkItemNodeHandler");
    }

}
