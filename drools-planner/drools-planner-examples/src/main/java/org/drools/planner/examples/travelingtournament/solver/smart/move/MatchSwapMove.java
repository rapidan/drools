package org.drools.planner.examples.travelingtournament.solver.smart.move;

import java.util.Arrays;
import java.util.Collection;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.drools.WorkingMemory;
import org.drools.FactHandle;
import org.drools.planner.core.localsearch.decider.acceptor.tabu.TabuPropertyEnabled;
import org.drools.planner.core.move.Move;
import org.drools.planner.examples.travelingtournament.domain.Day;
import org.drools.planner.examples.travelingtournament.domain.Match;

/**
 * @author Geoffrey De Smet
 */
public class MatchSwapMove implements Move, TabuPropertyEnabled {

    private Match firstMatch;
    private Match secondMatch;

    public MatchSwapMove(Match firstMatch, Match secondMatch) {
        this.firstMatch = firstMatch;
        this.secondMatch = secondMatch;
    }

    public boolean isMoveDoable(WorkingMemory workingMemory) {
        return true;
    }

    public Move createUndoMove(WorkingMemory workingMemory) {
        return this;
    }

    public void doMove(WorkingMemory workingMemory) {
        Day oldFirstMatchDay = firstMatch.getDay();
        FactHandle firstMatchHandle = workingMemory.getFactHandle(firstMatch);
        FactHandle secondMatchHandle = workingMemory.getFactHandle(secondMatch);
        firstMatch.setDay(secondMatch.getDay());
        secondMatch.setDay(oldFirstMatchDay);
        workingMemory.update(firstMatchHandle, firstMatch);
        workingMemory.update(secondMatchHandle, secondMatch);
    }

    public Collection<? extends Object> getTabuProperties() {
        return Arrays.asList(firstMatch, secondMatch);
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o instanceof MatchSwapMove) {
            MatchSwapMove other = (MatchSwapMove) o;
            return new EqualsBuilder()
                    .append(firstMatch, other.firstMatch)
                    .append(secondMatch, other.secondMatch)
                    .isEquals();
        } else {
            return false;
        }
    }

    public int hashCode() {
        return new HashCodeBuilder()
                .append(firstMatch)
                .append(secondMatch)
                .toHashCode();
    }

    public String toString() {
        return firstMatch + " <=> " + secondMatch;
    }

}
