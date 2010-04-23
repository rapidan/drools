package org.drools.guvnor.server.builder;
/*
 * Copyright 2005 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */



import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.jar.JarInputStream;

import junit.framework.TestCase;

import org.drools.lang.descr.PackageDescr;
import org.drools.lang.dsl.DSLTokenizedMappingFile;
import org.drools.rule.Package;
import org.drools.rule.builder.dialect.java.JavaDialectConfiguration;
import org.drools.builder.conf.DefaultPackageNameOption;

public class BRMSPackageBuilderTest extends TestCase {

   // Added this empty test so this class doesn't fail
   public void testEmpty() {

   }

   public void setUp() {
       System.getProperties().remove( "drools.dialect.java.compiler" );
   }

   public void tearDown() {
       System.getProperties().remove( "drools.dialect.java.compiler" );
   }

    // @FIXME rule "abc" is null and the Packge has no namespace
    public void testPartialPackage() throws Exception {

        JarInputStream jis = new JarInputStream( this.getClass().getResourceAsStream( "/billasurf.jar" ) );
        List<JarInputStream> l = new ArrayList<JarInputStream>();
        l.add( jis );

        Properties ps = new Properties();
        ps.setProperty( DefaultPackageNameOption.PROPERTY_NAME, "foo.bar" );
        BRMSPackageBuilder builder = BRMSPackageBuilder.getInstance( l, ps );

        //PackageDescr pc = new PackageDescr("foo.bar");
        //builder.addPackage( pc );

        String header = "import com.billasurf.Person\n import com.billasurf.Board";
        builder.addPackageFromDrl( new StringReader(header) );
        assertFalse(builder.hasErrors());

        JavaDialectConfiguration javaConf = ( JavaDialectConfiguration ) builder.getPackageBuilderConfiguration().getDialectConfiguration( "java" );
        assertEquals(JavaDialectConfiguration.ECLIPSE, javaConf.getCompiler());

        String ruleAtom = "rule foo \n when \n Person() \n then \n System.out.println(42); end";
        builder.addPackageFromDrl( new StringReader(ruleAtom) );
        if (builder.hasErrors()) {
            System.err.println(builder.getErrors().getErrors()[0].getMessage());
        }
        assertFalse(builder.hasErrors());

        ruleAtom = "rule foo2 \n when \n Person() \n then \n System.out.println(42); end";
        builder.addPackageFromDrl( new StringReader(ruleAtom) );
        if (builder.hasErrors()) {
            System.err.println(builder.getErrors().getErrors()[0].getMessage());
        }
        assertFalse(builder.hasErrors());

        assertEquals("foo.bar", builder.getPackage().getName());


        String functionAtom = "function int fooBar(String x) { return 42; }";
        builder.addPackageFromDrl( new StringReader(functionAtom) );
        if (builder.hasErrors()) {
            System.err.println(builder.getErrors().getErrors()[0].getMessage());
        }
        assertFalse(builder.hasErrors());

        Package p = builder.getPackage();
        assertEquals(2, p.getRules().length);
        assertEquals(1, p.getFunctions().size());
        assertNotNull(p.getRule( "foo2" ));

        functionAtom = "xxx";
        builder.addPackageFromDrl( new StringReader(functionAtom) );
        assertTrue(builder.hasErrors());
        builder.clearErrors();
        assertFalse(builder.hasErrors());


    }

    public void testGeneratedBeans() throws Exception {

            JarInputStream jis = new JarInputStream( this.getClass().getResourceAsStream( "/billasurf.jar" ) );
            List<JarInputStream> l = new ArrayList<JarInputStream>();
            l.add( jis );
            BRMSPackageBuilder builder = BRMSPackageBuilder.getInstance( l, new Properties() );

            PackageDescr pc = new PackageDescr("foo.bar");
            builder.addPackage( pc );

            String header = "import com.billasurf.Person\n import com.billasurf.Board\n declare GenBean \n name: String \n end";
            builder.addPackageFromDrl( new StringReader(header) );
            assertFalse(builder.hasErrors());

            JavaDialectConfiguration javaConf = ( JavaDialectConfiguration ) builder.getPackageBuilderConfiguration().getDialectConfiguration( "java" );
            assertEquals(JavaDialectConfiguration.ECLIPSE, javaConf.getCompiler());

            String ruleAtom = "rule foo \n when \n Person() \n GenBean(name=='mike')\n then \n System.out.println(42); end";
            builder.addPackageFromDrl( new StringReader(ruleAtom) );
            if (builder.hasErrors()) {
                System.err.println(builder.getErrors().getErrors()[0].getMessage());
            }
            assertFalse(builder.hasErrors());

            ruleAtom = "rule foo2 \n when \n Person() \n then \n System.out.println(42); end";
            builder.addPackageFromDrl( new StringReader(ruleAtom) );
            if (builder.hasErrors()) {
                System.err.println(builder.getErrors().getErrors()[0].getMessage());
            }
            assertFalse(builder.hasErrors());

            assertEquals("foo.bar", builder.getPackage().getName());


    }

