package kz.bee.drools.planner.schedule.domain;

import java.io.Serializable;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.drools.planner.api.domain.entity.PlanningEntity;
import org.drools.planner.api.domain.variable.PlanningVariable;
import org.drools.planner.api.domain.variable.ValueRangeFromSolutionProperty;

/**
 * @author Nurlan Rakhimzhanov
 * 
 */
@PlanningEntity
public class Lesson implements Serializable, Comparable<Lesson>{

	private Long id;
	private Course course;
	
	private Period period;
	private Room room;
	
	public Long getId() {
		return id;
	}


	public void setId(Long id) {
		this.id = id;
	}


	public Course getCourse() {
		return course;
	}


	public void setCourse(Course course) {
		this.course = course;
	}


	@PlanningVariable
	@ValueRangeFromSolutionProperty(propertyName="periodList")
	public Period getPeriod() {
		return period;
	}


	public void setPeriod(Period period) {
		this.period = period;
	}


	@PlanningVariable
	@ValueRangeFromSolutionProperty(propertyName="roomList")
	public Room getRoom() {
		return room;
	}


	public void setRoom(Room room) {
		this.room = room;
	}


	public Lesson clone() {
		Lesson lesson = new Lesson();
		
		lesson.setId(id);
		lesson.setCourse(course);
		lesson.setPeriod(period);
		lesson.setRoom(room);
		
		return lesson;
	}
	
	public int compareTo(Lesson other) {
		return new CompareToBuilder()
			.append(id, other.id)
			.append(course, other.course)
			.append(period, other.period)
			.append(room, other.room)
			.toComparison();
	}

	
	/**
     * The normal methods {@link #equals(Object)} and {@link #hashCode()} cannot be used
     * because the rule engine already requires them (for performance in their original state).
     * @see #solutionHashCode()
     */
    public boolean solutionEquals(Object o) {
        if (this == o) {
            return true;
        } else if (o instanceof Lesson) {
        	Lesson other = (Lesson) o;
            return new EqualsBuilder()
                    .append(id, other.id)
                    .append(course, other.course)
					.append(period, other.period)
					.append(room, other.room)
                    .isEquals();
        } else {
            return false;
        }
    }

    /**
     * The normal methods {@link #equals(Object)} and {@link #hashCode()} cannot be used
     * because the rule engine already requires them (for performance in their original state).
     * @see #solutionEquals(Object)
     */
    public int solutionHashCode() {
        return new HashCodeBuilder()
                .append(id)
                .append(course)
				.append(period)
				.append(room)
                .toHashCode();
    }

	
    @Override
	public String toString() {
		return "Lesson [id=" + id + ", course=" + course + ", period=" + period
				+ ", room=" + room + "]";
	}	
}
