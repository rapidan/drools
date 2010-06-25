package org.drools.bpmn2.legacy.beta1;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.drools.bpmn2.core.Lane;
import org.drools.definition.process.WorkflowProcess;
import org.drools.ruleflow.core.RuleFlowProcess;
import org.drools.xml.BaseAbstractHandler;
import org.drools.xml.ExtensibleXmlParser;
import org.drools.xml.Handler;
import org.w3c.dom.Element;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class LaneHandler extends BaseAbstractHandler implements Handler {
	
    public static final String LANES = "BPMN.Lanes";

	@SuppressWarnings("unchecked")
	public LaneHandler() {
		if ((this.validParents == null) && (this.validPeers == null)) {
			this.validParents = new HashSet();
			this.validParents.add(RuleFlowProcess.class);

			this.validPeers = new HashSet();
			this.validPeers.add(null);

			this.allowNesting = false;
		}
	}

	@SuppressWarnings("unchecked")
    public Object start(final String uri, final String localName,
			            final Attributes attrs, final ExtensibleXmlParser parser)
			throws SAXException {
		parser.startElementBuilder(localName, attrs);

		String id = attrs.getValue("id");
		String name = attrs.getValue("name");
		
		WorkflowProcess process = (WorkflowProcess) parser.getParent();
        
		List<Lane> lanes = (List<Lane>)
            ((RuleFlowProcess) process).getMetaData(LaneHandler.LANES);          
        if (lanes == null) {
            lanes = new ArrayList<Lane>();
            ((RuleFlowProcess) process).setMetaData(LaneHandler.LANES, lanes);
        }
        Lane lane = new Lane(id); 
        lane.setName(name);
        lanes.add(lane);
		return lane;
	}

	public Object end(final String uri, final String localName,
			          final ExtensibleXmlParser parser) throws SAXException {
        final Element element = parser.endElementBuilder();
        Lane lane = (Lane) parser.getCurrent();
        
        org.w3c.dom.Node xmlNode = element.getFirstChild();
        while (xmlNode != null) {
            String nodeName = xmlNode.getNodeName();
            if ("flowElementRef".equals(nodeName)) {
                String flowElementRef = xmlNode.getTextContent();
                lane.addFlowElement(flowElementRef);
            }
            xmlNode = xmlNode.getNextSibling();
        }
        return lane;
    }

	public Class<?> generateNodeFor() {
		return Lane.class;
	}

}