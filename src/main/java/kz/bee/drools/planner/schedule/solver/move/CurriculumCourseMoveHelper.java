package kz.bee.drools.planner.schedule.solver.move;

import kz.bee.drools.planner.schedule.domain.Lesson;
import kz.bee.drools.planner.schedule.domain.Period;
import kz.bee.drools.planner.schedule.domain.Room;

import org.drools.planner.core.score.director.ScoreDirector;

public class CurriculumCourseMoveHelper {

	public static void movePeriod(ScoreDirector scoreDirector, Lesson lesson, Period period) {
        scoreDirector.beforeVariableChanged(lesson, "period");
        lesson.setPeriod(period);
        scoreDirector.afterVariableChanged(lesson, "period");
    }

    public static void moveRoom(ScoreDirector scoreDirector, Lesson lesson, Room room) {
        scoreDirector.beforeVariableChanged(lesson, "room");
        lesson.setRoom(room);
        scoreDirector.afterVariableChanged(lesson, "room");
    }

    public static void moveLesson(ScoreDirector scoreDirector, Lesson lesson, Period period, Room room) {
        scoreDirector.beforeAllVariablesChanged(lesson);
        lesson.setPeriod(period);
        lesson.setRoom(room);
        scoreDirector.afterAllVariablesChanged(lesson);
    }

    private CurriculumCourseMoveHelper() {
    }

}
