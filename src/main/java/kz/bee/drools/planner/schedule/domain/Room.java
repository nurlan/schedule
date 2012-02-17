package kz.bee.drools.planner.schedule.domain;

import java.io.Serializable;

import org.apache.commons.lang.builder.CompareToBuilder;

/**
 * @author Nurlan Rakhimzhanov
 * 
 */
public class Room implements Serializable, Comparable<Room> {

	private Long id;
	private String number;
	
	public Long getId() {
		return id;
	}


	public void setId(Long id) {
		this.id = id;
	}


	public String getNumber() {
		return number;
	}


	public void setNumber(String number) {
		this.number = number;
	}


	@Override
	public String toString() {
		return "Room [id=" + id + ", number=" + number + "]";
	}


	public int compareTo(Room other) {
		return new CompareToBuilder()
			.append(id, other.id)
			.append(number, other.number)
			.toComparison();
	}
	
	
}
