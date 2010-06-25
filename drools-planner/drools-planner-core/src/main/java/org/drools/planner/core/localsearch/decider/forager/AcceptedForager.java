package org.drools.planner.core.localsearch.decider.forager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;

import org.drools.planner.core.localsearch.StepScope;
import org.drools.planner.core.localsearch.decider.MoveScope;
import org.drools.planner.core.localsearch.decider.acceptor.Acceptor;
import org.drools.planner.core.move.Move;
import org.drools.planner.core.score.Score;

/**
 * An AcceptedForager is a Forager which forages accepted moves and ignores unaccepted moves.
 * @see Forager
 * @see Acceptor
 * @author Geoffrey De Smet
 */
public class AcceptedForager extends AbstractForager {

    // final to allow better hotspot optimization. TODO prove that it indeed makes a difference
    protected final PickEarlyByScore pickEarlyByScore;
    protected final int minimalAcceptedSelection;

    protected AcceptedMoveScopeComparator acceptedMoveScopeComparator;

    protected int selectedCount;
    protected List<MoveScope> acceptedList;
    protected boolean listSorted;
    protected Score maxScore;
    protected double acceptChanceMaxScoreTotal;

    protected MoveScope earlyPickedMoveScope = null;

    public AcceptedForager(PickEarlyByScore pickEarlyByScore, int minimalAcceptedSelection) {
        this.pickEarlyByScore = pickEarlyByScore;
        this.minimalAcceptedSelection = minimalAcceptedSelection;
    }

    // ************************************************************************
    // Worker methods
    // ************************************************************************

    @Override
    public void beforeDeciding(StepScope stepScope) {
        acceptedMoveScopeComparator = new AcceptedMoveScopeComparator(stepScope.getDeciderScoreComparator());
        selectedCount = 0;
        acceptedList = new ArrayList<MoveScope>(1024); // TODO use size of moveList in decider
        listSorted = false;
        maxScore = stepScope.getLocalSearchSolverScope().getScoreDefinition().getPerfectMinimumScore();
        acceptChanceMaxScoreTotal = 0.0;
        earlyPickedMoveScope = null;
    }

    public void addMove(MoveScope moveScope) {
        selectedCount++;
        if (moveScope.getAcceptChance() > 0.0) {
            checkPickEarly(moveScope);
            addMoveScopeToAcceptedList(moveScope);
        }
    }

    protected void checkPickEarly(MoveScope moveScope) {
        switch (pickEarlyByScore) {
            case NONE:
                break;
            case FIRST_BEST_SCORE_IMPROVING:
                if (moveScope.getStepScope().getDeciderScoreComparator().compare(moveScope.getScore(),
                        moveScope.getStepScope().getLocalSearchSolverScope().getBestScore()) > 0) {
                    earlyPickedMoveScope = moveScope;
                }
                break;
            case FIRST_LAST_STEP_SCORE_IMPROVING:
                if (moveScope.getStepScope().getDeciderScoreComparator().compare(moveScope.getScore(),
                        moveScope.getStepScope().getLocalSearchSolverScope().getLastCompletedStepScope().getScore())
                        > 0) {
                    earlyPickedMoveScope = moveScope;
                }
                break;
            default:
                throw new IllegalStateException("The pickEarlyByScore (" + pickEarlyByScore + ") is not implemented");
        }
    }

    protected void addMoveScopeToAcceptedList(MoveScope moveScope) {
        acceptedList.add(moveScope);
        listSorted = false;
        if (moveScope.getStepScope().getDeciderScoreComparator().compare(moveScope.getScore(), maxScore) > 0) {
            acceptChanceMaxScoreTotal = moveScope.getAcceptChance();
            maxScore = moveScope.getScore();
        } else if (moveScope.getScore().equals(maxScore)) {
            acceptChanceMaxScoreTotal += moveScope.getAcceptChance();
        }
    }

    public boolean isQuitEarly() {
        return earlyPickedMoveScope != null || acceptedList.size() >= minimalAcceptedSelection;
    }

    public MoveScope pickMove(StepScope stepScope) {
        if (earlyPickedMoveScope != null) {
            return earlyPickedMoveScope;
        } else {
            return pickMaxScoreMoveScopeFromAcceptedList(stepScope);
        }
    }

    protected MoveScope pickMaxScoreMoveScopeFromAcceptedList(StepScope stepScope) {
        if (acceptedList.isEmpty()) {
            return null;
        }
        sortAcceptedList();
        MoveScope pickedMoveScope = null;
        double randomChance = stepScope.getWorkingRandom().nextDouble();
        double acceptMark = acceptChanceMaxScoreTotal * randomChance;
        for (ListIterator<MoveScope> it = acceptedList.listIterator(acceptedList.size()); it.hasPrevious();) {
            MoveScope moveScope = it.previous();
            acceptMark -= moveScope.getAcceptChance();
            // TODO That underflow warn is nonsense. randomChance can be 0.0 and the last acceptMark can end up 0.0
            // TODO so < is nonsense (do a testcase though)
            if (acceptMark < 0.0) {
                pickedMoveScope = moveScope;
                break;
            }
        }
        if (pickedMoveScope == null) {
            // TODO This isn't really underflow when an forager accepts only moves with acceptChance 0.0
            logger.warn("Underflow occurred with acceptChanceMaxScoreTotal ({}) " +
                    "and randomChance ({}).", acceptChanceMaxScoreTotal, randomChance);
            // Deal with it anyway (no fail-fast here)
            pickedMoveScope = acceptedList.get(acceptedList.size() - 1);
        }
        return pickedMoveScope;
    }

    public int getAcceptedMovesSize() {
        return acceptedList.size();
    }

    public List<Move> getTopList(int topSize) {
        sortAcceptedList();
        int size = acceptedList.size();
        List<Move> topList = new ArrayList<Move>(Math.min(topSize, size));
        List<MoveScope> subAcceptedList = acceptedList.subList(Math.max(0, size - topSize), size);
        for (MoveScope moveScope : subAcceptedList) {
            topList.add(moveScope.getMove());
        }
        return topList;
    }

    protected void sortAcceptedList() {
        if (!listSorted) {
            Collections.sort(acceptedList, acceptedMoveScopeComparator);
            listSorted = true;
        }
    }

}
