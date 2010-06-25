package org.drools.planner.examples.nurserostering.domain.contract;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * @author Geoffrey De Smet
 */
@XStreamAlias("MinMaxContractLine")
public class MinMaxContractLine extends ContractLine {

    private boolean minimumEnabled;
    private int minimumValue;
    private int minimumWeight;

    private boolean maximumEnabled;
    private int maximumValue;
    private int maximumWeight;

    public boolean isMinimumEnabled() {
        return minimumEnabled;
    }

    public void setMinimumEnabled(boolean minimumEnabled) {
        this.minimumEnabled = minimumEnabled;
    }

    public int getMinimumValue() {
        return minimumValue;
    }

    public void setMinimumValue(int minimumValue) {
        this.minimumValue = minimumValue;
    }

    public int getMinimumWeight() {
        return minimumWeight;
    }

    public void setMinimumWeight(int minimumWeight) {
        this.minimumWeight = minimumWeight;
    }

    public boolean isMaximumEnabled() {
        return maximumEnabled;
    }

    public void setMaximumEnabled(boolean maximumEnabled) {
        this.maximumEnabled = maximumEnabled;
    }

    public int getMaximumValue() {
        return maximumValue;
    }

    public void setMaximumValue(int maximumValue) {
        this.maximumValue = maximumValue;
    }

    public int getMaximumWeight() {
        return maximumWeight;
    }

    public void setMaximumWeight(int maximumWeight) {
        this.maximumWeight = maximumWeight;
    }

    public boolean isEnabled() {
        return minimumEnabled || maximumEnabled;
    }

}
