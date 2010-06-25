package org.drools.planner.examples.nurserostering.domain.contract;

import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import org.apache.commons.lang.builder.CompareToBuilder;
import org.drools.planner.examples.common.domain.AbstractPersistable;
import org.drools.planner.examples.nurserostering.domain.WeekendDefinition;

/**
 * @author Geoffrey De Smet
 */
@XStreamAlias("Contract")
public class Contract extends AbstractPersistable implements Comparable<Contract> {

    private String code;
    private String description;
    private WeekendDefinition weekendDefinition;

    private List<ContractLine> contractLineList;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public WeekendDefinition getWeekendDefinition() {
        return weekendDefinition;
    }

    public void setWeekendDefinition(WeekendDefinition weekendDefinition) {
        this.weekendDefinition = weekendDefinition;
    }

    public List<ContractLine> getContractLineList() {
        return contractLineList;
    }

    public void setContractLineList(List<ContractLine> contractLineList) {
        this.contractLineList = contractLineList;
    }

    public int compareTo(Contract other) {
        return new CompareToBuilder()
                .append(id, other.id)
                .toComparison();
    }

    @Override
    public String toString() {
        return code;
    }

    public int getWeekendLength() {
        return weekendDefinition.getWeekendLength();
    }

}
