package org.drools.planner.examples.nqueens.solver.move;

import java.util.Collection;
import java.util.Collections;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.drools.WorkingMemory;
import org.drools.FactHandle;
import org.drools.planner.core.localsearch.decider.acceptor.tabu.TabuPropertyEnabled;
import org.drools.planner.core.move.Move;
import org.drools.planner.examples.nqueens.domain.Queen;

/**
 * @author Geoffrey De Smet
 */
public class YChangeMove implements Move, TabuPropertyEnabled {

    private Queen queen;
    private int toY;

    public YChangeMove(Queen queen, int toY) {
        this.queen = queen;
        this.toY = toY;
    }

    public boolean isMoveDoable(WorkingMemory workingMemory) {
        return queen.getY() != toY;
    }

    public Move createUndoMove(WorkingMemory workingMemory) {
        return new YChangeMove(queen, queen.getY());
    }

    public void doMove(WorkingMemory workingMemory) {
        FactHandle queenHandle = workingMemory.getFactHandle(queen);
        queen.setY(toY);
        workingMemory.update(queenHandle, queen); // after changes are made
    }

    public Collection<? extends Object> getTabuProperties() {
        return Collections.singletonList(queen);
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o instanceof YChangeMove) {
            YChangeMove other = (YChangeMove) o;
            return new EqualsBuilder()
                    .append(queen, other.queen)
                    .append(toY, other.toY)
                    .isEquals();
        } else {
            return false;
        }
    }

    public int hashCode() {
        return new HashCodeBuilder()
                .append(queen)
                .append(toY)
                .toHashCode();
    }

    public String toString() {
        return queen + " => " + toY;
    }

}
