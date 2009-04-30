package org.drools.solver.core.score.definition;

import org.drools.solver.core.score.HardAndSoftScore;
import org.drools.solver.core.score.DefaultHardAndSoftScore;
import org.drools.solver.core.score.Score;

/**
 * @author Geoffrey De Smet
 */
public class HardAndSoftScoreDefinition extends AbstractScoreDefinition<HardAndSoftScore> {

    private double hardScoreTimeGradientWeight = 0.75;

    private HardAndSoftScore perfectMaximumScore = new DefaultHardAndSoftScore(0, 0);
    private HardAndSoftScore perfectMinimumScore = new DefaultHardAndSoftScore(Integer.MIN_VALUE, Integer.MIN_VALUE);

    /**
     * It's recommended to use a number which can be exactly represented as a double,
     * such as 0.5, 0.25, 0.75, 0.125, ... but not 0.1, 0.2, ... 
     * @param hardScoreTimeGradientWeight 0.0 <= hardScoreTimeGradientWeight <= 1.0
     */
    public void setHardScoreTimeGradientWeight(double hardScoreTimeGradientWeight) {
        this.hardScoreTimeGradientWeight = hardScoreTimeGradientWeight;
        if (hardScoreTimeGradientWeight < 0.0 || hardScoreTimeGradientWeight > 1.0) {
            throw new IllegalArgumentException("Property hardScoreTimeGradientWeight (" + hardScoreTimeGradientWeight
                    + ") must be greater or equal to 0.0jg and smaller or equal to 1.0.");
        }
    }

    public void setPerfectMaximumScore(HardAndSoftScore perfectMaximumScore) {
        this.perfectMaximumScore = perfectMaximumScore;
    }

    public void setPerfectMinimumScore(HardAndSoftScore perfectMinimumScore) {
        this.perfectMinimumScore = perfectMinimumScore;
    }

    // ************************************************************************
    // Worker methods
    // ************************************************************************

    public HardAndSoftScore getPerfectMaximumScore() {
        return perfectMaximumScore;
    }

    public HardAndSoftScore getPerfectMinimumScore() {
        return perfectMinimumScore;
    }

    public Score parseScore(String scoreString) {
        return DefaultHardAndSoftScore.parseScore(scoreString);
    }

    public double calculateTimeGradient(HardAndSoftScore startScore, HardAndSoftScore endScore,
            HardAndSoftScore score) {
        if (score.getHardScore() > endScore.getHardScore()) {
            return 1.0;
        } else if (startScore.getHardScore() > score.getHardScore()) {
            return 0.0;
        }
        double softScoreTimeGradientWeight;
        double timeGradient;
        if (startScore.getHardScore() == endScore.getHardScore()) {
            softScoreTimeGradientWeight = 1.0;
            timeGradient = 0.0;
        } else {
            softScoreTimeGradientWeight = 1.0 - hardScoreTimeGradientWeight;
            int hardScoreTotal = endScore.getHardScore() - startScore.getHardScore();
            int hardScoreDelta = score.getHardScore() - startScore.getHardScore();
            double hardTimeGradient = (double) hardScoreDelta / (double) hardScoreTotal;
            timeGradient = hardTimeGradient * hardScoreTimeGradientWeight;
        }
        if (score.getSoftScore() >= endScore.getSoftScore()) {
            timeGradient += softScoreTimeGradientWeight;
        } else if (startScore.getSoftScore() >= score.getSoftScore()) {
            // No change: timeGradient += 0.0
        } else {
            int softScoreTotal = endScore.getSoftScore() - startScore.getSoftScore();
            int softScoreDelta = score.getSoftScore() - startScore.getSoftScore();
            double softTimeGradient = (double) softScoreDelta / (double) softScoreTotal;
            timeGradient += softTimeGradient * softScoreTimeGradientWeight;
        }
        return timeGradient;
    }

}
