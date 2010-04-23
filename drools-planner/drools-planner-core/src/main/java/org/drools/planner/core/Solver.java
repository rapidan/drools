package org.drools.planner.core;

import java.util.concurrent.Future;

import org.drools.planner.core.solution.Solution;
import org.drools.planner.core.event.SolverEventListener;

/**
 * A Solver solves planning problems.
 * <p/>
 * Most methods are not thread-safe and should be called from the same thread.
 * @author Geoffrey De Smet
 */
public interface Solver {

    /**
     * @param startingSolution never null
     */
    void setStartingSolution(Solution startingSolution);
    
    Solution getBestSolution();

    /**
     * TODO timeMillisSpend should not continue to increase after the solver has been terminated
     * @return the amount of millis spend since this solver started
     */
    long getTimeMillisSpend();

    /**
     * Solves the planning problem.
     * It can take minutes, even hours or days before this method returns,
     * depending on the termination configuration.
     * To terminate a {@link Solver} early, call {@link #terminateEarly()}.
     * @see #terminateEarly()
     */
    void solve();

    /**
     * Notifies the solver that it should stop at its earliest convenience.
     * This method returns immediatly, but it takes an undetermined time
     * for the {@link #solve()} to actually return.
     * <p/>
     * This method is thread-safe.
     * @see #isTerminatedEarly()
     * @see Future#cancel(boolean)
     * @return true if successful
     */
    boolean terminateEarly();

    /**
     * This method is thread-safe.
     * @see Future#isCancelled()
     * @return true if terminateEarly has been called since the {@Solver} started.
     */
    boolean isTerminatedEarly();

    /**
     * @param eventListener never null
     */
    void addEventListener(SolverEventListener eventListener);
    /**
     * @param eventListener never null
     */
    void removeEventListener(SolverEventListener eventListener);

}
