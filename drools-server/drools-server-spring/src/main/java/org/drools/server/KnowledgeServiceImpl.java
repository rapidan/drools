package org.drools.server;

import java.io.StringReader;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.drools.server.profile.KnowledgeServiceConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

/**
 * Service to execute commands
 * @author Lucas Amador
 *
 */
public class KnowledgeServiceImpl
    implements
    KnowledgeService {

    private static final Logger                        LOG = LoggerFactory.getLogger( KnowledgeServiceImpl.class );

    private CamelContext                               camelContext;
    private ProducerTemplate                           template;
    private Map<String, KnowledgeServiceConfiguration> configurations;
    private String                                     smId;

    public KnowledgeServiceImpl(CamelContext camelContext,
                                Map<String, KnowledgeServiceConfiguration> configs,
                                String smId) throws Exception {
        this.camelContext = camelContext;
        this.configurations = configs;
        this.smId = smId;
        this.template = camelContext.createProducerTemplate();
        for ( String key : configurations.keySet() ) {
            createCamelRoutes( configurations.get( key ) );
        }
        this.camelContext.start();
        for ( String key : configurations.keySet() ) {
            executeCommands( configurations.get( key ).getCommands() );
        }
    }

    private void executeCommands(List<String> commands) throws RuntimeException {
        for ( String command : commands ) {
            executeCommand( command );
        }
    }

    public String executeCommand(String cmd) throws RuntimeException {
        String lookup = getLookup( cmd );
        if ( lookup == null || lookup.length() == 0 ) {
            LOG.error( "Unable to get command lookup attribute: " + lookup );
            throw new RuntimeException( "Unable to get command lookup attribute: " + lookup );
        }
        KnowledgeServiceConfiguration serviceConfiguration = configurations.get( lookup );
        if ( serviceConfiguration == null ) {
            LOG.error( "Unable to lookup session: " + lookup );
            throw new RuntimeException( "Unable to lookup session: " + lookup );
        }
        if ( "JAXB".equals( serviceConfiguration.getMarshaller() ) ) {
            JAXBContext jaxbContext = serviceConfiguration.getContext();
            String endpointUri = "direct:with-session-jaxb-" + lookup;
			return new String( (byte[]) template.requestBodyAndHeader( endpointUri,
                                                                       cmd,
                                                                       "jaxb-context",
                                                                       jaxbContext ) );
        } else if ( "XSTREAM".equals( serviceConfiguration.getMarshaller() ) ) {
            String endpointUri = "direct:with-session-xstream-" + lookup;
			return new String( (byte[]) template.requestBody( endpointUri,
                                                              cmd ) );
        }
        return null;
    }

    private void createCamelRoutes(KnowledgeServiceConfiguration configuration) throws Exception {
        RouteBuilder rb = new RouteBuilder() {
            public void configure() throws Exception {
            }
        };
        if ( "JAXB".equals( configuration.getMarshaller() ) ) {
            String uri = "direct:with-session-jaxb-" + configuration.getSessionId();
			rb.from( uri ).to( "drools:" + smId + "/" + configuration.getSessionId() + "?dataFormat=drools-jaxb" );
        } else if ( "XSTREAM".equals( configuration.getMarshaller() ) ) {
        	String uri = "direct:with-session-xstream-" + configuration.getSessionId();
            rb.from( uri ).to( "drools:" + smId + "/" + configuration.getSessionId() + "?dataFormat=drools-xstream" );
        } else {
            throw new IllegalArgumentException( "Invalid marshaller value on camel routes creation: " + configuration.getMarshaller() );
        }
        camelContext.addRoutes( rb );
    }

    private String getLookup(String xml) throws RuntimeException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        InputSource source = new InputSource( new StringReader( xml ) );
        Document d = null;
        try {
            d = factory.newDocumentBuilder().parse( source );
        } catch ( Exception e ) {
            throw new RuntimeException( "Error getting lookup: " + e.getMessage(),
                                              e );
        }
        return d.getDocumentElement().getAttribute( "lookup" );
    }

    public void setCamelContext(CamelContext camelContext) {
        this.camelContext = camelContext;
    }

    public CamelContext getCamelContext() {
        return camelContext;
    }

    public void setTemplate(ProducerTemplate template) {
        this.template = template;
    }

    public ProducerTemplate getTemplate() {
        return template;
    }

    public void setSmId(String smId) {
        this.smId = smId;
    }

    public String getSmId() {
        return smId;
    }

}
