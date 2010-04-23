package org.drools.planner.examples.examination.solver.move;

import java.util.Arrays;
import java.util.Collection;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.drools.WorkingMemory;
import org.drools.planner.core.localsearch.decider.acceptor.tabu.TabuPropertyEnabled;
import org.drools.planner.core.move.Move;
import org.drools.planner.examples.examination.domain.Exam;
import org.drools.planner.examples.examination.domain.Period;
import org.drools.planner.examples.examination.domain.Room;

/**
 * @author Geoffrey De Smet
 */
public class ExamSwitchMove implements Move, TabuPropertyEnabled {

    private Exam leftExam;
    private Exam rightExam;

    public ExamSwitchMove(Exam leftExam, Exam rightExam) {
        this.leftExam = leftExam;
        this.rightExam = rightExam;
    }

    public boolean isMoveDoable(WorkingMemory workingMemory) {
        return !(ObjectUtils.equals(leftExam.getPeriod(), rightExam.getPeriod())
                && ObjectUtils.equals(leftExam.getRoom(), rightExam.getRoom()));
    }

    public Move createUndoMove(WorkingMemory workingMemory) {
        return new ExamSwitchMove(rightExam, leftExam);
    }

    public void doMove(WorkingMemory workingMemory) {
        Period oldLeftPeriod = leftExam.getPeriod();
        Period oldRightPeriod = rightExam.getPeriod();
        Room oldLeftRoom = leftExam.getRoom();
        Room oldRightRoom = rightExam.getRoom();
        if (oldLeftPeriod.equals(oldRightPeriod)) {
            ExaminationMoveHelper.moveRoom(workingMemory, leftExam, oldRightRoom);
            ExaminationMoveHelper.moveRoom(workingMemory, rightExam, oldLeftRoom);
        } else if (oldLeftRoom.equals(oldRightRoom)) {
            ExaminationMoveHelper.movePeriod(workingMemory, leftExam, oldRightPeriod);
            ExaminationMoveHelper.movePeriod(workingMemory, rightExam, oldLeftPeriod);
        } else {
            ExaminationMoveHelper.moveExam(workingMemory, leftExam, oldRightPeriod, oldRightRoom);
            ExaminationMoveHelper.moveExam(workingMemory, rightExam, oldLeftPeriod, oldLeftRoom);
        }
    }

    public Collection<? extends Object> getTabuProperties() {
        return Arrays.<Exam>asList(leftExam, rightExam);
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o instanceof ExamSwitchMove) {
            ExamSwitchMove other = (ExamSwitchMove) o;
            return new EqualsBuilder()
                    .append(leftExam, other.leftExam)
                    .append(rightExam, other.rightExam)
                    .isEquals();
        } else {
            return false;
        }
    }

    public int hashCode() {
        return new HashCodeBuilder()
                .append(leftExam)
                .append(rightExam)
                .toHashCode();
    }

    public String toString() {
        return leftExam + " <=> " + rightExam;
    }

}