package org.drools.solver.config.localsearch;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import org.apache.commons.io.IOUtils;
import org.drools.RuleBase;
import org.drools.RuleBaseConfiguration;
import org.drools.RuleBaseFactory;
import org.drools.compiler.DroolsParserException;
import org.drools.compiler.PackageBuilder;
import org.drools.solver.config.localsearch.decider.accepter.AccepterConfig;
import org.drools.solver.config.localsearch.decider.forager.ForagerConfig;
import org.drools.solver.config.localsearch.decider.selector.SelectorConfig;
import org.drools.solver.config.localsearch.finish.FinishConfig;
import org.drools.solver.config.score.definition.ScoreDefinitionConfig;
import org.drools.solver.core.localsearch.DefaultLocalSearchSolver;
import org.drools.solver.core.localsearch.LocalSearchSolver;
import org.drools.solver.core.localsearch.bestsolution.BestSolutionRecaller;
import org.drools.solver.core.localsearch.decider.Decider;
import org.drools.solver.core.localsearch.decider.DefaultDecider;
import org.drools.solver.core.solution.initializer.StartingSolutionInitializer;
import org.drools.solver.core.score.definition.ScoreDefinition;

/**
 * @author Geoffrey De Smet
 */
@XStreamAlias("localSearchSolver")
public class LocalSearchSolverConfig {

    private Long randomSeed = null;

    @XStreamImplicit(itemFieldName = "scoreDrl")
    private List<String> scoreDrlList = null;
    @XStreamAlias("scoreDefinition")
    private ScoreDefinitionConfig scoreDefinitionConfig = new ScoreDefinitionConfig();

    private StartingSolutionInitializer startingSolutionInitializer = null;
    private Class<StartingSolutionInitializer> startingSolutionInitializerClass = null;

    @XStreamAlias("finish")
    private FinishConfig finishConfig = new FinishConfig(); // TODO this new is pointless due to xstream

    @XStreamAlias("selector")
    private SelectorConfig selectorConfig = new SelectorConfig();
    @XStreamAlias("accepter")
    private AccepterConfig accepterConfig = new AccepterConfig();
    @XStreamAlias("forager")
    private ForagerConfig foragerConfig = new ForagerConfig();

    public Long getRandomSeed() {
        return randomSeed;
    }

    public void setRandomSeed(Long randomSeed) {
        this.randomSeed = randomSeed;
    }

    public List<String> getScoreDrlList() {
        return scoreDrlList;
    }

    public void setScoreDrlList(List<String> scoreDrlList) {
        this.scoreDrlList = scoreDrlList;
    }

    public ScoreDefinitionConfig getScoreDefinitionConfig() {
        return scoreDefinitionConfig;
    }

    public void setScoreDefinitionConfig(ScoreDefinitionConfig scoreDefinitionConfig) {
        this.scoreDefinitionConfig = scoreDefinitionConfig;
    }

    public StartingSolutionInitializer getStartingSolutionInitializer() {
        return startingSolutionInitializer;
    }

    public void setStartingSolutionInitializer(StartingSolutionInitializer startingSolutionInitializer) {
        this.startingSolutionInitializer = startingSolutionInitializer;
    }

    public Class<StartingSolutionInitializer> getStartingSolutionInitializerClass() {
        return startingSolutionInitializerClass;
    }

    public void setStartingSolutionInitializerClass(Class<StartingSolutionInitializer> startingSolutionInitializerClass) {
        this.startingSolutionInitializerClass = startingSolutionInitializerClass;
    }

    public FinishConfig getFinishConfig() {
        return finishConfig;
    }

    public void setFinishConfig(FinishConfig finishConfig) {
        this.finishConfig = finishConfig;
    }

    public SelectorConfig getSelectorConfig() {
        return selectorConfig;
    }

    public void setSelectorConfig(SelectorConfig selectorConfig) {
        this.selectorConfig = selectorConfig;
    }

    public AccepterConfig getAccepterConfig() {
        return accepterConfig;
    }

    public void setAccepterConfig(AccepterConfig accepterConfig) {
        this.accepterConfig = accepterConfig;
    }

    public ForagerConfig getForagerConfig() {
        return foragerConfig;
    }

    public void setForagerConfig(ForagerConfig foragerConfig) {
        this.foragerConfig = foragerConfig;
    }

    // ************************************************************************
    // Builder methods
    // ************************************************************************

    public LocalSearchSolver buildSolver() {
        DefaultLocalSearchSolver localSearchSolver = new DefaultLocalSearchSolver();
        if (randomSeed != null) {
            localSearchSolver.setRandomSeed(randomSeed);
        } else {
            localSearchSolver.setRandomSeed(0L);
        }
        localSearchSolver.setRuleBase(buildRuleBase());
        ScoreDefinition scoreDefinition = scoreDefinitionConfig.buildScoreDefinition();
        localSearchSolver.setScoreDefinition(scoreDefinition);
        // remove when score-in-solution refactor
        localSearchSolver.setScoreCalculator(scoreDefinitionConfig.buildScoreCalculator());
        localSearchSolver.setStartingSolutionInitializer(buildStartingSolutionInitializer());
        localSearchSolver.setBestSolutionRecaller(new BestSolutionRecaller());
        localSearchSolver.setFinish(finishConfig.buildFinish(scoreDefinition));
        localSearchSolver.setDecider(buildDecider());
        return localSearchSolver;
    }

