package org.drools.testframework;

import static org.mvel2.MVEL.eval;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.drools.FactHandle;
import org.drools.RuleBase;
import org.drools.base.ClassTypeResolver;
import org.drools.base.TypeResolver;
import org.drools.common.InternalRuleBase;
import org.drools.common.InternalWorkingMemory;
import org.drools.ide.common.client.modeldriven.testing.ActivateRuleFlowGroup;
import org.drools.ide.common.client.modeldriven.testing.ExecutionTrace;
import org.drools.ide.common.client.modeldriven.testing.Expectation;
import org.drools.ide.common.client.modeldriven.testing.FactData;
import org.drools.ide.common.client.modeldriven.testing.FieldData;
import org.drools.ide.common.client.modeldriven.testing.Fixture;
import org.drools.ide.common.client.modeldriven.testing.RetractFact;
import org.drools.ide.common.client.modeldriven.testing.Scenario;
import org.drools.ide.common.client.modeldriven.testing.VerifyFact;
import org.drools.ide.common.client.modeldriven.testing.VerifyField;
import org.drools.ide.common.client.modeldriven.testing.VerifyRuleFired;
import org.drools.ide.common.server.util.ScenarioXMLPersistence;
import org.drools.rule.Package;
import org.drools.rule.TimeMachine;
import org.mvel2.MVEL;

/**
 * This actually runs the test scenarios.
 *
 * @author Michael Neale
 *
 */
public class ScenarioRunner {

    final Scenario                scenario;
    final Map<String, Object>     populatedData = new HashMap<String, Object>();
    final Map<String, Object>     globalData    = new HashMap<String, Object>();
    final Map<String, FactHandle> factHandles   = new HashMap<String, FactHandle>();

    final InternalWorkingMemory   workingMemory;

    /**
     * This constructor is normally used by Guvnor for running tests on a users request.
     * @param scenario
     *            The scenario to run.
     * @param resolver
     *            A populated type resolved to be used to resolve the types in
     *            the scenario.
     *
     * For info on how to invoke this, see
     * ContentPackageAssemblerTest.testPackageWithRuleflow in drools-guvnor This
     * requires that the classloader for the thread context be set
     * appropriately. The PackageBuilder can provide a suitable TypeResolver for
     * a given package header, and the Package config can provide a classloader.
     *
     */
    public ScenarioRunner(final Scenario scenario,
                          final TypeResolver resolver,
                          final InternalWorkingMemory wm) throws ClassNotFoundException {
        this.scenario = scenario;
        this.workingMemory = wm;
        runScenario( scenario,
                     resolver );
    }

    /**
     * Use this constructor if you have a scenario in a file, for instance.
     * @throws ClassNotFoundException
     */
    public ScenarioRunner(String xml,
                          RuleBase rb) throws ClassNotFoundException {
        this.scenario = ScenarioXMLPersistence.getInstance().unmarshal( xml );
        this.workingMemory = (InternalWorkingMemory) rb.newStatefulSession();
        Package pk = rb.getPackages()[0];
        ClassLoader cl = ((InternalRuleBase) rb).getRootClassLoader();
        HashSet<String> imports = new HashSet<String>();
        imports.add( pk.getName() + ".*" );
        imports.addAll( pk.getImports().keySet() );
        TypeResolver resolver = new ClassTypeResolver( imports,
                                                       cl );
        runScenario( scenario,
                     resolver );
    }

    interface Populate {
        public void go();
    }

