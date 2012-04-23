package kz.bee.drools.planner.schedule.domain;

import org.apache.commons.lang.builder.CompareToBuilder;

public class UnavailablePeriodConstraint implements Comparable<UnavailablePeriodConstraint> {

	private Long id;
    private Course course;
    private Period period;

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

    public Period getPeriod() {
        return period;
    }

    public void setPeriod(Period period) {
        this.period = period;
    }

    public int compareTo(UnavailablePeriodConstraint other) {
        return new CompareToBuilder()
        		.append(id, other.id)
                .append(course, other.course)
                .append(period, other.period)
                .toComparison();
    }

    @Override
    public String toString() {
        return id + "@" + course + "@" + period;
    }

}
