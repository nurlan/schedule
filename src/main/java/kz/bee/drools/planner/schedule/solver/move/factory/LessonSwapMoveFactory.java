package kz.bee.drools.planner.schedule.solver.move.factory;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import kz.bee.drools.planner.schedule.domain.Lesson;
import kz.bee.drools.planner.schedule.solution.Schedule;
import kz.bee.drools.planner.schedule.solver.move.LessonSwapMove;

import org.drools.planner.core.move.Move;
import org.drools.planner.core.move.factory.CachedMoveFactory;
import org.drools.planner.core.solution.Solution;

public class LessonSwapMoveFactory extends CachedMoveFactory {

    public List<Move> createCachedMoveList(Solution solution) {
        Schedule schedule = (Schedule) solution;
        List<Lesson> lectureList = schedule.getLessonList();
        List<Move> moveList = new ArrayList<Move>();
        for (ListIterator<Lesson> leftIt = lectureList.listIterator(); leftIt.hasNext();) {
            Lesson leftLecture = leftIt.next();
            for (ListIterator<Lesson> rightIt = lectureList.listIterator(leftIt.nextIndex()); rightIt.hasNext();) {
                Lesson rightLecture = rightIt.next();
                if (!leftLecture.getCourse().equals(rightLecture.getCourse())) {
                    moveList.add(new LessonSwapMove(leftLecture, rightLecture));
                }
            }
        }
        return moveList;
    }

}
