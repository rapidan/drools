package org.drools.planner.core.localsearch;

import java.util.Random;

import org.drools.RuleBase;
import org.drools.planner.core.localsearch.bestsolution.BestSolutionRecaller;
import org.drools.planner.core.localsearch.decider.Decider;
import org.drools.planner.core.localsearch.termination.Termination;
import org.drools.planner.core.move.Move;
import org.drools.planner.core.score.calculator.ScoreCalculator;
import org.drools.planner.core.score.definition.ScoreDefinition;
import org.drools.planner.core.solution.Solution;
import org.drools.planner.core.solution.initializer.StartingSolutionInitializer;
import org.drools.planner.core.AbstractSolver;

/**
 * Default implementation of {@link LocalSearchSolver}.
 * @author Geoffrey De Smet
 */
public class DefaultLocalSearchSolver extends AbstractSolver implements LocalSearchSolver,
        LocalSearchSolverLifecycleListener {

    protected Long randomSeed; // TODO refactor to AbstractSolver

    protected StartingSolutionInitializer startingSolutionInitializer = null; // TODO refactor to AbstractSolver
    protected BestSolutionRecaller bestSolutionRecaller;
    protected Termination termination;
    protected Decider decider;

    protected LocalSearchSolverScope localSearchSolverScope = new LocalSearchSolverScope(); // TODO remove me

    public void setRandomSeed(long randomSeed) {
        this.randomSeed = randomSeed;
    }

    public void setRuleBase(RuleBase ruleBase) {
        localSearchSolverScope.setRuleBase(ruleBase);
    }

    public void setScoreDefinition(ScoreDefinition scoreDefinition) {
        localSearchSolverScope.setScoreDefinition(scoreDefinition);
    }

    public void setScoreCalculator(ScoreCalculator scoreCalculator) {
        localSearchSolverScope.setWorkingScoreCalculator(scoreCalculator);
    }

    public StartingSolutionInitializer getStartingSolutionInitializer() {
        return startingSolutionInitializer;
    }

    public void setStartingSolutionInitializer(StartingSolutionInitializer startingSolutionInitializer) {
        this.startingSolutionInitializer = startingSolutionInitializer;
    }

    public void setBestSolutionRecaller(BestSolutionRecaller bestSolutionRecaller) {
        this.bestSolutionRecaller = bestSolutionRecaller;
        this.bestSolutionRecaller.setLocalSearchSolver(this);
        this.bestSolutionRecaller.setSolverEventSupport(solverEventSupport);
    }

    public Decider getDecider() {
        return decider;
    }

    public void setDecider(Decider decider) {
        this.decider = decider;
        this.decider.setLocalSearchSolver(this);
    }

    public void setTermination(Termination termination) {
        this.termination = termination;
        this.termination.setLocalSearchSolver(this);
    }

    public void setStartingSolution(Solution startingSolution) {
        localSearchSolverScope.setWorkingSolution(startingSolution);
    }

    public Solution getBestSolution() {
        return this.localSearchSolverScope.getBestSolution();
    }

    public long getTimeMillisSpend() {
        return this.localSearchSolverScope.calculateTimeMillisSpend();
    }

    public LocalSearchSolverScope getLocalSearchSolverScope() {
        return localSearchSolverScope;
    }

    // ************************************************************************
    // Worker methods
    // ************************************************************************

    @Override
    protected void solveImplementation() {
        LocalSearchSolverScope localSearchSolverScope = this.localSearchSolverScope;
        solvingStarted(localSearchSolverScope);

        StepScope stepScope = createNextStepScope(localSearchSolverScope, null);
        while (!terminatedEarly.get() && !termination.isTerminated(stepScope)) {
            stepScope.setTimeGradient(termination.calculateTimeGradient(stepScope));
            beforeDeciding(stepScope);
            decider.decideNextStep(stepScope);
            Move nextStep = stepScope.getStep();
            if (nextStep == null) {
                // TODO JBRULES-2213 do not terminate, but warn and try again (especially with relativeSelection)
                logger.warn("No move accepted for step index ({}) out of {} accepted moves. Terminating by exception.",
                        stepScope.getStepIndex(), decider.getForager().getAcceptedMovesSize());
                break;
            }
            logger.info("Step index ({}), time spend ({}) taking step ({}) out of {} accepted moves.",
                    new Object[]{stepScope.getStepIndex(), localSearchSolverScope.calculateTimeMillisSpend(),
                            nextStep, decider.getForager().getAcceptedMovesSize()});
            stepDecided(stepScope);
            nextStep.doMove(stepScope.getWorkingMemory());
            // there is no need to recalculate the score, but we still need to set it
            localSearchSolverScope.getWorkingSolution().setScore(stepScope.getScore());
            stepTaken(stepScope);
            stepScope = createNextStepScope(localSearchSolverScope, stepScope);
        }
        solvingEnded(localSearchSolverScope);
    }

    private StepScope createNextStepScope(LocalSearchSolverScope localSearchSolverScope, StepScope completedStepScope) {
        if (completedStepScope == null) {
            completedStepScope = new StepScope(localSearchSolverScope);
            completedStepScope.setScore(localSearchSolverScope.getStartingScore());
            completedStepScope.setStepIndex(-1);
            completedStepScope.setTimeGradient(0.0);
        }
        localSearchSolverScope.setLastCompletedStepScope(completedStepScope);
        StepScope stepScope = new StepScope(localSearchSolverScope);
        stepScope.setStepIndex(completedStepScope.getStepIndex() + 1);
        return stepScope;
    }

    public void solvingStarted(LocalSearchSolverScope localSearchSolverScope) {
        localSearchSolverScope.resetTimeMillisSpend();
        if (randomSeed != null) {
            logger.info("Solving with random seed ({}).", randomSeed);
            localSearchSolverScope.setWorkingRandom(new Random(randomSeed));
        } else {
            logger.info("Solving with a non-fixed random seed.");
            localSearchSolverScope.setWorkingRandom(new Random());
        }
        if (startingSolutionInitializer != null) {
            if (!startingSolutionInitializer.isSolutionInitialized(localSearchSolverScope)) {
                logger.info("Initializing solution.");
                startingSolutionInitializer.initializeSolution(localSearchSolverScope);
            } else {
                logger.debug("Solution is already initialized.");
            }
        }
        localSearchSolverScope.setStartingScore(localSearchSolverScope.calculateScoreFromWorkingMemory());
        bestSolutionRecaller.solvingStarted(localSearchSolverScope);
        termination.solvingStarted(localSearchSolverScope);
        decider.solvingStarted(localSearchSolverScope);
    }

    public void beforeDeciding(StepScope stepScope) {
        bestSolutionRecaller.beforeDeciding(stepScope);
        termination.beforeDeciding(stepScope);
        decider.beforeDeciding(stepScope);
    }

    public void stepDecided(StepScope stepScope) {
        bestSolutionRecaller.stepDecided(stepScope);
        termination.stepDecided(stepScope);
        decider.stepDecided(stepScope);
    }

    public void stepTaken(StepScope stepScope) {
        bestSolutionRecaller.stepTaken(stepScope);
        termination.stepTaken(stepScope);
        decider.stepTaken(stepScope);
    }

    public void solvingEnded(LocalSearchSolverScope localSearchSolverScope) {
        bestSolutionRecaller.solvingEnded(localSearchSolverScope);
        termination.solvingEnded(localSearchSolverScope);
        decider.solvingEnded(localSearchSolverScope);
        logger.info("Solved at step index ({}) with time spend ({}) for best score ({}).", new Object[] {
                localSearchSolverScope.getLastCompletedStepScope().getStepIndex(),
                localSearchSolverScope.calculateTimeMillisSpend(),
                localSearchSolverScope.getBestScore()
        });
    }

}
