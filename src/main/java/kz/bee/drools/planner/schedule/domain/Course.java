package kz.bee.drools.planner.schedule.domain;

import java.io.Serializable;

import org.apache.commons.lang.builder.CompareToBuilder;

/**
 * @author Nurlan Rakhimzhanov
 * 
 */
public class Course implements Serializable, Comparable<Course> {

	private Long id;
	private int lessonCount;
	private Teacher teacher;
	private Class clazz;
	private Room room;
	
	public Long getId() {
		return id;
	}


	public void setId(Long id) {
		this.id = id;
	}


	public int getLessonCount() {
		return lessonCount;
	}


	public void setLessonCount(int lessonCount) {
		this.lessonCount = lessonCount;
	}


	public Teacher getTeacher() {
		return teacher;
	}


	public void setTeacher(Teacher teacher) {
		this.teacher = teacher;
	}


	public Class getClazz() {
		return clazz;
	}


	public void setClazz(Class clazz) {
		this.clazz = clazz;
	}
	

	public Room getRoom() {
		return room;
	}


	public void setRoom(Room room) {
		this.room = room;
	}


	@Override
	public String toString() {
		return "Course [id=" + id + ", lessonCount=" + lessonCount
				+ ", teacher=" + teacher + ", clazz=" + clazz + ", room="
				+ room + "]";
	}


	public int compareTo(Course other) {
		return new CompareToBuilder()
			.append(id, other.id)
			.append(lessonCount, other.lessonCount)
			.append(teacher, other.teacher)
			.append(clazz, other.clazz)
			.append(room, other.room)
			.toComparison();
	}
	
}
