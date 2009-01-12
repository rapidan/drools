package org.drools.runtime.pipeline;

import java.util.List;

import javax.xml.bind.Unmarshaller;

import net.sf.jxls.reader.XLSReader;

import org.drools.ProviderInitializationException;
import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.StatelessKnowledgeSession;
import org.milyn.Smooks;

import com.thoughtworks.xstream.XStream;

public class PipelineFactory {

    private static CorePipelineProvider       corePipelineProvider;

    private static JaxbTransformerProvider    jaxbPipelineProvider;

    private static SmooksTransformerProvider  smooksPipelineProvider;

    private static XStreamTransformerProvider xstreamPipelineProvider;

    private static JxlsTransformerProvider    jxlsPipelineProvider;

    public static Pipeline newStatefulKnowledgeSessionPipeline(StatefulKnowledgeSession ksession) {
        return getCorePipelineProvider().newStatefulKnowledgeSessionPipeline( ksession );
    }

    public static Pipeline newStatefulKnowledgeSessionPipeline(StatefulKnowledgeSession ksession,
                                                               String entryPointName) {
        return getCorePipelineProvider().newStatefulKnowledgeSessionPipeline( ksession,
                                                                              entryPointName );
    }

    public static Pipeline newStatelessKnowledgeSessionPipeline(StatelessKnowledgeSession ksession) {
        return getCorePipelineProvider().newStatelessKnowledgeSessionPipelineImpl( ksession );
    }

    public static KnowledgeRuntimeCommand newStatefulKnowledgeSessionInsert() {
        return getCorePipelineProvider().newStatefulKnowledgeSessionInsert();
    }

    public static KnowledgeRuntimeCommand newStatelessKnowledgeSessionExecute() {
        return getCorePipelineProvider().newStatelessKnowledgeSessionExecute();
    }

    public static KnowledgeRuntimeCommand newStatefulKnowledgeSessionGetGlobal() {
        return getCorePipelineProvider().newStatefulKnowledgeSessionGetGlobal();
    }

    public static KnowledgeRuntimeCommand newStatefulKnowledgeSessionSetGlobal() {
        return getCorePipelineProvider().newStatefulKnowledgeSessionSetGlobal();
    }

    KnowledgeRuntimeCommand newStatefulKnowledgeSessionSetGlobal(String identifier) {
        return getCorePipelineProvider().newStatefulKnowledgeSessionSetGlobal( identifier );
    }

    public static KnowledgeRuntimeCommand newStatefulKnowledgeSessionSignalEvent(String eventType) {
        return getCorePipelineProvider().newStatefulKnowledgeSessionSignalEvent( eventType );
    }

    public static KnowledgeRuntimeCommand newStatefulKnowledgeSessionSignalEvent(String eventType,
                                                                                 long id) {
        return getCorePipelineProvider().newStatefulKnowledgeSessionSignalEvent( eventType,
                                                                                 id );
    }

    public static KnowledgeRuntimeCommand newStatefulKnowledgeSessionStartProcess(String eventType) {
        return getCorePipelineProvider().newStatefulKnowledgeSessionStartProcess( eventType );
    }
    
    public static Action newAssignObjectAsResult() {
        return getCorePipelineProvider().newAssignObjectAsResult();
    }
    
    public static Action newExecuteResultHandler() {
        return getCorePipelineProvider().newExecuteResultHandler();
    }
    
    public static Action newMvelAction(String action) {
        return getCorePipelineProvider().newMvelAction( action );
    }    

    public static Expression newMvelExpression(String expression) {
        return getCorePipelineProvider().newMvelExpression( expression );
    }

    public static Splitter newIterateSplitter() {
        return getCorePipelineProvider().newIterateSplitter();
    }    
    
    public static Join newListCollectJoin() {
        return getCorePipelineProvider().newListCollectJoin();
    }

    public static Transformer newSmooksTransformer(Smooks smooks,
                                                   String rootId) {
        return getSmooksPipelineProvider().newSmooksTransformer( smooks,
                                                                 rootId );
    }

    public static Transformer newJaxbTransformer(Unmarshaller unmarshaller) {
        return getJaxbPipelineProvider().newJaxbTransformer( unmarshaller );
    }

    public static Transformer newXStreamTransformer(XStream xstream) {
        return getXStreamTransformerProvider().newXStreamTransformer( xstream );
    }

    public static Transformer newJxlsTransformer(XLSReader xlsReader,
                                                 String text) {
        return getJxlsTransformerProvider().newJxlsTransformer( xlsReader,
                                                                text );
    }

    private static synchronized void setCorePipelineProvider(CorePipelineProvider provider) {
        PipelineFactory.corePipelineProvider = provider;
    }

