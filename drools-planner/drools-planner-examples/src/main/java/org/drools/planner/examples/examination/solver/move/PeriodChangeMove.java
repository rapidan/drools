package org.drools.planner.examples.examination.solver.move;

import java.util.Collection;
import java.util.Collections;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.drools.WorkingMemory;
import org.drools.planner.core.localsearch.decider.acceptor.tabu.TabuPropertyEnabled;
import org.drools.planner.core.move.Move;
import org.drools.planner.examples.examination.domain.Exam;
import org.drools.planner.examples.examination.domain.Period;

/**
 * @author Geoffrey De Smet
 */
public class PeriodChangeMove implements Move, TabuPropertyEnabled {

    private Exam exam;
    private Period toPeriod;

    public PeriodChangeMove(Exam exam, Period toPeriod) {
        this.exam = exam;
        this.toPeriod = toPeriod;
    }

    public boolean isMoveDoable(WorkingMemory workingMemory) {
        return !ObjectUtils.equals(exam.getPeriod(), toPeriod);
    }

    public Move createUndoMove(WorkingMemory workingMemory) {
        return new PeriodChangeMove(exam, exam.getPeriod());
    }

    public void doMove(WorkingMemory workingMemory) {
        ExaminationMoveHelper.movePeriod(workingMemory, exam, toPeriod);
    }

    public Collection<? extends Object> getTabuProperties() {
        return Collections.singletonList(exam);
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o instanceof PeriodChangeMove) {
            PeriodChangeMove other = (PeriodChangeMove) o;
            return new EqualsBuilder()
                    .append(exam, other.exam)
                    .append(toPeriod, other.toPeriod)
                    .isEquals();
        } else {
            return false;
        }
    }

    public int hashCode() {
        return new HashCodeBuilder()
                .append(exam)
                .append(toPeriod)
                .toHashCode();
    }

    public String toString() {
        return exam + " => " + toPeriod;
    }

}