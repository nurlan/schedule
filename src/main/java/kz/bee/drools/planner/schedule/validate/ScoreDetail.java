package kz.bee.drools.planner.schedule.validate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import kz.bee.drools.planner.schedule.domain.Lesson;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.drools.planner.core.score.constraint.ConstraintOccurrence;
import org.drools.planner.core.score.constraint.ConstraintType;
import org.drools.planner.core.score.constraint.DoubleConstraintOccurrence;
import org.drools.planner.core.score.constraint.IntConstraintOccurrence;
import org.drools.planner.core.score.constraint.LongConstraintOccurrence;
import org.drools.planner.core.score.constraint.UnweightedConstraintOccurrence;

/**
 * TODO Replace this class with the ConstraintOccurrenceTotal class: https://jira.jboss.org/jira/browse/JBRULES-2510
 */
public class ScoreDetail implements Comparable<ScoreDetail> {

    private String ruleId;
    private ConstraintType constraintType;

    private Set<ConstraintOccurrence> constraintOccurrenceSet = new HashSet<ConstraintOccurrence>();
    private double scoreTotal = 0.0;

    public ScoreDetail(String ruleId, ConstraintType constraintType) {
        this.ruleId = ruleId;
        this.constraintType = constraintType;
    }

    public String getRuleId() {
        return ruleId;
    }

    public ConstraintType getConstraintType() {
        return constraintType;
    }

    public Set<ConstraintOccurrence> getConstraintOccurrenceSet() {
        return constraintOccurrenceSet;
    }

    public int getOccurrenceSize() {
        return constraintOccurrenceSet.size();
    }

    public double getScoreTotal() {
        return scoreTotal;
    }

    public void addConstraintOccurrence(ConstraintOccurrence constraintOccurrence) {
        boolean added = constraintOccurrenceSet.add(constraintOccurrence);
        if (!added) {
            throw new IllegalArgumentException("Add the same constraintOccurrence (" + constraintOccurrence
                    + ") twice.");
        }
        double occurrenceScore;
        if (constraintOccurrence instanceof IntConstraintOccurrence) {
            occurrenceScore = ((IntConstraintOccurrence) constraintOccurrence).getWeight();
        } else if (constraintOccurrence instanceof DoubleConstraintOccurrence) {
            occurrenceScore = ((DoubleConstraintOccurrence) constraintOccurrence).getWeight();
        } else if (constraintOccurrence instanceof LongConstraintOccurrence) {
            occurrenceScore = ((LongConstraintOccurrence) constraintOccurrence).getWeight();
        } else if (constraintOccurrence instanceof UnweightedConstraintOccurrence) {
            occurrenceScore = 1.0;
        } else {
            throw new IllegalStateException("Cannot determine occurrenceScore of ConstraintOccurrence class: "
                    + constraintOccurrence.getClass());
        }
        scoreTotal += occurrenceScore;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o instanceof ScoreDetail) {
            ScoreDetail other = (ScoreDetail) o;
            return new EqualsBuilder()
                    .append(ruleId, other.ruleId)
                    .append(constraintType, other.constraintType)
                    .isEquals();
        } else {
            return false;
        }
    }

    public int hashCode() {
        return new HashCodeBuilder()
                .append(ruleId)
                .append(constraintType)
                .toHashCode();
    }

    public int compareTo(ScoreDetail other) {
        return new CompareToBuilder()
                .append(constraintType, other.constraintType)
                .append(ruleId, other.ruleId)
                .toComparison();
    }

    public String toString() {
        return ruleId + "/" + constraintType + " (" + getOccurrenceSize() + ") = " + scoreTotal;
    }

    public List<Object[]> buildConstraintOccurrenceList() {
        List<ConstraintOccurrence> constraintOccurrenceList = new ArrayList(constraintOccurrenceSet);
        Collections.sort(constraintOccurrenceList);
        List<Object[]> list = new ArrayList<Object[]>();
        for (ConstraintOccurrence constraintOccurrence : constraintOccurrenceList) {
        	Object[] object = new Object[4];
        	object[0] = constraintOccurrence.getRuleId();
        	object[1] = constraintOccurrence.getConstraintType().toString();
        	object[2] = ((Lesson)((Object[])constraintOccurrence.getCauses())[0]).getId();
        	object[3] = ((constraintOccurrence.getCauses().length>1)?((Lesson)((Object[])constraintOccurrence.getCauses())[1]).getId():-1);
        	list.add(object);
        }
        return list;
    }
    
    public List<Object[]> buildHardConstraintOccurrenceList() {
        List<ConstraintOccurrence> constraintOccurrenceList = new ArrayList(constraintOccurrenceSet);
        Collections.sort(constraintOccurrenceList);
        List<Object[]> list = new ArrayList<Object[]>();
        for (ConstraintOccurrence constraintOccurrence : constraintOccurrenceList) {
        	if(constraintOccurrence.getConstraintType().equals("NEGATIVE_HARD")) {
	        	Object[] object = new Object[4];
	        	object[0] = constraintOccurrence.getRuleId();
	        	object[1] = constraintOccurrence.getConstraintType().toString();
	        	object[2] = ((Lesson)((Object[])constraintOccurrence.getCauses())[0]).getId();
	        	object[3] = ((constraintOccurrence.getCauses().length>1)?((Lesson)((Object[])constraintOccurrence.getCauses())[1]).getId():-1);
	        	list.add(object);
        	}
        }
        return list;
    }

    public String buildConstraintOccurrenceListText() {
        List<ConstraintOccurrence> constraintOccurrenceList = new ArrayList(constraintOccurrenceSet);
        Collections.sort(constraintOccurrenceList);
        StringBuilder text = new StringBuilder(constraintOccurrenceList.size() * 80);
        for (ConstraintOccurrence constraintOccurrence : constraintOccurrenceList) {
            text.append(constraintOccurrence.toString()).append("\n");
        }
        return text.toString();
    }
}
