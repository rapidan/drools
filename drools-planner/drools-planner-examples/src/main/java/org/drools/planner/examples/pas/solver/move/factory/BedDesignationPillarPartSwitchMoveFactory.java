package org.drools.planner.examples.pas.solver.move.factory;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.drools.planner.core.move.Move;
import org.drools.planner.core.move.CompositeMove;
import org.drools.planner.core.move.factory.AbstractMoveFactory;
import org.drools.planner.core.solution.Solution;
import org.drools.planner.examples.pas.domain.BedDesignation;
import org.drools.planner.examples.pas.domain.PatientAdmissionSchedule;
import org.drools.planner.examples.pas.domain.Bed;
import org.drools.planner.examples.pas.solver.move.BedChangeMove;
import org.apache.commons.lang.builder.CompareToBuilder;

/**
 * @author Geoffrey De Smet
 */
public class BedDesignationPillarPartSwitchMoveFactory extends AbstractMoveFactory {

    public List<Move> createMoveList(Solution solution) {
        PatientAdmissionSchedule patientAdmissionSchedule = (PatientAdmissionSchedule) solution;

        Map<Bed, List<BedDesignation>> bedToBedDesignationList = new HashMap<Bed, List<BedDesignation>>(
                patientAdmissionSchedule.getBedList().size());
        for (BedDesignation bedDesignation : patientAdmissionSchedule.getBedDesignationList()) {
            List<BedDesignation> bedDesignationListPerBed = bedToBedDesignationList.get(bedDesignation.getBed());
            if (bedDesignationListPerBed == null) {
                // Note: the initialCapacity is probably to high,
                // which is bad for memory, but the opposite is bad for performance (which is worse)
                bedDesignationListPerBed = new ArrayList<BedDesignation>(
                        patientAdmissionSchedule.getNightList().size());
                bedToBedDesignationList.put(bedDesignation.getBed(), bedDesignationListPerBed);
            }
            bedDesignationListPerBed.add(bedDesignation);
        }
        for (List<BedDesignation> bedDesignationListPerBed : bedToBedDesignationList.values()) {
            Collections.sort(bedDesignationListPerBed, new Comparator<BedDesignation>() {
                public int compare(BedDesignation a, BedDesignation b) {
                    // This comparison is sameBedInSameNight safe.
                    return new CompareToBuilder()
                            .append(a.getAdmissionPart().getFirstNight(), b.getAdmissionPart().getFirstNight())
                            .append(a.getAdmissionPart().getLastNight(), b.getAdmissionPart().getLastNight())
                            .append(a.getAdmissionPart(), b.getAdmissionPart())
                            .toComparison();
                }
            });
        }

        List<Bed> bedList = patientAdmissionSchedule.getBedList();
        List<Move> moveList = new ArrayList<Move>();

        // For every 2 distinct beds
        for (ListIterator<Bed> leftBedIt = bedList.listIterator(); leftBedIt.hasNext();) {
            Bed leftBed = leftBedIt.next();
            for (ListIterator<Bed> rightBedIt = bedList.listIterator(leftBedIt.nextIndex());
                    rightBedIt.hasNext();) {
                Bed rightBed = rightBedIt.next();
                List<BedDesignation> leftBedDesignationList = bedToBedDesignationList.get(leftBed);
                if (leftBedDesignationList == null) {
                    leftBedDesignationList = Collections.emptyList();
                }
                List<BedDesignation> rightBedDesignationList = bedToBedDesignationList.get(rightBed);
                if (rightBedDesignationList == null) {
                    rightBedDesignationList = Collections.emptyList();
                }
                LowestFirstNightBedDesignationIterator lowestIt = new LowestFirstNightBedDesignationIterator(
                        leftBedDesignationList, rightBedDesignationList);
                // For every pillar part duo
                while (lowestIt.hasNext()) {
                    BedDesignation pillarPartBedDesignation = lowestIt.next();
                    // Note: the initialCapacity is probably to high,
                    // which is bad for memory, but the opposite is bad for performance (which is worse)
                    List<Move> moveListByPillarPartDuo = new ArrayList<Move>(
                            leftBedDesignationList.size() + rightBedDesignationList.size());
                    int lastNightIndex = pillarPartBedDesignation.getAdmissionPart().getLastNight().getIndex();
                    Bed otherBed;
                    int leftMinimumFirstNightIndex = Integer.MIN_VALUE;
                    int rightMinimumFirstNightIndex = Integer.MIN_VALUE;
                    if (lowestIt.isLastNextWasLeft()) {
                        otherBed = rightBed;
                        leftMinimumFirstNightIndex = lastNightIndex;
                    } else {
                        otherBed = leftBed;
                        rightMinimumFirstNightIndex = lastNightIndex;
                    }
                    moveListByPillarPartDuo.add(new BedChangeMove(pillarPartBedDesignation, otherBed));
                    // For every BedDesignation in that pillar part duo
                    while (lowestIt.hasNextWithMaximumFirstNightIndexes(
                            leftMinimumFirstNightIndex, rightMinimumFirstNightIndex)) {
                        pillarPartBedDesignation = lowestIt.next();
                        lastNightIndex = pillarPartBedDesignation.getAdmissionPart().getLastNight().getIndex();
                        if (lowestIt.isLastNextWasLeft()) {
                            otherBed = rightBed;
                            leftMinimumFirstNightIndex = Math.max(leftMinimumFirstNightIndex, lastNightIndex);
                        } else {
                            otherBed = leftBed;
                            rightMinimumFirstNightIndex = Math.max(rightMinimumFirstNightIndex, lastNightIndex);
                        }
                        moveListByPillarPartDuo.add(new BedChangeMove(pillarPartBedDesignation, otherBed));
                    }
                    moveList.add(new CompositeMove(moveListByPillarPartDuo));
                }
            }
        }
        return moveList;
    }

