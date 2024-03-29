package kz.bee.drools.planner.schedule.domain;

import java.io.Serializable;

import kz.bee.drools.planner.schedule.domain.solver.LessonDifficultyWeightFactory;
import kz.bee.drools.planner.schedule.domain.solver.PeriodStrengthWeightFactory;
import kz.bee.drools.planner.schedule.domain.solver.RoomStrengthWeightFactory;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.drools.planner.api.domain.entity.PlanningEntity;
import org.drools.planner.api.domain.variable.PlanningVariable;
import org.drools.planner.api.domain.variable.ValueRange;
import org.drools.planner.api.domain.variable.ValueRangeType;

/**
 * @author Nurlan Rakhimzhanov
 * 
 */
@PlanningEntity(difficultyWeightFactoryClass=LessonDifficultyWeightFactory.class)
public class Lesson implements Serializable, Comparable<Lesson>{

	/**
	 * 
	 */
	private static final long serialVersionUID = -6993119950253101764L;
	
	
	private Long id;
	private Course course;
	private Teacher teacher;
	private String lessonType;
	private int priority;
	private int timeValue;
	private boolean pinned;
	private String businessKey;
	
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

	public Teacher getTeacher() {
		return teacher;
	}

	public void setTeacher(Teacher teacher) {
		this.teacher = teacher;
	}

	@PlanningVariable(strengthWeightFactoryClass=PeriodStrengthWeightFactory.class)
	@ValueRange(type=ValueRangeType.FROM_SOLUTION_PROPERTY,solutionProperty="periodList")
	public Period getPeriod() {
		return period;
	}


	public void setPeriod(Period period) {
		this.period = period;
	}


	@PlanningVariable(strengthWeightFactoryClass=RoomStrengthWeightFactory.class)
	@ValueRange(type=ValueRangeType.FROM_SOLUTION_PROPERTY,solutionProperty="roomList")
	public Room getRoom() {
		return room;
	}

	public void setRoom(Room room) {
		this.room = room;
	}

	public String getLessonType() {
		return lessonType;
	}

	public void setLessonType(String lessonType) {
		this.lessonType = lessonType;
	}

	public int getPriority() {
		return priority;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}

	public int getTimeValue() {
		return timeValue;
	}

	public void setTimeValue(int timeValue) {
		this.timeValue = timeValue;
	}

	public boolean isPinned() {
		return pinned;
	}
	
	public void setPinned(boolean pinned) {
		this.pinned = pinned;
	}

	
	public String getBusinessKey() {
		return businessKey;
	}

	public void setBusinessKey(String businessKey) {
		this.businessKey = businessKey;
	}

	
	public Lesson clone() {
		Lesson lesson = new Lesson();
		
		lesson.setId(id);
		lesson.setCourse(course);
		lesson.setTeacher(teacher);
		lesson.setLessonType(lessonType);
		lesson.setPriority(priority);
		lesson.setTimeValue(timeValue);
		lesson.setPinned(pinned);
		lesson.setBusinessKey(businessKey);
		lesson.setPeriod(period);
		lesson.setRoom(room);
		
		return lesson;
	}
	
	public int compareTo(Lesson other) {
		return new CompareToBuilder()
			.append(id, other.id)
			.append(course, other.course)
			.append(teacher, other.teacher)
			.append(lessonType, other.lessonType)
			.append(priority, other.priority)
			.append(timeValue, other.timeValue)
			.append(pinned, other.pinned)
			.append(businessKey, other.businessKey)
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
                    .append(teacher, other.teacher)
                    .append(lessonType, other.lessonType)
                    .append(priority, other.priority)
                    .append(timeValue, other.timeValue)
                    .append(pinned, other.pinned)
                    .append(businessKey, other.businessKey)
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
                .append(teacher)
                .append(lessonType)
                .append(priority)
                .append(timeValue)
                .append(pinned)
                .append(businessKey)
				.append(period)
				.append(room)
                .toHashCode();
    }

	@Override
	public String toString() {
		return "Lesson [id=" + id + ", course=" + course + ", teacher="
				+ teacher + ", lessonType=" + lessonType + ", priority="
				+ priority + ", timeValue=" + timeValue + ", pinned=" + pinned
				+ ", businessKey=" + businessKey + ", period=" + period
				+ ", room=" + room + "]";
	}

}
