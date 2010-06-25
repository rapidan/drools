package org.drools.planner.examples.nurserostering.domain;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.drools.planner.examples.common.domain.AbstractPersistable;
import org.drools.planner.examples.nurserostering.domain.contract.Contract;

/**
 * @author Geoffrey De Smet
 */
@XStreamAlias("Assignment")
public class Assignment extends AbstractPersistable implements Comparable<Assignment> {

    private Shift shift;

    // Changed by moves, between score calculations.
    private Employee employee;

    public Shift getShift() {
        return shift;
    }

    public void setShift(Shift shift) {
        this.shift = shift;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public String getLabel() {
        return shift.getShiftType().getCode();
    }

    public int compareTo(Assignment other) {
        return new CompareToBuilder()
                .append(shift, other.shift)
                .append(employee, other.employee)
                .toComparison();
    }

    public Assignment clone() {
        Assignment clone = new Assignment();
        clone.id = id;
        clone.shift = shift;
        clone.employee = employee;
        return clone;
    }

    /**
     * The normal methods {@link #equals(Object)} and {@link #hashCode()} cannot be used because the rule engine already
     * requires them (for performance in their original state).
     * @see #solutionHashCode()
     */
    public boolean solutionEquals(Object o) {
        if (this == o) {
            return true;
        } else if (o instanceof Assignment) {
            Assignment other = (Assignment) o;
            return new EqualsBuilder()
                    .append(id, other.id)
                    .append(shift, other.shift)
                    .append(employee, other.employee)
                    .isEquals();
        } else {
            return false;
        }
    }

    /**
     * The normal methods {@link #equals(Object)} and {@link #hashCode()} cannot be used because the rule engine already
     * requires them (for performance in their original state).
     * @see #solutionEquals(Object)
     */
    public int solutionHashCode() {
        return new HashCodeBuilder()
                .append(id)
                .append(shift)
                .append(shift)
                .toHashCode();
    }

    @Override
    public String toString() {
        return shift + "->" + employee;
    }

    public ShiftDate getShiftDate() {
        return shift.getShiftDate();
    }

    public ShiftType getShiftType() {
        return shift.getShiftType();
    }

    public int getShiftDateDayIndex() {
        return shift.getShiftDate().getDayIndex();
    }

    public DayOfWeek getShiftDateDayOfWeek() {
        return shift.getShiftDate().getDayOfWeek();
    }

    public Contract getContract() {
        return employee.getContract();
    }

    public boolean isWeekend() {
        WeekendDefinition weekendDefinition = employee.getContract().getWeekendDefinition();
        DayOfWeek dayOfWeek = shift.getShiftDate().getDayOfWeek();
        return weekendDefinition.isWeekend(dayOfWeek);
    }

    public int getWeekendSundayIndex() {
        return shift.getShiftDate().getWeekendSundayIndex();
    }

}
