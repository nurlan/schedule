package kz.bee.drools.planner.schedule.domain;

import java.io.Serializable;

import org.apache.commons.lang.builder.CompareToBuilder;

/**
 * @author Nurlan Rakhimzhanov
 * 
 */
public class Class implements Serializable, Comparable<Class> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2760804358431340961L;
	
	
	private Long id;
	private Room room;
	//private String ringGroup;
	private String wxGroupName;
	private int level;
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	
	public Room getRoom() {
		return room;
	}
	public void setRoom(Room room) {
		this.room = room;
	}
//	public String getRingGroup() {
//		return ringGroup;
//	}
//	public void setRingGroup(String ringGroup) {
//		this.ringGroup = ringGroup;
//	}

	public int getLevel() {
		return level;
	}
	public void setLevel(int level) {
		this.level = level;
	}
	
	public String getWxGroupName() {
		return wxGroupName;
	}
	
	public void setWxGroupName(String wxGroupName) {
		this.wxGroupName = wxGroupName;
	}


	@Override
	public String toString() {
		return "Class [id=" + id + ", room=" + room + ", wxGroupName="
				+ wxGroupName + ", level=" + level + "]";
	}

	public int compareTo(Class other) {
		return new CompareToBuilder()
			.append(id, other.id)
			.append(room, other.room)
//			.append(ringGroup, other.ringGroup)
			.append(wxGroupName, other.wxGroupName)
			.append(level, other.level)
			.toComparison();
	}
	
}
