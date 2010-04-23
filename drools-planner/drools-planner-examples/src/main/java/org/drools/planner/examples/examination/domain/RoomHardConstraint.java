package org.drools.planner.examples.examination.domain;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import org.apache.commons.lang.builder.CompareToBuilder;
import org.drools.planner.examples.common.domain.AbstractPersistable;

/**
 * @author Geoffrey De Smet
 */
@XStreamAlias("RoomHardConstraint")
public class RoomHardConstraint extends AbstractPersistable implements Comparable<RoomHardConstraint> {

    private RoomHardConstraintType roomHardConstraintType;
    private Topic topic;

    public RoomHardConstraintType getRoomHardConstraintType() {
        return roomHardConstraintType;
    }

    public void setRoomHardConstraintType(RoomHardConstraintType roomHardConstraintType) {
        this.roomHardConstraintType = roomHardConstraintType;
    }

    public Topic getTopic() {
        return topic;
    }

    public void setTopic(Topic topic) {
        this.topic = topic;
    }

    public int compareTo(RoomHardConstraint other) {
        return new CompareToBuilder()
                .append(roomHardConstraintType, other.roomHardConstraintType)
                .append(topic, other.topic)
                .append(id, other.id)
                .toComparison();
    }

    @Override
    public String toString() {
        return roomHardConstraintType + "@" + topic.getId();
    }

}
