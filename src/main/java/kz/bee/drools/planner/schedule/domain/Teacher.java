package kz.bee.drools.planner.schedule.domain;

import java.io.Serializable;

import org.apache.commons.lang.builder.CompareToBuilder;

/**
 * @author Nurlan Rakhimzhanov
 * 
 */
public class Teacher implements Serializable, Comparable<Teacher> {

	private Long id;
	private String name;
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return "Teacher [id=" + id + ", name=" + name + "]";
	}

	public int compareTo(Teacher other) {
		return new CompareToBuilder()
			.append(id, other.id)
			.append(name, other.name)
			.toComparison();
	}
	
}