    private void runScenario(final Scenario scenario,
                             final TypeResolver resolver) throws ClassNotFoundException {
        MVEL.COMPILER_OPT_ALLOW_NAKED_METH_CALL = true;
        scenario.lastRunResult = new Date();
        //stub out any rules we don't want to have the consequences firing of.
        HashSet<String> ruleList = new HashSet<String>();
        ruleList.addAll( scenario.rules );

        TestingEventListener listener = null;

        List<Populate> toPopulate = new ArrayList<Populate>();

        for ( final FactData fact : scenario.globals ) {
            final Object factObject = eval( "new " + getTypeName( resolver,
                                                                  fact ) + "()" );
            toPopulate.add( new Populate() {
                public void go() {
                    populateFields( fact,
                                    globalData,
                                    factObject );
                }
            } );
            globalData.put( fact.name,
                            factObject );
            this.workingMemory.setGlobal( fact.name,
                                          factObject );
        }

        doPopulate( toPopulate );

        for ( Iterator<Fixture> iterator = scenario.fixtures.iterator(); iterator.hasNext(); ) {
            Fixture fixture = iterator.next();

            if ( fixture instanceof FactData ) {
                //deal with facts and globals
                final FactData fact = (FactData) fixture;
                final Object factObject = (fact.isModify) ? this.populatedData.get( fact.name ) : eval( "new " + getTypeName( resolver,
                                                                                                                              fact ) + "()" );
                if ( fact.isModify ) {
                    if ( !this.factHandles.containsKey( fact.name ) ) {
                        throw new IllegalArgumentException( "Was not a previously inserted fact. [" + fact.name + "]" );
                    }
                    toPopulate.add( new Populate() {
                        public void go() {
                            populateFields( fact,
                                            populatedData,
                                            factObject );
                            workingMemory.update( factHandles.get( fact.name ),
                                                  factObject );
                        }
                    } );
                } else /* a new one */{
                    populatedData.put( fact.name,
                                       factObject );
                    toPopulate.add( new Populate() {
                        public void go() {
                            populateFields( fact,
                                            populatedData,
                                            factObject );
                            factHandles.put( fact.name,
                                             workingMemory.insert( factObject ) );
                        }
                    } );
                }
            } else if ( fixture instanceof RetractFact ) {
                RetractFact retractFact = (RetractFact) fixture;
                this.workingMemory.retract( this.factHandles.get( retractFact.name ) );
                this.populatedData.remove( retractFact.name );
            } else if ( fixture instanceof ActivateRuleFlowGroup ) {
                workingMemory.getAgenda().activateRuleFlowGroup( ((ActivateRuleFlowGroup) fixture).name );
            } else if ( fixture instanceof ExecutionTrace ) {
                doPopulate( toPopulate );
                ExecutionTrace executionTrace = (ExecutionTrace) fixture;
                //create the listener to trace rules

                if ( listener != null ) this.workingMemory.removeEventListener( listener ); //remove the old
                listener = new TestingEventListener();

                this.workingMemory.addEventListener( listener );

                //set up the time machine
                applyTimeMachine( this.workingMemory,
                                  executionTrace );

                //love you
                long time = System.currentTimeMillis();
                this.workingMemory.fireAllRules( listener.getAgendaFilter( ruleList,
                                                                           scenario.inclusive ),
                                                 scenario.maxRuleFirings );
                executionTrace.executionTimeResult = System.currentTimeMillis() - time;
                executionTrace.numberOfRulesFired = listener.totalFires;
                executionTrace.rulesFired = listener.getRulesFiredSummary();

            } else if ( fixture instanceof Expectation ) {
                doPopulate( toPopulate );
                Expectation assertion = (Expectation) fixture;
                if ( assertion instanceof VerifyFact ) {
                    verify( (VerifyFact) assertion );
                } else if ( assertion instanceof VerifyRuleFired ) {
                    verify( (VerifyRuleFired) assertion,
                            (listener.firingCounts != null) ? listener.firingCounts : new HashMap<String, Integer>() );
                }
            } else {
                throw new IllegalArgumentException( "Not sure what to do with " + fixture );
            }

        }

        doPopulate( toPopulate );
    }

    private void doPopulate(List<Populate> toPopulate) {
        for ( Populate p : toPopulate ) {
            p.go();
        }
        toPopulate.clear();
    }

    private String getTypeName(TypeResolver resolver,
                               FactData fact) throws ClassNotFoundException {

        String fullName = resolver.getFullTypeName( fact.type );
        if ( fullName.equals( "java.util.List" ) || fullName.equals( "java.util.Collection" ) ) {
            return "java.util.ArrayList";
        } else {
            return fullName;
        }
    }

