package org.drools.guvnor.server.contenthandler;

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

import junit.framework.TestCase;

public class DRLFileContentHandlerTest extends TestCase {

    public void testSniffDRLType() throws Exception {

        DRLFileContentHandler h = new DRLFileContentHandler();

        // in this case we have package, and N rules
        String classic = "package foobar \n rule boo \n when \n then\n end \n rule boo2 \n when \n then\n end";

        // in this case we just have rules
        String moreRuleClassic = "\nrule bar \n when \n then \n end\nrule x \n when \n then \n end ";

        // in this case we just have a single rule
        String newRule = "agenda-group 'x' \n when \n then \n";

        String moreSingle = "rule foo when then end";

        String query = "query FooBar\nend";

        String moreNewRule = "agenda-group 'x' \n when end.bar \n then rule.end.bar";

        String emptyRule = "";

        String complex = "#some comments about the rule \n#here\n when \n #goo \n foo.bar \n then \n #goo \n end.bar";

        assertTrue( h.isStandAloneRule( newRule ) );
        assertFalse( h.isStandAloneRule( moreRuleClassic ) );
        assertFalse( h.isStandAloneRule( classic ) );
        assertFalse( h.isStandAloneRule( moreSingle ) );
        assertFalse( h.isStandAloneRule( null ) );
        assertFalse( h.isStandAloneRule( emptyRule ) );
        assertTrue( h.isStandAloneRule( moreNewRule ) );
        assertTrue( h.isStandAloneRule( complex ) );
        assertFalse( h.isStandAloneRule( query ) );

    }

    public void testRuleWithDialect() {
        String rule = "rule \"DemoRule\" \n " + "    salience 10 \n" + "    dialect \"mvel\" \n " + " when \n" + " Driver( age > 65 ) \n" + " then \n" + " insert(new Rejection(\" too old \"));" + "end ";
        DRLFileContentHandler h = new DRLFileContentHandler();
        assertFalse( h.isStandAloneRule( rule ) );

        assertFalse( h.isStandAloneRule( "" ) );

    }

    public void testStandAlone() throws Exception {
        String rule = "when \nFoo()\nthen\n\tbar()";
        DRLFileContentHandler h = new DRLFileContentHandler();
        assertTrue( h.isStandAloneRule( rule ) );

        String r = h.wrapRuleDeclaration( "whee",
                                          "",
                                          rule );
        assertTrue( r.indexOf( "rule 'whee'" ) > -1 );
        assertTrue( r.indexOf( "dialect 'mvel'" ) > -1 );

        rule = "dialect 'java'\nwhen \nFoo()\nthen\n\tbar()";
        r = h.wrapRuleDeclaration( "whee",
                                   null,
                                   rule );
        assertTrue( r.indexOf( "rule 'whee'" ) > -1 );
        assertEquals( -1,
                      r.indexOf( "dialect 'mvel'" ) );
        assertTrue( r.indexOf( "dialect 'java'" ) > -1 );

    }

    /**
     * Tests a rule that inherits another.
     * @throws Exception 
     */
    public void testStandAloneWithExtends() throws Exception {
        String rule = "when \nFoo()\nthen\n\tbar()";
        DRLFileContentHandler h = new DRLFileContentHandler();
        assertTrue( h.isStandAloneRule( rule ) );

        String r = h.wrapRuleDeclaration( "whee",
                                          "parentRule",
                                          rule );
        assertTrue( r.indexOf( "extends parentRule" ) > -1 );

        rule = "dialect 'java'\nwhen \nFoo()\nthen\n\tbar()";
        r = h.wrapRuleDeclaration( "whee",
                                   "",
                                   rule );
        assertTrue( r.indexOf( "extends" ) == -1 );

    }

    public void testRuleWithRuleFlowGroup() {
        String rule = "rule \"DemoRule\" \n " + "    ruleflow-group \"name-of-ruleflow\"  \n" + "    dialect \"mvel\" \n " + " when \n" + " Driver( age > 65 ) \n" + " then \n" + " insert(new Rejection(\" too old \"));" + "end ";
        DRLFileContentHandler h = new DRLFileContentHandler();
        assertFalse( h.isStandAloneRule( rule ) );

        rule = "    ruleflow-group \"name-of-ruleflow\"  \n" + "    dialect \"mvel\" \n " + " when \n" + " Driver( age > 65 ) \n" + " then \n" + " insert(new Rejection(\" too old \"));";
        assertTrue( h.isStandAloneRule( rule ) );
    }

}