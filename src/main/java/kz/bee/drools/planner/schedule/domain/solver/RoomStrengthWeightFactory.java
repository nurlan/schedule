package kz.bee.drools.planner.schedule.domain.solver;

import kz.bee.drools.planner.schedule.domain.Room;
import kz.bee.drools.planner.schedule.solution.Schedule;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.drools.planner.api.domain.variable.PlanningValueStrengthWeightFactory;
import org.drools.planner.core.solution.Solution;

public class RoomStrengthWeightFactory implements PlanningValueStrengthWeightFactory {

    public Comparable createStrengthWeight(Solution solution, Object planningValue) {
        Schedule schedule = (Schedule) solution;
        Room room = (Room) planningValue;
        return new RoomStrengthWeight(room);
    }

    public static class RoomStrengthWeight implements Comparable<RoomStrengthWeight> {

        private final Room room;

        public RoomStrengthWeight(Room room) {
            this.room = room;
        }

        public int compareTo(RoomStrengthWeight other) {
            return new CompareToBuilder()
//                    .append(room.getCapacity(), other.room.getCapacity())
                    .append(room.getType(), other.room.getType())
                    .append(room.getId(), other.room.getId())
                    .toComparison();
        }

    }

}
