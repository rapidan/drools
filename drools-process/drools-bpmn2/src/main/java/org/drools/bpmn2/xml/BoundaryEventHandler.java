package org.drools.bpmn2.xml;

import java.util.ArrayList;
import java.util.List;

import org.drools.process.core.event.EventFilter;
import org.drools.process.core.event.EventTypeFilter;
import org.drools.workflow.core.Node;
import org.drools.workflow.core.NodeContainer;
import org.drools.workflow.core.node.EventNode;
import org.drools.xml.ExtensibleXmlParser;
import org.w3c.dom.Element;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class BoundaryEventHandler extends AbstractNodeHandler {
    
    protected Node createNode(Attributes attrs) {
        return new EventNode();
    }
    
    @SuppressWarnings("unchecked")
	public Class generateNodeFor() {
        return EventNode.class;
    }

    public Object end(final String uri, final String localName,
                      final ExtensibleXmlParser parser) throws SAXException {
        final Element element = parser.endElementBuilder();
        Node node = (Node) parser.getCurrent();
        String attachedTo = element.getAttribute("attachedToRef");
        String cancelActivityString = element.getAttribute("cancelActivity");
        boolean cancelActivity = true;
        if ("false".equals(cancelActivityString)) {
            cancelActivity = false;
        }
        // determine type of event definition, so the correct type of node
        // can be generated
        org.w3c.dom.Node xmlNode = element.getFirstChild();
        while (xmlNode != null) {
            String nodeName = xmlNode.getNodeName();
            if ("escalationEventDefinition".equals(nodeName)) {
                // reuse already created EventNode
                handleEscalationNode(node, element, uri, localName, parser, attachedTo, cancelActivity);
                break;
            } else if ("errorEventDefinition".equals(nodeName)) {
                // reuse already created EventNode
                handleErrorNode(node, element, uri, localName, parser, attachedTo, cancelActivity);
                break;
            } else if ("timerEventDefinition".equals(nodeName)) {
                // reuse already created EventNode
                handleTimerNode(node, element, uri, localName, parser, attachedTo, cancelActivity);
                break;
            } else if ("compensateEventDefinition".equals(nodeName)) {
                // reuse already created EventNode
                handleCompensationNode(node, element, uri, localName, parser, attachedTo, cancelActivity);
                break;
            }
            xmlNode = xmlNode.getNextSibling();
        }
        NodeContainer nodeContainer = (NodeContainer) parser.getParent();
        nodeContainer.addNode(node);
        return node;
    }
    
    protected void handleEscalationNode(final Node node, final Element element, final String uri, 
            final String localName, final ExtensibleXmlParser parser, final String attachedTo,
            final boolean cancelActivity) throws SAXException {
        super.handleNode(node, element, uri, localName, parser);
        EventNode eventNode = (EventNode) node;
        eventNode.setMetaData("AttachedTo", attachedTo);
        eventNode.setMetaData("CancelActivity", cancelActivity);
        org.w3c.dom.Node xmlNode = element.getFirstChild();
        while (xmlNode != null) {
            String nodeName = xmlNode.getNodeName();
            if ("escalationEventDefinition".equals(nodeName)) {
                String type = ((Element) xmlNode).getAttribute("escalationCode");
                if (type != null && type.trim().length() > 0) {
                    List<EventFilter> eventFilters = new ArrayList<EventFilter>();
                    EventTypeFilter eventFilter = new EventTypeFilter();
                    eventFilter.setType("Escalation-" + attachedTo + "-" + type);
                    eventFilters.add(eventFilter);
                    eventNode.setEventFilters(eventFilters);
                    eventNode.setMetaData("EscalationEvent", type);
                }
            }
            xmlNode = xmlNode.getNextSibling();
        }
    }
    
    protected void handleErrorNode(final Node node, final Element element, final String uri, 
            final String localName, final ExtensibleXmlParser parser, final String attachedTo,
            final boolean cancelActivity) throws SAXException {
        super.handleNode(node, element, uri, localName, parser);
        EventNode eventNode = (EventNode) node;
        eventNode.setMetaData("AttachedTo", attachedTo);
        org.w3c.dom.Node xmlNode = element.getFirstChild();
        while (xmlNode != null) {
            String nodeName = xmlNode.getNodeName();
            if ("errorEventDefinition".equals(nodeName)) {
                String type = ((Element) xmlNode).getAttribute("errorCode");
                if (type != null && type.trim().length() > 0) {
                    List<EventFilter> eventFilters = new ArrayList<EventFilter>();
                    EventTypeFilter eventFilter = new EventTypeFilter();
                    eventFilter.setType("Error-" + attachedTo + "-" + type);
                    eventFilters.add(eventFilter);
                    eventNode.setEventFilters(eventFilters);
                    eventNode.setMetaData("ErrorEvent", type);
                }
            }
            xmlNode = xmlNode.getNextSibling();
        }
    }
    
    protected void handleTimerNode(final Node node, final Element element, final String uri, 
            final String localName, final ExtensibleXmlParser parser, final String attachedTo,
            final boolean cancelActivity) throws SAXException {
        super.handleNode(node, element, uri, localName, parser);
        EventNode eventNode = (EventNode) node;
        eventNode.setMetaData("AttachedTo", attachedTo);
        eventNode.setMetaData("CancelActivity", cancelActivity);
        org.w3c.dom.Node xmlNode = element.getFirstChild();
        while (xmlNode != null) {
            String nodeName = xmlNode.getNodeName();
            if ("timerEventDefinition".equals(nodeName)) {
                String timeCycle = null;
                org.w3c.dom.Node subNode = xmlNode.getFirstChild();
                while (subNode instanceof Element) {
                    String subNodeName = subNode.getNodeName();
                    if ("timeCycle".equals(subNodeName)) {
                        timeCycle = subNode.getTextContent();
                        break;
                    }
                    subNode = subNode.getNextSibling();
                }
                if (timeCycle != null && timeCycle.trim().length() > 0) {
                    List<EventFilter> eventFilters = new ArrayList<EventFilter>();
                    EventTypeFilter eventFilter = new EventTypeFilter();
                    eventFilter.setType("Timer-" + attachedTo + "-" + timeCycle);
                    eventFilters.add(eventFilter);
                    eventNode.setEventFilters(eventFilters);
                    eventNode.setMetaData("TimeCycle", timeCycle);
                }
            }
            xmlNode = xmlNode.getNextSibling();
        }
    }
    
    protected void handleCompensationNode(final Node node, final Element element, final String uri, 
            final String localName, final ExtensibleXmlParser parser, final String attachedTo,
            final boolean cancelActivity) throws SAXException {
        super.handleNode(node, element, uri, localName, parser);
        EventNode eventNode = (EventNode) node;
        eventNode.setMetaData("AttachedTo", attachedTo);
        List<EventFilter> eventFilters = new ArrayList<EventFilter>();
        EventTypeFilter eventFilter = new EventTypeFilter();
        String eventType = "Compensate-";
        eventFilter.setType(eventType);
        eventFilters.add(eventFilter);
        ((EventNode) node).setEventFilters(eventFilters);
    }
    
	public void writeNode(Node node, StringBuilder xmlDump, boolean includeMeta) {
	    throw new IllegalArgumentException("Writing out should be handled by specific handlers");
    }

}
