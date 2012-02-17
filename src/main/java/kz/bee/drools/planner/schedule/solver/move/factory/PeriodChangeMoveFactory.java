package kz.bee.drools.planner.schedule.solver.move.factory;

import java.util.ArrayList;
import java.util.List;

import kz.bee.drools.planner.schedule.domain.Lesson;
import kz.bee.drools.planner.schedule.domain.Period;
import kz.bee.drools.planner.schedule.solution.Schedule;
import kz.bee.drools.planner.schedule.solver.move.PeriodChangeMove;

import org.drools.planner.core.move.Move;
import org.drools.planner.core.move.factory.CachedMoveFactory;
import org.drools.planner.core.solution.Solution;

public class PeriodChangeMoveFactory extends CachedMoveFactory {

	@Override
	public List<Move> createCachedMoveList(Solution solution) {
		Schedule schedule = (Schedule) solution;
		List<Period> periodList = schedule.getPeriodList();
        List<Move> moveList = new ArrayList<Move>();
        for (Lesson lesson : schedule.getLessonList()) {
            for (Period period : periodList) {
                moveList.add(new PeriodChangeMove(lesson, period));
            }
        }
        return moveList;
	}

}
