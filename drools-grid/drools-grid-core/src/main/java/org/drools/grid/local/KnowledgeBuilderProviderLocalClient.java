/**
 * 
 */
package org.drools.grid.local;

import java.util.Properties;

import org.drools.KnowledgeBase;
import org.drools.builder.DecisionTableConfiguration;
import org.drools.builder.JaxbConfiguration;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderConfiguration;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.KnowledgeBuilderFactoryService;
import org.drools.builder.conf.impl.JaxbConfigurationImpl;

import com.sun.tools.xjc.Options;

public class KnowledgeBuilderProviderLocalClient
    implements
    KnowledgeBuilderFactoryService {

    public DecisionTableConfiguration newDecisionTableConfiguration() {
        return KnowledgeBuilderFactory.newDecisionTableConfiguration();
    }

    public KnowledgeBuilder newKnowledgeBuilder() {
        return KnowledgeBuilderFactory.newKnowledgeBuilder();
    }

    public KnowledgeBuilder newKnowledgeBuilder(KnowledgeBuilderConfiguration conf) {
        return KnowledgeBuilderFactory.newKnowledgeBuilder( conf );
    }

    public KnowledgeBuilder newKnowledgeBuilder(KnowledgeBase kbase) {
        return KnowledgeBuilderFactory.newKnowledgeBuilder( kbase );
    }

    public KnowledgeBuilder newKnowledgeBuilder(KnowledgeBase kbase,
                                                KnowledgeBuilderConfiguration conf) {
        return KnowledgeBuilderFactory.newKnowledgeBuilder( kbase,
                                                            conf );
    }

    public KnowledgeBuilderConfiguration newKnowledgeBuilderConfiguration() {
        return KnowledgeBuilderFactory.newKnowledgeBuilderConfiguration();
    }

    public KnowledgeBuilderConfiguration newKnowledgeBuilderConfiguration(Properties properties,
                                                                          ClassLoader classLoader) {
        return KnowledgeBuilderFactory.newKnowledgeBuilderConfiguration( properties,
                                                                         classLoader );
    }
    
    public JaxbConfiguration newJaxbConfiguration(Options xjcOpts,
                                                  String systemId) {
        return KnowledgeBuilderFactory.newJaxbConfiguration( xjcOpts, systemId );
    }    

}