package org.drools.planner.core.localsearch.decider.forager;

import java.util.Comparator;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.drools.planner.core.localsearch.decider.MoveScope;
import org.drools.planner.core.score.Score;

/**
 * @author Geoffrey De Smet
 */
public class AcceptedMoveScopeComparator implements Comparator<MoveScope> {

    private final Comparator<? extends Score> deciderScoreComparator;

    public AcceptedMoveScopeComparator(Comparator<? extends Score> deciderScoreComparator) {
        this.deciderScoreComparator = deciderScoreComparator;
    }
    
    // ************************************************************************
    // Worker methods
    // ************************************************************************

    public int compare(MoveScope a, MoveScope b) {
        CompareToBuilder compareToBuilder = new CompareToBuilder();
        compareToBuilder.append(a.getScore(), b.getScore(), deciderScoreComparator);
        compareToBuilder.append(a.getAcceptChance(), b.getAcceptChance());
        // moves are not compared
        return compareToBuilder.toComparison();
    }

}
