package kz.bee.drools.planner.schedule.domain;

import java.io.Serializable;
import java.util.List;

import org.apache.commons.lang.builder.CompareToBuilder;

/**
 * @author Nurlan Rakhimzhanov
 * 
 */
public class Class implements Serializable, Comparable<Class> {

	private Long id;
//	private List<Long> studentList;
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	
//	public List<Long> getStudentList() {
//		return studentList;
//	}
//	public void setStudentList(List<Long> studentList) {
//		this.studentList = studentList;
//	}
	
	@Override
	public String toString() {
		return "Class [id=" + id + "]";//, studentList=" + studentList + "]";
	}
	
	public int compareTo(Class other) {
		return new CompareToBuilder()
			.append(id, other.id)
//			.append(studentList, other.studentList)
			.toComparison();
	}
	
}
