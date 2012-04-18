package kz.bee.drools.planner.schedule.solver.move;

import java.util.Collection;
import java.util.Collections;

import kz.bee.drools.planner.schedule.domain.Lesson;
import kz.bee.drools.planner.schedule.domain.Period;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.drools.planner.core.move.Move;
import org.drools.planner.core.score.director.ScoreDirector;

public class PeriodChangeMove implements Move {

	private Lesson lesson;
    private Period toPeriod;

    public PeriodChangeMove(Lesson lesson, Period toPeriod) {
        this.lesson = lesson;
        this.toPeriod = toPeriod;
    }

    public boolean isMoveDoable(ScoreDirector scoreDirector) {
        return !ObjectUtils.equals(lesson.getPeriod(), toPeriod);
    }

    public Move createUndoMove(ScoreDirector scoreDirector) {
        return new PeriodChangeMove(lesson, lesson.getPeriod());
    }

    public void doMove(ScoreDirector scoreDirector) {
        CurriculumCourseMoveHelper.movePeriod(scoreDirector, lesson, toPeriod);
    }

    public Collection<? extends Object> getPlanningEntities() {
        return Collections.singletonList(lesson);
    }

    public Collection<? extends Object> getPlanningValues() {
        return Collections.singletonList(toPeriod);
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o instanceof PeriodChangeMove) {
            PeriodChangeMove other = (PeriodChangeMove) o;
            return new EqualsBuilder()
                    .append(lesson, other.lesson)
                    .append(toPeriod, other.toPeriod)
                    .isEquals();
        } else {
            return false;
        }
    }

    public int hashCode() {
        return new HashCodeBuilder()
                .append(lesson)
                .append(toPeriod)
                .toHashCode();
    }

    public String toString() {
        return lesson + " => " + toPeriod;
    }
}