    private void applyTimeMachine(final InternalWorkingMemory wm,
                                  ExecutionTrace executionTrace) {
        if ( executionTrace.scenarioSimulatedDate != null ) {
            final Calendar now = Calendar.getInstance();
            now.setTimeInMillis( executionTrace.scenarioSimulatedDate.getTime() );
            wm.setTimeMachine( new TimeMachine() {
                @Override
                public Calendar getNow() {
                    return now;
                }
            } );
        } else {
            //normal time.
            wm.setTimeMachine( new TimeMachine() );
        }
    }

    void verify(VerifyRuleFired assertion,
                Map<String, Integer> firingCounts) {

        assertion.actualResult = firingCounts.containsKey( assertion.ruleName ) ? firingCounts.get( assertion.ruleName ) : 0;
        if ( assertion.expectedFire != null ) {
            if ( assertion.expectedFire ) {
                if ( assertion.actualResult > 0 ) {
                    assertion.successResult = true;
                    assertion.explanation = "Rule [" + assertion.ruleName + "] was actived " + assertion.actualResult + " times.";
                } else {
                    assertion.successResult = false;
                    assertion.explanation = "Rule [" + assertion.ruleName + "] was not activated. Expected it to be activated.";
                }
            } else {
                if ( assertion.actualResult == 0 ) {
                    assertion.successResult = true;
                    assertion.explanation = "Rule [" + assertion.ruleName + "] was not activated.";
                } else {
                    assertion.successResult = false;
                    assertion.explanation = "Rule [" + assertion.ruleName + "] was activated " + assertion.actualResult + " times, but expected none.";
                }
            }
        }

        if ( assertion.expectedCount != null ) {
            if ( assertion.actualResult.equals( assertion.expectedCount ) ) {
                assertion.successResult = true;
                assertion.explanation = "Rule [" + assertion.ruleName + "] activated " + assertion.actualResult + " times.";
            } else {
                assertion.successResult = false;
                assertion.explanation = "Rule [" + assertion.ruleName + "] activated " + assertion.actualResult + " times. Expected " + assertion.expectedCount + " times.";
            }
        }
    }

    void verify(VerifyFact value) {

        if ( !value.anonymous ) {
            Object factObject = this.populatedData.get( value.name );
            if ( factObject == null ) factObject = this.globalData.get( value.name );
            FactFieldValueVerifier fieldVerifier = new FactFieldValueVerifier( populatedData,
                                                                               value.name,
                                                                               factObject );
            fieldVerifier.checkFields( value.fieldValues );
        } else {
            Iterator obs = this.workingMemory.iterateObjects();
            while ( obs.hasNext() ) {
                Object factObject = obs.next();
                if ( factObject.getClass().getSimpleName().equals( value.name ) ) {
                    FactFieldValueVerifier fieldVerifier = new FactFieldValueVerifier( populatedData,
                                                                                       value.name,
                                                                                       factObject );
                    fieldVerifier.checkFields( value.fieldValues );
                    if ( value.wasSuccessful() ) return;
                }
            }
            for ( VerifyField vfl : value.fieldValues ) {
                if ( vfl.successResult == null ) {
                    vfl.successResult = Boolean.FALSE;
                    vfl.actualResult = "No match";
                }
            }
        }
    }

    Object populateFields(FactData fact,
                          Map<String, Object> factData,
                          Object factObject) {
        for ( int i = 0; i < fact.fieldData.size(); i++ ) {
            FieldData field = (FieldData) fact.fieldData.get( i );
            Object val;
            if ( field.value != null && !field.value.equals( "" ) ) {
                if ( field.value.startsWith( "=" ) ) {
                    // eval the val into existence
                    val = eval( field.value.substring( 1 ),
                                factData );
                } else {
                    val = field.value;
                }
                Map<String, Object> vars = new HashMap<String, Object>();
                vars.putAll( factData );
                vars.put( "__val__",
                          val );
                vars.put( "__fact__",
                          factObject );
                eval( "__fact__." + field.name + " = __val__",
                      vars );
            }
        }
        return factObject;
    }

    /**
     * True if the scenario was run with 100% success.
     */
    public boolean wasSuccess() {
        return this.scenario.wasSuccessful();
    }

    /**
     * @return A pretty printed report detailing any failures that occured
     * when running the scenario (unmet expectations).
     */
    public String getReport() {
        return this.scenario.printFailureReport();
    }

}