    private class LowestFirstNightBedDesignationIterator implements Iterator<BedDesignation> {

        private Iterator<BedDesignation> leftIterator;
        private Iterator<BedDesignation> rightIterator;

        private boolean leftHasNext = true;
        private boolean rightHasNext = true;

        private BedDesignation nextLeft;
        private BedDesignation nextRight;

        private boolean lastNextWasLeft;

        public LowestFirstNightBedDesignationIterator(
                List<BedDesignation> leftBedDesignationList, List<BedDesignation> rightBedDesignationList) {
            // Buffer the nextLeft and nextRight
            leftIterator = leftBedDesignationList.iterator();
            if (leftIterator.hasNext()) {
                nextLeft = leftIterator.next();
            } else {
                leftHasNext = false;
                nextLeft = null;
            }
            rightIterator = rightBedDesignationList.iterator();
            if (rightIterator.hasNext()) {
                nextRight = rightIterator.next();
            } else {
                rightHasNext = false;
                nextRight = null;
            }
        }

        public boolean hasNext() {
            return leftHasNext || rightHasNext;
        }

        public boolean hasNextWithMaximumFirstNightIndexes(
                int leftMinimumFirstNightIndex, int rightMinimumFirstNightIndex) {
            if (!hasNext()) {
                return false;
            }
            boolean nextIsLeft = nextIsLeft();
            if (nextIsLeft) {
                int firstNightIndex = nextLeft.getAdmissionPart().getFirstNight().getIndex();
                // It should not be conflict in the same pillar and it should be in conflict with the other pillar
                return firstNightIndex > leftMinimumFirstNightIndex && firstNightIndex <= rightMinimumFirstNightIndex;
            } else {
                int firstNightIndex = nextRight.getAdmissionPart().getFirstNight().getIndex();
                // It should not be conflict in the same pillar and it should be in conflict with the other pillar
                return firstNightIndex > rightMinimumFirstNightIndex && firstNightIndex <= leftMinimumFirstNightIndex;
            }
        }

        public BedDesignation next() {
            lastNextWasLeft = nextIsLeft();
            // Buffer the nextLeft or nextRight
            BedDesignation lowest;
            if (lastNextWasLeft) {
                lowest = nextLeft;
                if (leftIterator.hasNext()) {
                    nextLeft = leftIterator.next();
                } else {
                    leftHasNext = false;
                    nextLeft = null;
                }
            } else {
                lowest = nextRight;
                if (rightIterator.hasNext()) {
                    nextRight = rightIterator.next();
                } else {
                    rightHasNext = false;
                    nextRight = null;
                }
            }
            return lowest;
        }

        private boolean nextIsLeft() {
            boolean returnLeft;
            if (leftHasNext) {
                if (rightHasNext) {
                    int leftFirstNightIndex = nextLeft.getAdmissionPart().getFirstNight().getIndex();
                    int rightFirstNightIndex = nextRight.getAdmissionPart().getFirstNight().getIndex();
                    returnLeft = leftFirstNightIndex <= rightFirstNightIndex;
                } else {
                    returnLeft = true;
                }
            } else {
                if (rightHasNext) {
                    returnLeft = false;
                } else {
                    throw new NoSuchElementException();
                }
            }
            return returnLeft;
        }

        public void remove() {
            throw new UnsupportedOperationException("Remove not supported.");
        }

        public boolean isLastNextWasLeft() {
            return lastNextWasLeft;
        }

    }

}
