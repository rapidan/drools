package org.drools.bpmn2.legacy.beta1;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.drools.bpmn2.core.Message;
import org.drools.compiler.xml.ProcessBuildData;
import org.drools.process.core.event.EventFilter;
import org.drools.process.core.event.EventTypeFilter;
import org.drools.process.core.timer.Timer;
import org.drools.workflow.core.Node;
import org.drools.workflow.core.NodeContainer;
import org.drools.workflow.core.node.EventNode;
import org.drools.workflow.core.node.StateNode;
import org.drools.workflow.core.node.TimerNode;
import org.drools.xml.ExtensibleXmlParser;
import org.w3c.dom.Element;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class IntermediateCatchEventHandler extends AbstractNodeHandler {
    
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
        // determine type of event definition, so the correct type of node
        // can be generated
        org.w3c.dom.Node xmlNode = element.getFirstChild();
        while (xmlNode != null) {
            String nodeName = xmlNode.getNodeName();
            if ("signalEventDefinition".equals(nodeName)) {
                // reuse already created EventNode
                handleSignalNode(node, element, uri, localName, parser);
                break;
            } else if ("messageEventDefinition".equals(nodeName)) {
                // reuse already created EventNode
                handleMessageNode(node, element, uri, localName, parser);
                break;
            } else if ("timerEventDefinition".equals(nodeName)) {
                // create new timerNode
                TimerNode timerNode = new TimerNode();
                timerNode.setId(node.getId());
                timerNode.setName(node.getName());
                timerNode.setMetaData("UniqueId", node.getMetaData("UniqueId"));
                node = timerNode;
                handleTimerNode(node, element, uri, localName, parser);
                break;
            } else if ("conditionalEventDefinition".equals(nodeName)) {
                // create new stateNode
                StateNode stateNode = new StateNode();
                stateNode.setId(node.getId());
                stateNode.setName(node.getName());
                stateNode.setMetaData("UniqueId", node.getMetaData("UniqueId"));
                node = stateNode;
                handleStateNode(node, element, uri, localName, parser);
                break;
            }
            xmlNode = xmlNode.getNextSibling();
        }
        NodeContainer nodeContainer = (NodeContainer) parser.getParent();
        nodeContainer.addNode(node);
        return node;
    }
    
    protected void handleSignalNode(final Node node, final Element element, final String uri, 
            final String localName, final ExtensibleXmlParser parser) throws SAXException {
        super.handleNode(node, element, uri, localName, parser);
        EventNode eventNode = (EventNode) node;
        org.w3c.dom.Node xmlNode = element.getFirstChild();
        while (xmlNode != null) {
            String nodeName = xmlNode.getNodeName();
            if ("dataOutputAssociation".equals(nodeName)) {
                readDataOutputAssociation(xmlNode, eventNode);
            } else if ("signalEventDefinition".equals(nodeName)) {
                String type = ((Element) xmlNode).getAttribute("signalRef");
                if (type != null && type.trim().length() > 0) {
                    List<EventFilter> eventFilters = new ArrayList<EventFilter>();
                    EventTypeFilter eventFilter = new EventTypeFilter();
                    eventFilter.setType(type);
                    eventFilters.add(eventFilter);
                    eventNode.setEventFilters(eventFilters);
                }
            }
            xmlNode = xmlNode.getNextSibling();
        }
    }
    
    @SuppressWarnings("unchecked")
    protected void handleMessageNode(final Node node, final Element element, final String uri, 
            final String localName, final ExtensibleXmlParser parser) throws SAXException {
        super.handleNode(node, element, uri, localName, parser);
        EventNode eventNode = (EventNode) node;
        org.w3c.dom.Node xmlNode = element.getFirstChild();
        while (xmlNode != null) {
            String nodeName = xmlNode.getNodeName();
            if ("dataOutputAssociation".equals(nodeName)) {
                readDataOutputAssociation(xmlNode, eventNode);
            } else if ("messageEventDefinition".equals(nodeName)) {
                String messageRef = ((Element) xmlNode).getAttribute("messageRef");
                Map<String, Message> messages = (Map<String, Message>)
                    ((ProcessBuildData) parser.getData()).getMetaData("Messages");
                if (messages == null) {
                    throw new IllegalArgumentException("No messages found");
                }
                Message message = messages.get(messageRef);
                if (message == null) {
                    throw new IllegalArgumentException("Could not find message " + messageRef);
                }
                eventNode.setMetaData("MessageType", message.getType());
                List<EventFilter> eventFilters = new ArrayList<EventFilter>();
                EventTypeFilter eventFilter = new EventTypeFilter();
                eventFilter.setType("Message-" + messageRef);
                eventFilters.add(eventFilter);
                eventNode.setEventFilters(eventFilters);
            }
            xmlNode = xmlNode.getNextSibling();
        }
    }
    
    protected void handleTimerNode(final Node node, final Element element, final String uri, 
            final String localName, final ExtensibleXmlParser parser) throws SAXException {
        super.handleNode(node, element, uri, localName, parser);
        TimerNode timerNode = (TimerNode) node;
        org.w3c.dom.Node xmlNode = element.getFirstChild();
        while (xmlNode != null) {
            String nodeName = xmlNode.getNodeName();
            if ("timerEventDefinition".equals(nodeName)) {
                Timer timer = new Timer();
                org.w3c.dom.Node subNode = xmlNode.getFirstChild();
                while (subNode instanceof Element) {
                    String subNodeName = subNode.getNodeName();
                    if ("timeCycle".equals(subNodeName)) {
                        String period = subNode.getTextContent();
                        timer.setDelay(period);
                        break;
                    }
                    subNode = subNode.getNextSibling();
                }
                timerNode.setTimer(timer);
            }
            xmlNode = xmlNode.getNextSibling();
        }
    }
    
    protected void handleStateNode(final Node node, final Element element, final String uri, 
            final String localName, final ExtensibleXmlParser parser) throws SAXException {
        super.handleNode(node, element, uri, localName, parser);
        StateNode stateNode = (StateNode) node;
        org.w3c.dom.Node xmlNode = element.getFirstChild();
        while (xmlNode != null) {
            String nodeName = xmlNode.getNodeName();
            if ("conditionalEventDefinition".equals(nodeName)) {
                org.w3c.dom.Node subNode = xmlNode.getFirstChild();
                while (subNode != null) {
                    String subnodeName = subNode.getNodeName();
                    if ("condition".equals(subnodeName)) {
                        stateNode.setMetaData("Condition", xmlNode.getTextContent());
                        break;
                    }
                    subNode = subNode.getNextSibling();
                }
            }
            xmlNode = xmlNode.getNextSibling();
        }
    }
    
    protected void readDataOutputAssociation(org.w3c.dom.Node xmlNode, EventNode eventNode) {
		// sourceRef
		org.w3c.dom.Node subNode = xmlNode.getFirstChild();
		// targetRef
		subNode = subNode.getNextSibling();
		String to = subNode.getTextContent();
		eventNode.setVariableName(to);
    }

	public void writeNode(Node node, StringBuilder xmlDump, boolean includeMeta) {
	    throw new IllegalArgumentException("Writing out should be handled by specific handlers");
    }

}
