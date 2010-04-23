package org.drools.planner.examples.curriculumcourse.domain;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import org.apache.commons.lang.builder.CompareToBuilder;
import org.drools.planner.examples.common.domain.AbstractPersistable;

/**
 * @author Geoffrey De Smet
 */
@XStreamAlias("Day")
public class Day extends AbstractPersistable implements Comparable<Day> {

    private int dayIndex;

    public int getDayIndex() {
        return dayIndex;
    }

    public void setDayIndex(int dayIndex) {
        this.dayIndex = dayIndex;
    }

    public int compareTo(Day other) {
        return new CompareToBuilder()
                .append(dayIndex, other.dayIndex)
                .toComparison();
    }

    @Override
    public String toString() {
        return Integer.toString(dayIndex);
    }

}