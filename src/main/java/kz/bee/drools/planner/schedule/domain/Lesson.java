package kz.bee.drools.planner.schedule.domain;

import java.io.Serializable;

import kz.bee.drools.planner.schedule.domain.solver.LessonDifficultyWeightFactory;
import kz.bee.drools.planner.schedule.domain.solver.PeriodStrengthWeightFactory;
import kz.bee.drools.planner.schedule.domain.solver.PeriodStrengthWeightFactory.PeriodStrengthWeight;
import kz.bee.drools.planner.schedule.domain.solver.RoomStrengthWeightFactory;

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
@PlanningEntity(difficultyWeightFactoryClass=LessonDifficultyWeightFactory.class)
public class Lesson implements Serializable, Comparable<Lesson>{

	private Long id;
//	private Long courseId;
//	private String teacherId;
//	private Long classId;
	private Course course;
	private boolean pinned;
	
	private Period period;
	private Room room;
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

//	public Long getCourseId() {
//		return courseId;
//	}
//
//
//	public void setCourseId(Long courseId) {
//		this.courseId = courseId;
//	}
//
//	
//	public String getTeacherId() {
//		return teacherId;
//	}
//
//
//	public void setTeacherId(String teacherId) {
//		this.teacherId = teacherId;
//	}
//
//
//	public Long getClassId() {
//		return classId;
//	}
//
//
//	public void setClassId(Long classId) {
//		this.classId = classId;
//	}

	public Course getCourse() {
		return course;
	}


	public void setCourse(Course course) {
		this.course = course;
	}

	@PlanningVariable(strengthWeightFactoryClass=PeriodStrengthWeightFactory.class)
	@ValueRangeFromSolutionProperty(propertyName="periodList")
	public Period getPeriod() {
		return period;
	}


	public void setPeriod(Period period) {
		this.period = period;
	}


	@PlanningVariable(strengthWeightFactoryClass=RoomStrengthWeightFactory.class)
	@ValueRangeFromSolutionProperty(propertyName="roomList")
	public Room getRoom() {
		return room;
	}


	public void setRoom(Room room) {
		this.room = room;
	}


	public boolean isPinned() {
		return pinned;
	}
	
	public void setPinned(boolean pinned) {
		this.pinned = pinned;
	}

	public Lesson clone() {
		Lesson lesson = new Lesson();
		
		lesson.setId(id);
//		lesson.setCourseId(courseId);
//		lesson.setTeacherId(teacherId);
//		lesson.setClassId(classId);
		lesson.setCourse(course);
		lesson.setPeriod(period);
		lesson.setRoom(room);
		
		return lesson;
	}
	
	public int compareTo(Lesson other) {
		return new CompareToBuilder()
			.append(id, other.id)
//			.append(courseId, other.courseId)
//			.append(teacherId, other.teacherId)
//			.append(classId, other.classId)
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
//                    .append(courseId, other.courseId)
//                    .append(teacherId, other.teacherId)
//                    .append(classId, other.classId)
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
//                .append(courseId)
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
