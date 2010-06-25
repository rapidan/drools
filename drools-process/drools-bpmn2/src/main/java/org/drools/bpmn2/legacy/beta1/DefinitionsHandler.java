package org.drools.bpmn2.legacy.beta1;

import java.util.HashSet;

import org.drools.bpmn2.core.Definitions;
import org.drools.compiler.xml.ProcessBuildData;
import org.drools.ruleflow.core.RuleFlowProcess;
import org.drools.xml.BaseAbstractHandler;
import org.drools.xml.ExtensibleXmlParser;
import org.drools.xml.Handler;
import org.w3c.dom.Element;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class DefinitionsHandler extends BaseAbstractHandler implements Handler {
	
	public static final String CONNECTIONS = "BPMN.Connections";

	@SuppressWarnings("unchecked")
	public DefinitionsHandler() {
		if ((this.validParents == null) && (this.validPeers == null)) {
			this.validParents = new HashSet();
			this.validParents.add(null);

			this.validPeers = new HashSet();
			this.validPeers.add(null);

			this.allowNesting = false;
		}
	}

	public Object start(final String uri, final String localName,
			            final Attributes attrs, final ExtensibleXmlParser parser)
			throws SAXException {
		parser.startElementBuilder(localName, attrs);
		return new Definitions();
	}

	public Object end(final String uri, final String localName,
			          final ExtensibleXmlParser parser) throws SAXException {
		final Element element = parser.endElementBuilder();
		Definitions definitions = (Definitions) parser.getCurrent();
        RuleFlowProcess process = (RuleFlowProcess)
			((ProcessBuildData) parser.getData()).getProcess();
		String namespace = element.getAttribute("targetNamespace");
		process.setMetaData("TargetNamespace", namespace);
		definitions.setTargetNamespace(namespace);
		return definitions;
	}

	public Class<?> generateNodeFor() {
		return null;
	}
	
}