    private RuleBase buildRuleBase() {
        PackageBuilder packageBuilder = new PackageBuilder();
        for (String scoreDrl : scoreDrlList) {
            InputStream scoreDrlIn = getClass().getResourceAsStream(scoreDrl);
            if (scoreDrlIn == null) {
                throw new IllegalArgumentException("scoreDrl (" + scoreDrl + ") does not exist as a classpath resource.");
            }
            try {
                packageBuilder.addPackageFromDrl(new InputStreamReader(scoreDrlIn, "utf-8"));
            } catch (DroolsParserException e) {
                throw new IllegalArgumentException("scoreDrl (" + scoreDrl + ") could not be loaded.", e);
            } catch (IOException e) {
                throw new IllegalArgumentException("scoreDrl (" + scoreDrl + ") could not be loaded.", e);
            } finally {
                IOUtils.closeQuietly(scoreDrlIn);
            }
        }
        RuleBaseConfiguration ruleBaseConfiguration = new RuleBaseConfiguration();
        RuleBase ruleBase = RuleBaseFactory.newRuleBase(ruleBaseConfiguration);
        if (packageBuilder.hasErrors()) {
            throw new IllegalStateException("There are errors in the scoreDrl's:"
                    + packageBuilder.getErrors().toString());
        }
        ruleBase.addPackage(packageBuilder.getPackage());
        return ruleBase;
    }

    public StartingSolutionInitializer buildStartingSolutionInitializer() {
        if (startingSolutionInitializer != null) {
            return startingSolutionInitializer;
        } else if (startingSolutionInitializerClass != null) {
            try {
                return startingSolutionInitializerClass.newInstance();
            } catch (InstantiationException e) {
                throw new IllegalArgumentException("startingSolutionInitializerClass ("
                        + startingSolutionInitializerClass.getName()
                        + ") does not have a public no-arg constructor", e);
            } catch (IllegalAccessException e) {
                throw new IllegalArgumentException("startingSolutionInitializerClass ("
                        + startingSolutionInitializerClass.getName()
                        + ") does not have a public no-arg constructor", e);
            }
        } else {
            return null;
        }
    }

    private Decider buildDecider() {
        DefaultDecider decider = new DefaultDecider();
        decider.setSelector(selectorConfig.buildSelector());
        decider.setAccepter(accepterConfig.buildAccepter());
        decider.setForager(foragerConfig.buildForager());
        return decider;
    }

    public void inherit(LocalSearchSolverConfig inheritedConfig) {
        if (randomSeed == null) {
            randomSeed = inheritedConfig.getRandomSeed();
        }
        if (scoreDrlList == null) {
            scoreDrlList = inheritedConfig.getScoreDrlList();
        } else {
            List<String> inheritedScoreDrlList = inheritedConfig.getScoreDrlList();
            if (inheritedScoreDrlList != null) {
                for (String inheritedScoreDrl : inheritedScoreDrlList) {
                    if (!scoreDrlList.contains(inheritedScoreDrl)) {
                        scoreDrlList.add(inheritedScoreDrl);
                    }
                }
            }
        }
        if (scoreDefinitionConfig == null) {
            scoreDefinitionConfig = inheritedConfig.getScoreDefinitionConfig();
        } else if (inheritedConfig.getScoreDefinitionConfig() != null) {
            scoreDefinitionConfig.inherit(inheritedConfig.getScoreDefinitionConfig());
        }
        if (startingSolutionInitializer == null && startingSolutionInitializerClass == null) {
            startingSolutionInitializer = inheritedConfig.getStartingSolutionInitializer();
            startingSolutionInitializerClass = inheritedConfig.getStartingSolutionInitializerClass();
        }
        if (finishConfig == null) {
            finishConfig = inheritedConfig.getFinishConfig();
        } else if (inheritedConfig.getFinishConfig() != null) {
            finishConfig.inherit(inheritedConfig.getFinishConfig());
        }
        if (selectorConfig == null) {
            selectorConfig = inheritedConfig.getSelectorConfig();
        } else if (inheritedConfig.getSelectorConfig() != null) {
            selectorConfig.inherit(inheritedConfig.getSelectorConfig());
        }
        if (accepterConfig == null) {
            accepterConfig = inheritedConfig.getAccepterConfig();
        } else if (inheritedConfig.getAccepterConfig() != null) {
            accepterConfig.inherit(inheritedConfig.getAccepterConfig());
        }
        if (foragerConfig == null) {
            foragerConfig = inheritedConfig.getForagerConfig();
        } else if (inheritedConfig.getForagerConfig() != null) {
            foragerConfig.inherit(inheritedConfig.getForagerConfig());
        }
    }

}
