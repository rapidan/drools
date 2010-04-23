package org.drools.planner.core.localsearch.termination;

import org.drools.planner.core.localsearch.LocalSearchSolver;
import org.drools.planner.core.localsearch.LocalSearchSolverScope;
import org.drools.planner.core.localsearch.StepScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract superclass for {@link Termination}.
 * @author Geoffrey De Smet
 */
public abstract class AbstractTermination implements Termination {

    protected final transient Logger logger = LoggerFactory.getLogger(getClass());

    protected LocalSearchSolver localSearchSolver;

    public void setLocalSearchSolver(LocalSearchSolver localSearchSolver) {
        this.localSearchSolver = localSearchSolver;
    }

    // ************************************************************************
    // Worker methods
    // ************************************************************************

    public void solvingStarted(LocalSearchSolverScope localSearchSolverScope) {
        // Hook which can be optionally overwritten by subclasses.
    }

    public void beforeDeciding(StepScope stepScope) {
        // Hook which can be optionally overwritten by subclasses.
    }

    public void stepDecided(StepScope stepScope) {
        // Hook which can be optionally overwritten by subclasses.
    }

    public void stepTaken(StepScope stepScope) {
        // Hook which can be optionally overwritten by subclasses.
    }

    public void solvingEnded(LocalSearchSolverScope localSearchSolverScope) {
        // Hook which can be optionally overwritten by subclasses.
    }

}