    public void testHasDSL() {
        BRMSPackageBuilder builder = new BRMSPackageBuilder();
        assertFalse(builder.hasDSL());
    }

    public void testGetExpander() {
        BRMSPackageBuilder builder = new BRMSPackageBuilder();
        List<DSLTokenizedMappingFile> files = new ArrayList<DSLTokenizedMappingFile>();
        files.add( new DSLTokenizedMappingFile() );
        builder.setDSLFiles( files );
        assertTrue(builder.hasDSL());
        assertNotNull(builder.getDSLExpander());
    }


    public void testDefaultCompiler() throws Exception {

        JarInputStream jis = new JarInputStream( this.getClass().getResourceAsStream( "/billasurf.jar" ) );
        List<JarInputStream> l = new ArrayList<JarInputStream>();
        l.add( jis );
        Properties p = new Properties();
        p.setProperty("drools.accumulate.function.groupCount", "org.drools.base.accumulators.MaxAccumulateFunction");
        BRMSPackageBuilder builder = BRMSPackageBuilder.getInstance( l, p );
        assertEquals("org.drools.base.accumulators.MaxAccumulateFunction", builder.getPackageBuilderConfiguration().getAccumulateFunction("groupCount").getClass().getName());

        PackageDescr pc = new PackageDescr("foo.bar");
        builder.addPackage( pc );

        String header = "import com.billasurf.Person\n import com.billasurf.Board";
        builder.addPackageFromDrl( new StringReader(header) );
        assertFalse(builder.hasErrors());

        JavaDialectConfiguration javaConf = ( JavaDialectConfiguration ) builder.getPackageBuilderConfiguration().getDialectConfiguration( "java" );
        assertEquals(JavaDialectConfiguration.ECLIPSE, javaConf.getCompiler());
    }

    public void testEclipseCompiler() throws Exception {

        System.setProperty( "drools.dialect.java.compiler", "ECLIPSE" );
        JarInputStream jis = new JarInputStream( this.getClass().getResourceAsStream( "/billasurf.jar" ) );
        List<JarInputStream> l = new ArrayList<JarInputStream>();
        l.add( jis );
        BRMSPackageBuilder builder = BRMSPackageBuilder.getInstance( l, new Properties() );

        PackageDescr pc = new PackageDescr("foo.bar");
        builder.addPackage( pc );

        String header = "import com.billasurf.Person\n import com.billasurf.Board";
        builder.addPackageFromDrl( new StringReader(header) );
        assertFalse(builder.hasErrors());

        JavaDialectConfiguration javaConf = ( JavaDialectConfiguration ) builder.getPackageBuilderConfiguration().getDialectConfiguration( "java" );
        assertEquals(JavaDialectConfiguration.ECLIPSE, javaConf.getCompiler());
    }

    public void testNamespaceSingle() throws Exception {

        System.setProperty( "drools.dialect.java.compiler", "ECLIPSE" );
        JarInputStream jis = new JarInputStream( this.getClass().getResourceAsStream( "/billasurf.jar" ) );
        List<JarInputStream> l = new ArrayList<JarInputStream>();
        l.add( jis );
        BRMSPackageBuilder builder = BRMSPackageBuilder.getInstance( l, new Properties() );

        assertFalse(builder.getPackageBuilderConfiguration().isAllowMultipleNamespaces());
    }

    public void testRuleFlow() throws Exception {
        BRMSPackageBuilder builder = BRMSPackageBuilder.getInstance( new ArrayList<JarInputStream>(), new Properties() );
        builder.addProcessFromXml( new InputStreamReader( this.getClass().getResourceAsStream( "evaluation.rf" ) ) );
        assertFalse(builder.hasErrors());
    }

    public void testBPMN2Process() throws Exception {
        BRMSPackageBuilder builder = BRMSPackageBuilder.getInstance( new ArrayList<JarInputStream>(), new Properties() );
        builder.addProcessFromXml( new InputStreamReader( this.getClass().getResourceAsStream( "Hello.bpmn" ) ) );
        assertFalse(builder.hasErrors());
    }

}