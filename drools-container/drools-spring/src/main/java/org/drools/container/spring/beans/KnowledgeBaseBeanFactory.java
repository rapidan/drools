package org.drools.container.spring.beans;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseFactoryService;
import org.drools.builder.JaxbConfiguration;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderErrors;
import org.drools.builder.KnowledgeBuilderFactoryService;
import org.drools.builder.ResourceType;
import org.drools.builder.help.KnowledgeBuilderHelper;
import org.drools.grid.ExecutionNode;
import org.drools.grid.local.LocalConnection;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

import com.sun.tools.xjc.Language;
import com.sun.tools.xjc.Options;

public class KnowledgeBaseBeanFactory
    implements
    FactoryBean,
    InitializingBean {

    private KnowledgeBase               kbase;
    private ExecutionNode               node;
    private List<DroolsResourceAdapter> resources = Collections.emptyList();

    public Object getObject() throws Exception {
        return kbase;
    }

    public Class< ? extends KnowledgeBase> getObjectType() {
        return KnowledgeBase.class;
    }

    public boolean isSingleton() {
        return true;
    }

    public void afterPropertiesSet() throws Exception {
        if ( node == null ) {
            node = new LocalConnection().getExecutionNode();
        }
        KnowledgeBuilder kbuilder = node.get( KnowledgeBuilderFactoryService.class ).newKnowledgeBuilder();
        kbase = node.get( KnowledgeBaseFactoryService.class ).newKnowledgeBase();
 
        List<DroolsResourceAdapter> xsds = new ArrayList<DroolsResourceAdapter>();
        
        for ( DroolsResourceAdapter res : resources ) {            
            
            if ( res.getResourceConfiguration() == null ) {
                kbuilder.add( res.getDroolsResource(),
                              res.getResourceType() );
            } else {
                kbuilder.add( res.getDroolsResource(),
                              res.getResourceType(),
                              res.getResourceConfiguration() );
            }
        }

        KnowledgeBuilderErrors errors = kbuilder.getErrors();
        if ( !errors.isEmpty() ) {
            throw new RuntimeException( errors.toString() );
        }

        kbase.addKnowledgePackages( kbuilder.getKnowledgePackages() );

    }

    public KnowledgeBase getKbase() {
        return kbase;
    }

    public void setKbase(KnowledgeBase kbase) {
        this.kbase = kbase;
    }

    public ExecutionNode getNode() {
        return node;
    }

    public List<DroolsResourceAdapter> getResources() {
        return resources;
    }

    public void setResources(List<DroolsResourceAdapter> resources) {
        this.resources = resources;
    }

    public void setNode(ExecutionNode executionNode) {
        this.node = executionNode;
    }
}
