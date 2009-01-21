package org.drools.builder;

import java.util.Collection;

import org.drools.definition.KnowledgePackage;
import org.drools.io.Resource;

/**
 * <p>
 * The KnowledgeBuilder is responsible for taking source files, such as a .drl file or an xls file,
 * and turning them into a KnowledgePackage of rule and process definitions which a KnowledgeBase
 * can consume. It uses the ResourceType enum to tell it the type of the resource it is being asked to build.
 * 
 * </p>
 * 
 * <p>
 * The ResourceFactory provides capabilities to load Resources from a number of sources; such as
 * Reader, ClassPath, URL, File, ByteArray. Binaries, such as xls decision tables,
 * should not use a Reader based Resource handler, which is only suitable for text based resources.
 * </p>
 * 
 * <p>
 * Always check the hasErrors() method after an addition, you should not add more resources
 * or get the KnowledgePackages if there are errors; KnowledgePackages() will return empty
 * if there are errors.
 * </p>
 * 
 * <p>
 * Simple example showing how to build a KnowledgeBase from an DRL rule resource.
 * </p>
 * <pre>
 * KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
 * kbuilder.add( ResourceFactory.newUrlResource( "file://myrules.drl" ),
 *                       ResourceType.DRL);
 * assertFalse( kbuilder.hasErrors() );
 * KnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase();
 * </pre>
 * 
 * <p>
 * Simple example showing how to build a KnowledgeBase from an XLS decision table resource.
 * </p>
 * <pre>
 * KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
 * DecisionTableConfiguration dtconf = KnowledgeBuilderFactory.newDecisionTableConfiguration();
 * dtconf.setInputType( DecisionTableInputType.XLS );
 * dtconf.setWorksheetName( "Tables_2" );
 * kbuilder.add( ResourceFactory.newUrlResource( "file://IntegrationExampleTest.xls" ),
 *               ResourceType.DTABLE,
                 dtconf );
 * assertFalse( kbuilder.hasErrors() );
 * KnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase();
 * </pre>
 * 
 * <p>
 * Simple example showing how to build a KnowledgeBase from an DRF flow resource.
 * <p>
 * <pre>
 * KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
 * kbuilder.add( ResourceFactory.newUrlResource( "file://myflow.rf" ),
 *               ResourceType.DRF);
 * assertFalse( kbuilder.hasErrors() );
 * KnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase();
 * </pre>
 * <p>
 * If there are errors a simple toString can print the errors
 * </p>
 * <pre>
 * if ( kbuilder.hasErrors() ) {
 *     log.exception( kbuilder.getErrors().toString() )
 * }
 * </pre>     
 * 
 * <p>
 * The KnowledgeBuilder can also be built from configuration using the xml change-set format and the ResourceType.ChangeSet value. While change-set
 * supports add, remove, modify as elements. KnowledgeBuilder will only process add. If the resource element provided points to a directory, all
 * files found in that directory will be added. Currently the knowledge type is not derived from the .ext extension of the file and must be explicitely
 * set in the XML for the resource. It is expected that all resources in the directory, when the directory is a given resource, are all of the specified
 * type.
 * </p>
 * <pre>
 * &lt;change-set xmlns='http://drools.org/drools-5.0/change-set'
 *             xmlns:xs='http://www.w3.org/2001/XMLSchema-instance'
 *             xs:schemaLocation='http://drools.org/drools-5.0/change-set drools-change-set-5.0.xsd' &gt;
 *  &lt;add&gt;
 *       &lt;resource source='http:org/domain/myrules.drl' type='DRL' /&gt;
 *       &lt;resource source='classpath:data/IntegrationExampleTest.xls' type="DTABLE"&gt;
 *           &lt;decisiontable-conf input-type="XLS" worksheet-name="Tables_2" /&gt;
 *       &lt;/resource&gt;
 *       &lt;resource source='file:org/drools/decisiontable/myflow.drf' type='DRF' /&gt;
 *   &lt;/add&gt;
 * &lt;/change-set&gt;
 * </pre>
 * 
 * <pre>
 * KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
 * kbuilder.add( ResourceFactory.newUrlResource( url ),
 *               ResourceType.ChangeSet );
 * </pre>
 * 
 */
public interface KnowledgeBuilder
    extends
    RuleBuilder,
    ProcessBuilder {

    /**
     * A a resource of the ResourceType from a given URL.
     * 
     * @param url
     * @param type
     */
    void add(Resource resource,
             ResourceType type);

    /**
    * A a resource of the ResourceType from a given URL, using the provided ResourceConfiguration.
    * Currently only only decision tables use this, via the DecisionTableResourceConfiguration class.
    */
    void add(Resource resource,
             ResourceType type,
             ResourceConfiguration configuration);

    /**
     * Returns the built packages.
     * 
     * If the KnowledgeBuilder has errors the Collection will be empty. The hasErrors()
     * method should always be checked first, to make sure you are getting the packages
     * that you wanted built.
     * 
     * @return
     *     The Collection of KnowledgePackages
     */
    Collection<KnowledgePackage> getKnowledgePackages();

    /**
     * If errors occurred during the build process they are added here
     * @return
     */
    boolean hasErrors();

    /**
     * Return errors that occurred during the build process.
     * @return
     */
    KnowledgeBuilderErrors getErrors();

}
