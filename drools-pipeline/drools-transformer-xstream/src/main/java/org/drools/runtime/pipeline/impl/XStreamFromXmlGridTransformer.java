/**
 * 
 */
package org.drools.runtime.pipeline.impl;

import org.drools.builder.DirectoryLookupFactoryService;
import org.drools.grid.ExecutionNode;
import org.drools.impl.StatefulKnowledgeSessionImpl;
import org.drools.impl.StatelessKnowledgeSessionImpl;
import org.drools.reteoo.ReteooRuleBase;
import org.drools.runtime.CommandExecutor;
import org.drools.runtime.pipeline.PipelineContext;
import org.drools.runtime.pipeline.Transformer;
import org.w3c.dom.Document;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomReader;

public class XStreamFromXmlGridTransformer extends BaseEmitter
    implements
    Transformer {

    private XStreamResolverStrategy strategy;
    private Object payload;
    private PipelineContext context;
    
    public XStreamFromXmlGridTransformer(XStreamResolverStrategy strategy) {
        this.strategy = strategy;
    }

    public void receive(Object object,
                        PipelineContext context) {            
        this.payload = processPayload(object, context);
        emit( this.payload,
              context );
    }

    public Object processPayload(Object object, PipelineContext context) {
        
        try {
            Document d = (Document) object;
            String name = d.getDocumentElement().getAttribute("lookup");
            XStream xstream = this.strategy.lookup(name);
            if (xstream == null) {
                throw new IllegalArgumentException("Unable to lookup XStream parser using name '" + name + "'");
            }
            ExecutionNodePipelineContextImpl executionNodeContext = (ExecutionNodePipelineContextImpl) context;
            ExecutionNode node = executionNodeContext.getExecutionNode();
            CommandExecutor executor = node.get(DirectoryLookupFactoryService.class).lookup(name);
            if (executor == null) {
                throw new IllegalArgumentException("Unable to lookup CommandExecutor using name '" + name + "'");
            }
            executionNodeContext.setCommandExecutor(executor);
            ClassLoader cl = null;
            if (executor instanceof StatefulKnowledgeSessionImpl) {
                cl = ((ReteooRuleBase) (( StatefulKnowledgeSessionImpl ) executor).getRuleBase()).getRootClassLoader();
                xstream.setClassLoader(cl);
            } else if (executor instanceof StatelessKnowledgeSessionImpl) {
                cl = ((ReteooRuleBase) (( StatelessKnowledgeSessionImpl ) executor).getRuleBase()).getRootClassLoader();
            } else {
                throw new IllegalArgumentException("Unable to set ClassLoader on " + executor);
            }
            xstream.setClassLoader(cl);
            executionNodeContext.setClassLoader(cl);
            payload = xstream.unmarshal(new DomReader(d));
            context.getProperties().put("xstream-instance", xstream);
            this.context = context;
        } catch (Exception e) {
            handleException(this, object, e);
        }
        return payload;
    }

    public Object getPayload() {
        return payload;
    }

    public void setPayload(Object payload) {
        this.payload = payload;
    }

    public PipelineContext getContext() {
        return context;
    }

    public void setContext(PipelineContext context) {
        this.context = context;
    }
    
}