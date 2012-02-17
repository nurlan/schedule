package kz.bee.drools.planner.schedule.solver.move;

import java.util.Collection;
import java.util.Collections;

import kz.bee.drools.planner.schedule.domain.Lesson;
import kz.bee.drools.planner.schedule.domain.Period;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.drools.FactHandle;
import org.drools.WorkingMemory;
import org.drools.planner.core.localsearch.decider.acceptor.tabu.TabuPropertyEnabled;
import org.drools.planner.core.move.Move;

public class PeriodChangeMove implements Move, TabuPropertyEnabled{

	private Lesson lesson;
	private Period toPeriod;
	
	public PeriodChangeMove(Lesson lesson, Period toPeriod) {
		this.lesson = lesson;
		this.toPeriod = toPeriod;
	}
	
	public Collection<? extends Object> getTabuProperties() {
		return Collections.singletonList(lesson);
	}

	public boolean isMoveDoable(WorkingMemory workingMemory) {
		return !ObjectUtils.equals(lesson.getPeriod(), toPeriod);
	}

	public Move createUndoMove(WorkingMemory workingMemory) {
		return new PeriodChangeMove(lesson, lesson.getPeriod());
	}

	public void doMove(WorkingMemory workingMemory) {
		FactHandle lessonHandle = workingMemory.getFactHandle(lesson);
		lesson.setPeriod(toPeriod);
		workingMemory.update(lessonHandle, lesson);
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
}
