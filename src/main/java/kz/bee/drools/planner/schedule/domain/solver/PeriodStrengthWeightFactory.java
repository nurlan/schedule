package kz.bee.drools.planner.schedule.domain.solver;

import kz.bee.drools.planner.schedule.domain.Period;
import kz.bee.drools.planner.schedule.domain.UnavailablePeriodConstraint;
import kz.bee.drools.planner.schedule.solution.Schedule;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.drools.planner.api.domain.variable.PlanningValueStrengthWeightFactory;
import org.drools.planner.core.solution.Solution;

public class PeriodStrengthWeightFactory implements PlanningValueStrengthWeightFactory {

    public Comparable createStrengthWeight(Solution solution, Object planningValue) {
        Schedule schedule = (Schedule) solution;
        Period period = (Period) planningValue;
        int unavailablePeriodConstraintCount = 0;
        for (UnavailablePeriodConstraint constraint : schedule.getUnavailablePeriodConstraintList()) {
            if (constraint.getPeriod().equals(period)) {
                unavailablePeriodConstraintCount++;
            }
        }
        return new PeriodStrengthWeight(period, unavailablePeriodConstraintCount);
    }

    public static class PeriodStrengthWeight implements Comparable<PeriodStrengthWeight> {

        private final Period period;
        private final int unavailablePeriodConstraintCount;

        public PeriodStrengthWeight(Period period, int unavailablePeriodConstraintCount) {
            this.period = period;
            this.unavailablePeriodConstraintCount = unavailablePeriodConstraintCount;
        }

        public int compareTo(PeriodStrengthWeight other) {
            return new CompareToBuilder()
                    // The higher unavailablePeriodConstraintCount, the weaker
                    .append(other.unavailablePeriodConstraintCount, unavailablePeriodConstraintCount) // Descending
                    .append(period.getDay().getValue(), other.period.getDay().getValue())
                    .append(period.getTime().getValue(), other.period.getTime().getValue())
                    .append(period.getId(), other.period.getId())
                    .toComparison();
        }

    }

}
