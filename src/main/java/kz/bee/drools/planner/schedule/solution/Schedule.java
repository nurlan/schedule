package kz.bee.drools.planner.schedule.solution;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import kz.bee.drools.planner.schedule.domain.Class;
import kz.bee.drools.planner.schedule.domain.Course;
import kz.bee.drools.planner.schedule.domain.Day;
import kz.bee.drools.planner.schedule.domain.Lesson;
import kz.bee.drools.planner.schedule.domain.Period;
import kz.bee.drools.planner.schedule.domain.Room;
import kz.bee.drools.planner.schedule.domain.Teacher;
import kz.bee.drools.planner.schedule.domain.Time;
import kz.bee.drools.planner.schedule.domain.UnavailablePeriodConstraint;

import org.apache.commons.lang.builder.HashCodeBuilder;
import org.drools.planner.api.domain.solution.PlanningEntityCollectionProperty;
//import org.drools.planner.core.score.SimpleScore;
import org.drools.planner.core.score.buildin.hardandsoft.HardAndSoftScore;
import org.drools.planner.core.score.buildin.simple.SimpleScore;
import org.drools.planner.core.solution.Solution;

/**
 * @author Nurlan Rakhimzhanov
 *
 */
public class Schedule implements Solution<HardAndSoftScore> {

	private Long id;
	private List<Course> courseList;
	private List<Class> clazzList;
	private List<Teacher> teacherList;
	private List<Room> roomList;
	private List<Period> periodList;
	private List<Day> dayList;
	private List<Time> timeList;
	private List<UnavailablePeriodConstraint> unavailablePeriodConstraintList;
	
	private List<Lesson> lessonList;
	
	private HardAndSoftScore score;
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public List<Course> getCourseList() {
		return courseList;
	}

	public void setCourseList(List<Course> courseList) {
		this.courseList = courseList;
	}

	public List<Class> getClazzList() {
		return clazzList;
	}

	public void setClazzList(List<Class> clazzList) {
		this.clazzList = clazzList;
	}

	public List<Teacher> getTeacherList() {
		return teacherList;
	}

	public void setTeacherList(List<Teacher> teacherList) {
		this.teacherList = teacherList;
	}

	public List<Room> getRoomList() {
		return roomList;
	}

	public void setRoomList(List<Room> roomList) {
		this.roomList = roomList;
	}

	public List<Period> getPeriodList() {
		return periodList;
	}

	public void setPeriodList(List<Period> periodList) {
		this.periodList = periodList;
	}

	public List<Day> getDayList() {
		return dayList;
	}

	public void setDayList(List<Day> dayList) {
		this.dayList = dayList;
	}

	public List<Time> getTimeList() {
		return timeList;
	}

	public void setTimeList(List<Time> timeList) {
		this.timeList = timeList;
	}
	

	public List<UnavailablePeriodConstraint> getUnavailablePeriodConstraintList() {
		return unavailablePeriodConstraintList;
	}

	public void setUnavailablePeriodConstraintList(
			List<UnavailablePeriodConstraint> unavailablePeriodConstraintList) {
		this.unavailablePeriodConstraintList = unavailablePeriodConstraintList;
	}

	@PlanningEntityCollectionProperty
	public List<Lesson> getLessonList() {
		return lessonList;
	}

	public void setLessonList(List<Lesson> lessonList) {
		this.lessonList = lessonList;
	}

	public HardAndSoftScore getScore() {
		return score;
	}

	public void setScore(HardAndSoftScore score) {
		this.score = score;
	}

	// ************************************************************************
    // Complex methods
    // ************************************************************************
	
	public Collection<? extends Object> getProblemFacts() {
		List<Object> facts =  new ArrayList<Object>();
		facts.addAll(courseList);
		facts.addAll(clazzList);
		facts.addAll(teacherList);
		facts.addAll(roomList);
		facts.addAll(periodList);
		facts.addAll(dayList);
		facts.addAll(timeList);
		facts.addAll(unavailablePeriodConstraintList);
		// Do not add the planning entity's (lessonList) because that will be done automatically
		return facts;
	}

	/**
     * Clone will only deep copy the {@link #lessonList}.
     */
	public Schedule cloneSolution() {
		Schedule clone = new Schedule();
		
		clone.id = id;
		clone.courseList = courseList; 
		clone.clazzList = clazzList;
		clone.teacherList = teacherList;
		clone.roomList = roomList;
		clone.periodList = periodList;
		clone.dayList = dayList;
		clone.timeList = timeList;
		clone.unavailablePeriodConstraintList = unavailablePeriodConstraintList;
		
		List<Lesson> clonedLessonList = new ArrayList<Lesson>(lessonList.size());
		for( Lesson l : lessonList ) {
			Lesson clonedLesson = l.clone(); 
			clonedLessonList.add(clonedLesson);
		}
		
		clone.lessonList = clonedLessonList;
		clone.score = score;
		
		return clone;
	}
	
	public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (id == null || !(o instanceof Schedule)) {
            return false;
        } else {
        	Schedule other = (Schedule) o;
            if (lessonList.size() != other.lessonList.size()) {
                return false;
            }
            for (Iterator<Lesson> it = lessonList.iterator(), otherIt = other.lessonList.iterator(); it.hasNext();) {
                Lesson lesson = it.next();
                Lesson otherLecture = otherIt.next();
                // Notice: we don't use equals()
                if (!lesson.solutionEquals(otherLecture)) {
                    return false;
                }
            }
            return true;
        }
    }
	
	public int hashCode() {
        HashCodeBuilder hashCodeBuilder = new HashCodeBuilder();
        for (Lesson lesson : lessonList) {
            // Notice: we don't use hashCode()
            hashCodeBuilder.append(lesson.solutionHashCode());
        }
        return hashCodeBuilder.toHashCode();
    }
}
