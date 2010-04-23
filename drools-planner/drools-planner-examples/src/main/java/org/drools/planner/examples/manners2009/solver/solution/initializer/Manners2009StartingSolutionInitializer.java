package org.drools.planner.examples.manners2009.solver.solution.initializer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.drools.FactHandle;
import org.drools.WorkingMemory;
import org.drools.planner.core.localsearch.LocalSearchSolverScope;
import org.drools.planner.core.score.DefaultSimpleScore;
import org.drools.planner.core.score.Score;
import org.drools.planner.core.solution.initializer.AbstractStartingSolutionInitializer;
import org.drools.planner.examples.common.domain.PersistableIdComparator;
import org.drools.planner.examples.manners2009.domain.Guest;
import org.drools.planner.examples.manners2009.domain.Manners2009;
import org.drools.planner.examples.manners2009.domain.Seat;
import org.drools.planner.examples.manners2009.domain.SeatDesignation;

/**
 * @author Geoffrey De Smet
 */
public class Manners2009StartingSolutionInitializer extends AbstractStartingSolutionInitializer {

    @Override
    public boolean isSolutionInitialized(LocalSearchSolverScope localSearchSolverScope) {
        Manners2009 manners2009 = (Manners2009) localSearchSolverScope.getWorkingSolution();
        return manners2009.isInitialized();
    }

    public void initializeSolution(LocalSearchSolverScope localSearchSolverScope) {
        Manners2009 manners2009 = (Manners2009) localSearchSolverScope.getWorkingSolution();
        initializeSeatDesignationList(localSearchSolverScope, manners2009);
    }

    private void initializeSeatDesignationList(LocalSearchSolverScope localSearchSolverScope, Manners2009 manners2009) {
        WorkingMemory workingMemory = localSearchSolverScope.getWorkingMemory();
        List<SeatDesignation> seatDesignationList = createSeatDesignationList(manners2009);
        // Assign one guest at a time
        List<Seat> undesignatedSeatList = manners2009.getSeatList();
        for (SeatDesignation seatDesignation : seatDesignationList) {
            Score bestScore = DefaultSimpleScore.valueOf(Integer.MIN_VALUE);
            Seat bestSeat = null;

            FactHandle seatDesignationHandle = null;
            // Try every seat for that guest
            // TODO by reordening the seats so index 0 has a different table then index 1 and so on,
            // this will probably be faster because perfectMatch will be true sooner
            for (Seat seat : undesignatedSeatList) {
                if (seatDesignation.getGuest().getGender() == seat.getRequiredGender()) {
                    if (seatDesignationHandle == null) {
                        seatDesignation.setSeat(seat);
                        seatDesignationHandle = workingMemory.insert(seatDesignation);
                    } else {
                        seatDesignation.setSeat(seat);
                        workingMemory.update(seatDesignationHandle, seatDesignation);
                    }
                    Score score = localSearchSolverScope.calculateScoreFromWorkingMemory();
                    if (score.compareTo(bestScore) > 0) {
                        bestScore = score;
                        bestSeat = seat;
                    }
                }
            }
            if (bestSeat == null) {
                throw new IllegalStateException("The bestSeat (" + bestSeat + ") cannot be null.");
            }
            seatDesignation.setSeat(bestSeat);
            workingMemory.update(seatDesignationHandle, seatDesignation);
            // There will always be enough allowed seats: ok to do this for this problem, but not ok for most problems
            undesignatedSeatList.remove(bestSeat);
        }
        // For the GUI's combobox list mainly, not really needed
        Collections.sort(seatDesignationList, new PersistableIdComparator());
        manners2009.setSeatDesignationList(seatDesignationList);
    }

    private List<SeatDesignation> createSeatDesignationList(Manners2009 manners2009) {
        List<SeatDesignation> seatDesignationList = new ArrayList<SeatDesignation>(manners2009.getGuestList().size());
        for (Guest guest : manners2009.getGuestList()) {
            SeatDesignation seatDesignation = new SeatDesignation();
            seatDesignation.setId(guest.getId());
            seatDesignation.setGuest(guest);
            seatDesignationList.add(seatDesignation);
        }
        return seatDesignationList;
    }

}