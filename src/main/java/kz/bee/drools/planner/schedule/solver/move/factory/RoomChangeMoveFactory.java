package kz.bee.drools.planner.schedule.solver.move.factory;

import java.util.ArrayList;
import java.util.List;

import kz.bee.drools.planner.schedule.domain.Lesson;
import kz.bee.drools.planner.schedule.domain.Room;
import kz.bee.drools.planner.schedule.solution.Schedule;
import kz.bee.drools.planner.schedule.solver.move.RoomChangeMove;

import org.drools.planner.core.move.Move;
import org.drools.planner.core.move.factory.CachedMoveFactory;
import org.drools.planner.core.solution.Solution;

public class RoomChangeMoveFactory extends CachedMoveFactory {

	@Override
	public List<Move> createCachedMoveList(Solution solution) {
		Schedule schedule = (Schedule) solution;
		List<Room> roomList = schedule.getRoomList();
        List<Move> moveList = new ArrayList<Move>();
        for (Lesson lesson : schedule.getLessonList()) {
            for (Room room : roomList) {
                moveList.add(new RoomChangeMove(lesson, room));
            }
        }
        return moveList;
	}
}
