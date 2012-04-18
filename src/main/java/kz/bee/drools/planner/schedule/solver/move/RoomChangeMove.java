package kz.bee.drools.planner.schedule.solver.move;

import java.util.Collection;
import java.util.Collections;

import kz.bee.drools.planner.schedule.domain.Lesson;
import kz.bee.drools.planner.schedule.domain.Room;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.drools.FactHandle;
import org.drools.WorkingMemory;
import org.drools.planner.core.localsearch.decider.acceptor.tabu.TabuPropertyEnabled;
import org.drools.planner.core.move.Move;

public class RoomChangeMove implements Move, TabuPropertyEnabled{

	private Lesson lesson;
	private Room toRoom;
	
	public RoomChangeMove(Lesson lesson, Room toRoom) {
		this.lesson = lesson;
		this.toRoom = toRoom;
	}
	
	public Collection<? extends Object> getTabuProperties() {
		return Collections.singletonList(lesson);
	}

	public boolean isMoveDoable(WorkingMemory workingMemory) {
		return !ObjectUtils.equals(lesson.getRoom(), toRoom);
	}

	public Move createUndoMove(WorkingMemory workingMemory) {
		return new RoomChangeMove(lesson, lesson.getRoom());
	}

//	public void doMove(WorkingMemory workingMemory) {
//		FactHandle lessonHandle = workingMemory.getFactHandle(lesson);
//		lesson.setRoom(toRoom);
//		workingMemory.update(lessonHandle, lesson);
//	}

	public void doMove(WorkingMemory workingMemory) {
        CurriculumCourseMoveHelper.moveRoom(workingMemory, lesson, toRoom);
    }
	
	public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o instanceof RoomChangeMove) {
            RoomChangeMove other = (RoomChangeMove) o;
            return new EqualsBuilder()
                    .append(lesson, other.lesson)
                    .append(toRoom, other.toRoom)
                    .isEquals();
        } else {
            return false;
        }
    }

    public int hashCode() {
        return new HashCodeBuilder()
                .append(lesson)
                .append(toRoom)
                .toHashCode();
    }

	@Override
	public String toString() {
		return "RoomChangeMove [lesson=" + lesson + ", toRoom=" + toRoom + "]";
	}
    
}