    private static synchronized CorePipelineProvider getCorePipelineProvider() {
        if ( corePipelineProvider == null ) {
            loadCorePipelineProvider();
        }
        return corePipelineProvider;
    }

    private static void loadCorePipelineProvider() {
        try {
            Class<CorePipelineProvider> cls = (Class<CorePipelineProvider>) Class.forName( "org.drools.runtime.pipeline.impl.CorePipelineProviderImpl" );
            setCorePipelineProvider( cls.newInstance() );
        } catch ( Exception e2 ) {
            throw new ProviderInitializationException( "org.drools.runtime.pipeline.impl.CorePipelineProviderImpl could not be set.",
                                                       e2 );
        }
    }

    private static synchronized void setJaxbTransformerProvider(JaxbTransformerProvider provider) {
        PipelineFactory.jaxbPipelineProvider = provider;
    }

    private static synchronized JaxbTransformerProvider getJaxbPipelineProvider() {
        if ( jaxbPipelineProvider == null ) {
            loadJaxbTransformerProvider();
        }
        return jaxbPipelineProvider;
    }

    private static void loadJaxbTransformerProvider() {
        try {
            Class<JaxbTransformerProvider> cls = (Class<JaxbTransformerProvider>) Class.forName( "org.drools.runtime.pipeline.impl.JaxbTransformer$JaxbTransformerProviderImpl" );
            setJaxbTransformerProvider( cls.newInstance() );
        } catch ( Exception e2 ) {
            throw new ProviderInitializationException( "Provider org.drools.runtime.pipeline.impl.JaxbTransformer$JaxbTransformerProviderImpl could not be set.",
                                                       e2 );
        }
    }

    private static synchronized void setSmooksTransformerProvider(SmooksTransformerProvider provider) {
        PipelineFactory.smooksPipelineProvider = provider;
    }

    private static synchronized SmooksTransformerProvider getSmooksPipelineProvider() {
        if ( smooksPipelineProvider == null ) {
            loadSmooksTransformerProvider();
        }
        return smooksPipelineProvider;
    }

    private static void loadSmooksTransformerProvider() {
        try {
            Class<SmooksTransformerProvider> cls = (Class<SmooksTransformerProvider>) Class.forName( "org.drools.runtime.pipeline.impl.SmooksTransformer$SmooksTransformerProviderImpl" );
            setSmooksTransformerProvider( cls.newInstance() );
        } catch ( Exception e2 ) {
            throw new ProviderInitializationException( "Provider org.drools.runtime.pipeline.impl.SmooksTransformer$SmooksTransformerProviderImpl could not be set.",
                                                       e2 );
        }
    }

    private static synchronized void setXStreamTransformerProvider(XStreamTransformerProvider provider) {
        PipelineFactory.xstreamPipelineProvider = provider;
    }

    private static synchronized XStreamTransformerProvider getXStreamTransformerProvider() {
        if ( xstreamPipelineProvider == null ) {
            loadXStreamTransformerProvider();
        }
        return xstreamPipelineProvider;
    }

    private static void loadXStreamTransformerProvider() {
        try {
            Class<XStreamTransformerProvider> cls = (Class<XStreamTransformerProvider>) Class.forName( "org.drools.runtime.pipeline.impl.XStreamTransformer$XStreamTransformerProviderImpl" );
            setXStreamTransformerProvider( cls.newInstance() );
        } catch ( Exception e2 ) {
            throw new ProviderInitializationException( "Provider org.drools.runtime.pipeline.impl.XStreamTransformer$XStreamTransformerProviderImpl could not be set.",
                                                       e2 );
        }
    }

    private static synchronized void setJxlsTransformerProvider(JxlsTransformerProvider provider) {
        PipelineFactory.jxlsPipelineProvider = provider;
    }

    private static synchronized JxlsTransformerProvider getJxlsTransformerProvider() {
        if ( jxlsPipelineProvider == null ) {
            loadJxlsTransformerProvider();
        }
        return jxlsPipelineProvider;
    }

    private static void loadJxlsTransformerProvider() {
        try {
            Class<JxlsTransformerProvider> cls = (Class<JxlsTransformerProvider>) Class.forName( "org.drools.runtime.pipeline.impl.JxlsTransformer$JxlsTransformerProviderImpl" );
            setJxlsTransformerProvider( cls.newInstance() );
        } catch ( Exception e2 ) {
            throw new ProviderInitializationException( "Provider org.drools.runtime.pipeline.impl.JxlsTransformer$JxlsTransformerProviderImpl could not be set.",
                                                       e2 );
        }
    }

}
