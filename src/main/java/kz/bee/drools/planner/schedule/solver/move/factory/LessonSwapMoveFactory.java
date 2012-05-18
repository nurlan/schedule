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
        List<Lesson> lessonList = schedule.getLessonList();
        List<Move> moveList = new ArrayList<Move>();
        for (ListIterator<Lesson> leftIt = lessonList.listIterator(); leftIt.hasNext();) {
            Lesson leftLesson = leftIt.next();
            for (ListIterator<Lesson> rightIt = lessonList.listIterator(leftIt.nextIndex()); rightIt.hasNext();) {
                Lesson rightLesson = rightIt.next();
                if (!leftLesson.getCourse().equals(rightLesson.getCourse()) && !leftLesson.isPinned() && !rightLesson.isPinned()) { 
                    moveList.add(new LessonSwapMove(leftLesson, rightLesson));
                }
            }
        }
        return moveList;
    }

}
