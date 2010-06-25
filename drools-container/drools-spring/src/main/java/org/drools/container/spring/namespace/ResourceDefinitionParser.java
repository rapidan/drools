package org.drools.container.spring.namespace;

import java.util.List;

import org.drools.builder.DecisionTableInputType;
import org.drools.builder.JaxbConfiguration;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;
import org.drools.builder.conf.impl.DecisionTableConfigurationImpl;
import org.drools.container.spring.beans.DroolsResourceAdapter;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.sun.tools.xjc.Language;
import com.sun.tools.xjc.Options;

public class ResourceDefinitionParser extends AbstractBeanDefinitionParser {

    private static final String WORKSHEET_NAME_ATTRIBUTE = "worksheet-name";
    private static final String INPUT_TYPE_ATTRIBUTE     = "input-type";
    private static final String TYPE_ATTRIBUTE           = "type";
    private static final String SOURCE_ATTRIBUTE         = "source";
    private static final String SYSTEM_ID                = "system-id";
    private static final String SCHEMA_LANGUAGE          = "schema-language";

    @SuppressWarnings("unchecked")
    @Override
    protected AbstractBeanDefinition parseInternal(Element element,
                                                   ParserContext parserContext) {
        BeanDefinitionBuilder factory = BeanDefinitionBuilder.rootBeanDefinition( DroolsResourceAdapter.class );

        String source = element.getAttribute( SOURCE_ATTRIBUTE );
        emptyAttributeCheck( element.getLocalName(),
                             SOURCE_ATTRIBUTE,
                             source );
        factory.addPropertyValue( "resource",
                                  source );

        String type = element.getAttribute( TYPE_ATTRIBUTE );

        String resourceType = type == null || type.length() == 0 ? ResourceType.DRL.getName() : type;

        factory.addPropertyValue( "resourceType",
                                  resourceType );

        if ( "xsd".equals( resourceType.toLowerCase() ) ) {
            List<Element> childElements = DomUtils.getChildElementsByTagName( element,
                                                                              "jaxb-conf" );
            if ( !childElements.isEmpty() ) {
                Element conf = childElements.get( 0 );
                
                String systemId = conf.getAttribute( SYSTEM_ID );
                systemId = ( systemId != null && systemId.trim().length() > 0) ? systemId : "xsd";
                
                String schemaLanguage = conf.getAttribute( SCHEMA_LANGUAGE );
                schemaLanguage = ( schemaLanguage != null && schemaLanguage.trim().length() > 0) ? schemaLanguage : "XMLSCHEMA";
                
                Options options = new Options();
                options.setSchemaLanguage( Language.valueOf( schemaLanguage ) );
                

                JaxbConfiguration jaxbConf = KnowledgeBuilderFactory.newJaxbConfiguration( new Options(),
                                                                                           systemId );

                factory.addPropertyValue( "resourceConfiguration",
                                          jaxbConf );
            } else {
                JaxbConfiguration jaxbConf = KnowledgeBuilderFactory.newJaxbConfiguration( new Options(),
                                                                                           "xsd" );

                factory.addPropertyValue( "resourceConfiguration",
                                          jaxbConf );                
            }
        } else if ( "dtable".equals( resourceType.toLowerCase() ) ) {
            List<Element> childElements = DomUtils.getChildElementsByTagName( element,
                                                                              "decisiontable-conf" );
            if ( !childElements.isEmpty() ) {
                Element conf = childElements.get( 0 );
                DecisionTableConfigurationImpl dtableConf = new DecisionTableConfigurationImpl();

                String inputType = conf.getAttribute( INPUT_TYPE_ATTRIBUTE );
                emptyAttributeCheck( conf.getLocalName(),
                                     INPUT_TYPE_ATTRIBUTE,
                                     inputType );
                dtableConf.setInputType( DecisionTableInputType.valueOf( inputType ) );

                String worksheetName = conf.getAttribute( WORKSHEET_NAME_ATTRIBUTE );
                emptyAttributeCheck( conf.getLocalName(),
                                     WORKSHEET_NAME_ATTRIBUTE,
                                     worksheetName );
                dtableConf.setWorksheetName( worksheetName );

                factory.addPropertyValue( "resourceConfiguration",
                                          dtableConf );
            }
        }

        return factory.getBeanDefinition();
    }

    public void emptyAttributeCheck(final String element,
                                    final String attributeName,
                                    final String attribute) {
        if ( attribute == null || attribute.trim().equals( "" ) ) {
            throw new IllegalArgumentException( "<" + element + "> requires a '" + attributeName + "' attribute" );
        }
    }
}
