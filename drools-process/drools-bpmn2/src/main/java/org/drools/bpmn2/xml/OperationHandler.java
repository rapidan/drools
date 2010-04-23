package org.drools.bpmn2.xml;

import java.util.HashSet;

import org.drools.bpmn2.core.Interface;
import org.drools.bpmn2.core.Interface.Operation;
import org.drools.xml.BaseAbstractHandler;
import org.drools.xml.ExtensibleXmlParser;
import org.drools.xml.Handler;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class OperationHandler extends BaseAbstractHandler implements Handler {
	
	@SuppressWarnings("unchecked")
	public OperationHandler() {
		if ((this.validParents == null) && (this.validPeers == null)) {
			this.validParents = new HashSet();
			this.validParents.add(Interface.class);

			this.validPeers = new HashSet();
			this.validPeers.add(null);

			this.allowNesting = false;
		}
	}

    public Object start(final String uri, final String localName,
			            final Attributes attrs, final ExtensibleXmlParser parser)
			throws SAXException {
		parser.startElementBuilder(localName, attrs);

		String id = attrs.getValue("id");
		String name = attrs.getValue("name");
		
		Interface i = (Interface) parser.getParent();
        Operation operation = i.addOperation(id, name);
		return operation;
	}

	public Object end(final String uri, final String localName,
			          final ExtensibleXmlParser parser) throws SAXException {
		parser.endElementBuilder();
		return parser.getCurrent();
	}

	public Class<?> generateNodeFor() {
		return Operation.class;
	}

}