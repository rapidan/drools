package org.drools.planner.core.localsearch.decider.selector;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.drools.planner.core.localsearch.LocalSearchSolverScope;
import org.drools.planner.core.localsearch.StepScope;
import org.drools.planner.core.move.Move;

/**
 * @author Geoffrey De Smet
 */
public class TopListSelector extends AbstractSelector {

    private int topSize;

    private List<Move> topList;

    public void setTopSize(int topSize) {
        this.topSize = topSize;
    }

    // ************************************************************************
    // Worker methods
    // ************************************************************************

    @Override
    public void solvingStarted(LocalSearchSolverScope localSearchSolverScope) {
        topList = Collections.emptyList();
    }

    public Iterator<Move> moveIterator(StepScope stepScope) {
        return topList.iterator();
    }

    @Override
    public void stepTaken(StepScope stepScope) {
        topList = decider.getForager().getTopList(topSize);
    }

}
