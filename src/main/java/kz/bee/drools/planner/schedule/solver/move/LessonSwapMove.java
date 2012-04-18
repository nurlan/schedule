package kz.bee.drools.planner.schedule.solver.move;

import java.util.Arrays;
import java.util.Collection;

import kz.bee.drools.planner.schedule.domain.Lesson;
import kz.bee.drools.planner.schedule.domain.Period;
import kz.bee.drools.planner.schedule.domain.Room;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.drools.planner.core.move.Move;
import org.drools.planner.core.score.director.ScoreDirector;

public class LessonSwapMove implements Move {

    private Lesson leftLesson;
    private Lesson rightLesson;

    public LessonSwapMove(Lesson leftLesson, Lesson rightLesson) {
        this.leftLesson = leftLesson;
        this.rightLesson = rightLesson;
    }

    public boolean isMoveDoable(ScoreDirector scoreDirector) {
        return !(ObjectUtils.equals(leftLesson.getPeriod(), rightLesson.getPeriod())
                && ObjectUtils.equals(leftLesson.getRoom(), rightLesson.getRoom()));
    }

    public Move createUndoMove(ScoreDirector scoreDirector) {
        return new LessonSwapMove(rightLesson, leftLesson);
    }

    public void doMove(ScoreDirector scoreDirector) {
        Period oldLeftPeriod = leftLesson.getPeriod();
        Period oldRightPeriod = rightLesson.getPeriod();
        Room oldLeftRoom = leftLesson.getRoom();
        Room oldRightRoom = rightLesson.getRoom();
        if (oldLeftPeriod.equals(oldRightPeriod)) {
            CurriculumCourseMoveHelper.moveRoom(scoreDirector, leftLesson, oldRightRoom);
            CurriculumCourseMoveHelper.moveRoom(scoreDirector, rightLesson, oldLeftRoom);
        } else if (oldLeftRoom.equals(oldRightRoom)) {
            CurriculumCourseMoveHelper.movePeriod(scoreDirector, leftLesson, oldRightPeriod);
            CurriculumCourseMoveHelper.movePeriod(scoreDirector, rightLesson, oldLeftPeriod);
        } else {
            CurriculumCourseMoveHelper.moveLesson(scoreDirector, leftLesson, oldRightPeriod, oldRightRoom);
            CurriculumCourseMoveHelper.moveLesson(scoreDirector, rightLesson, oldLeftPeriod, oldLeftRoom);
        }
    }

    public Collection<? extends Object> getPlanningEntities() {
        return Arrays.asList(leftLesson, rightLesson);
    }

    public Collection<? extends Object> getPlanningValues() {
        return Arrays.<Object>asList(leftLesson.getPeriod(), leftLesson.getRoom(),
                rightLesson.getPeriod(), rightLesson.getRoom());
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o instanceof LessonSwapMove) {
            LessonSwapMove other = (LessonSwapMove) o;
            return new EqualsBuilder()
                    .append(leftLesson, other.leftLesson)
                    .append(rightLesson, other.rightLesson)
                    .isEquals();
        } else {
            return false;
        }
    }

    public int hashCode() {
        return new HashCodeBuilder()
                .append(leftLesson)
                .append(rightLesson)
                .toHashCode();
    }

    public String toString() {
        return leftLesson + " <=> " + rightLesson;
    }

}
