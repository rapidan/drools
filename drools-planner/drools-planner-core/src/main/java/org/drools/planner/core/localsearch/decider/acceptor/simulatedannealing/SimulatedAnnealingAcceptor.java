package org.drools.planner.core.localsearch.decider.acceptor.simulatedannealing;

import org.drools.planner.core.localsearch.decider.MoveScope;
import org.drools.planner.core.localsearch.decider.acceptor.AbstractAcceptor;
import org.drools.planner.core.localsearch.LocalSearchSolverScope;
import org.drools.planner.core.score.Score;

/**
 * TODO Under construction. Feel free to create a patch to improve this acceptor!
 * @author Geoffrey De Smet
 */
public class SimulatedAnnealingAcceptor extends AbstractAcceptor {

    protected boolean compareToBestScore = false;

    public void setCompareToBestScore(boolean compareToBestScore) {
        this.compareToBestScore = compareToBestScore;
    }

    // ************************************************************************
    // Worker methods
    // ************************************************************************

    public double calculateAcceptChance(MoveScope moveScope) {
        Score compareScore = compareToBestScore
                ? moveScope.getStepScope().getLocalSearchSolverScope().getBestScore()
                : moveScope.getStepScope().getLocalSearchSolverScope().getLastCompletedStepScope().getScore();
        // TODO Support for decision score
        Score moveScore = moveScope.getScore();
        if (moveScore.compareTo(compareScore) > 0) {
            // It's better so accept it.
            return 1.0;
        }
        double timeGradient = moveScope.getStepScope().getTimeGradient();
        LocalSearchSolverScope localSearchSolverScope = moveScope.getStepScope().getLocalSearchSolverScope();
        // TODO This algorithm might be nice, but the normal temparture based algorithm should also be possible
        double scoreDelta = 1.0 - localSearchSolverScope.getScoreDefinition().calculateTimeGradient(
                localSearchSolverScope.getStartingScore(), compareScore, moveScore);
        double acceptChance = Math.exp(timeGradient * scoreDelta);
        return acceptChance;
    }
    
}
