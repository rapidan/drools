package org.drools.planner.config.localsearch.decider.forager;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import org.drools.planner.core.localsearch.decider.forager.AcceptedForager;
import org.drools.planner.core.localsearch.decider.forager.Forager;
import org.drools.planner.core.localsearch.decider.forager.PickEarlyByScore;

/**
 * @author Geoffrey De Smet
 */
@XStreamAlias("forager")
public class ForagerConfig {

    private Forager forager = null; // TODO remove this and document extending ForagerConfig
    private Class<Forager> foragerClass = null;
    private ForagerType foragerType = null;

    protected Integer minimalAcceptedSelection = null;

    public Forager getForager() {
        return forager;
    }

    public void setForager(Forager forager) {
        this.forager = forager;
    }

    public Class<Forager> getForagerClass() {
        return foragerClass;
    }

    public void setForagerClass(Class<Forager> foragerClass) {
        this.foragerClass = foragerClass;
    }

    public ForagerType getForagerType() {
        return foragerType;
    }

    public void setForagerType(ForagerType foragerType) {
        this.foragerType = foragerType;
    }

    public Integer getMinimalAcceptedSelection() {
        return minimalAcceptedSelection;
    }

    public void setMinimalAcceptedSelection(Integer minimalAcceptedSelection) {
        this.minimalAcceptedSelection = minimalAcceptedSelection;
    }

    // ************************************************************************
    // Builder methods
    // ************************************************************************

    public Forager buildForager() {
        if (forager != null) {
            return forager;
        } else if (foragerClass != null) {
            try {
                return foragerClass.newInstance();
            } catch (InstantiationException e) {
                throw new IllegalArgumentException("foragerClass (" + foragerClass.getName()
                        + ") does not have a public no-arg constructor", e);
            } catch (IllegalAccessException e) {
                throw new IllegalArgumentException("foragerClass (" + foragerClass.getName()
                        + ") does not have a public no-arg constructor", e);
            }
        }
        PickEarlyByScore pickEarlyByScore;
        if (foragerType != null) {
            switch (foragerType) {
                case MAX_SCORE_OF_ALL:
                    pickEarlyByScore = PickEarlyByScore.NONE;
                    break;
                case FIRST_BEST_SCORE_IMPROVING:
                    pickEarlyByScore = PickEarlyByScore.FIRST_BEST_SCORE_IMPROVING;
                    break;
                case FIRST_LAST_STEP_SCORE_IMPROVING:
                    pickEarlyByScore = PickEarlyByScore.FIRST_LAST_STEP_SCORE_IMPROVING;
                    break;
                case FIRST_RANDOMLY_ACCEPTED:
                    pickEarlyByScore = PickEarlyByScore.NONE;
                    break;
                default:
                    throw new IllegalStateException("The foragerType (" + foragerType + ") is not implemented");
            }
        } else {
            pickEarlyByScore = PickEarlyByScore.NONE;
        }
        int minimalAcceptedSelection = (this.minimalAcceptedSelection == null)
                ? Integer.MAX_VALUE : this.minimalAcceptedSelection;

        return new AcceptedForager(pickEarlyByScore, minimalAcceptedSelection);
    }

    public void inherit(ForagerConfig inheritedConfig) {
        if (forager == null && foragerClass == null && foragerType == null && minimalAcceptedSelection == null) {
            forager = inheritedConfig.getForager();
            foragerClass = inheritedConfig.getForagerClass();
            foragerType = inheritedConfig.getForagerType();
            minimalAcceptedSelection = inheritedConfig.getMinimalAcceptedSelection();
        }
    }

    public static enum ForagerType {
        MAX_SCORE_OF_ALL,
        FIRST_BEST_SCORE_IMPROVING,
        FIRST_LAST_STEP_SCORE_IMPROVING,
        FIRST_RANDOMLY_ACCEPTED
    }

}
