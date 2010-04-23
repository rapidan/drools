package org.drools.planner.examples.pas.domain;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import org.apache.commons.lang.builder.CompareToBuilder;
import org.drools.planner.examples.common.domain.AbstractPersistable;

/**
 * @author Geoffrey De Smet
 */
@XStreamAlias("DepartmentSpecialism")
public class DepartmentSpecialism extends AbstractPersistable implements Comparable<DepartmentSpecialism> {

    private Department department;
    private Specialism specialism;

    private int priority; // AKA choice

    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }

    public Specialism getSpecialism() {
        return specialism;
    }

    public void setSpecialism(Specialism specialism) {
        this.specialism = specialism;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public int compareTo(DepartmentSpecialism other) {
        return new CompareToBuilder()
                .append(department, other.department)
                .append(specialism, other.specialism)
                .append(id, other.id)
                .toComparison();
    }

    @Override
    public String toString() {
        return department + "-" + specialism;
    }

}