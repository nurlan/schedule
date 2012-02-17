package kz.bee.drools.planner.schedule.domain;

import java.io.Serializable;

import org.apache.commons.lang.builder.CompareToBuilder;

/**
 * @author Nurlan Rakhimzhanov
 * 
 */
public class Time implements Serializable, Comparable<Time> {

	private Long id;
	private int value;
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public int getValue() {
		return value;
	}
	public void setValue(int value) {
		this.value = value;
	}
	
	@Override
	public String toString() {
		return "Time [id=" + id + ", value=" + value + "]";
	}
	
	public int compareTo(Time other) {
		return new CompareToBuilder()
	        .append(value, other.value)
	        .append(id, other.id)
	        .toComparison();
	}
}
