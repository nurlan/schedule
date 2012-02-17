package kz.bee.drools.planner.schedule.domain;

import java.io.Serializable;

import org.apache.commons.lang.builder.CompareToBuilder;

/**
 * @author Nurlan Rakhimzhanov
 * 
 */
public class Period implements Serializable, Comparable<Period> {
	
	private Long id;
	private Day day;
	private Time time;
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	
	public Day getDay() {
		return day;
	}
	public void setDay(Day day) {
		this.day = day;
	}
	
	public Time getTime() {
		return time;
	}
	public void setTime(Time time) {
		this.time = time;
	}
	
	@Override
	public String toString() {
		return "Period [id=" + id + ", day=" + day + ", time=" + time + "]";
	}
	
	public int compareTo(Period other) {
		return new CompareToBuilder()
			.append(id, other.id)
			.append(day, other.day)
			.append(time, other.time)
			.toComparison();
	}
	
}
