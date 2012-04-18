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
import org.drools.planner.core.move.Move;
import org.drools.planner.core.score.director.ScoreDirector;

public class RoomChangeMove implements Move {

	private Lesson lesson;
    private Room toRoom;

    public RoomChangeMove(Lesson lecture, Room toRoom) {
        this.lesson = lecture;
        this.toRoom = toRoom;
    }

    public boolean isMoveDoable(ScoreDirector scoreDirector) {
        return !ObjectUtils.equals(lesson.getRoom(), toRoom);
    }

    public Move createUndoMove(ScoreDirector scoreDirector) {
        return new RoomChangeMove(lesson, lesson.getRoom());
    }

    public void doMove(ScoreDirector scoreDirector) {
        CurriculumCourseMoveHelper.moveRoom(scoreDirector, lesson, toRoom);
    }

    public Collection<? extends Object> getPlanningEntities() {
        return Collections.singletonList(lesson);
    }

    public Collection<? extends Object> getPlanningValues() {
        return Collections.singletonList(toRoom);
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

    public String toString() {
        return lesson + " => " + toRoom;
    }
    
}
