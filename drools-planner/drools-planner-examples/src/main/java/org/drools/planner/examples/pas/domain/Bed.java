package org.drools.planner.examples.pas.domain;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import org.apache.commons.lang.builder.CompareToBuilder;
import org.drools.planner.examples.common.domain.AbstractPersistable;

/**
 * @author Geoffrey De Smet
 */
@XStreamAlias("Bed")
public class Bed extends AbstractPersistable implements Comparable<Bed> {

    private Room room;
    private int indexInRoom;

    public Room getRoom() {
        return room;
    }

    public void setRoom(Room room) {
        this.room = room;
    }

    public int getIndexInRoom() {
        return indexInRoom;
    }

    public void setIndexInRoom(int indexInRoom) {
        this.indexInRoom = indexInRoom;
    }

    public int compareTo(Bed other) {
        return new CompareToBuilder()
                .append(room, other.room)
                .append(indexInRoom, other.indexInRoom)
                .append(id, other.id)
                .toComparison();
    }

    @Override
    public String toString() {
        return room + "_" + indexInRoom;
    }

}