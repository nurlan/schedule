package kz.bee.drools.planner.schedule.domain.solver;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.drools.planner.api.domain.entity.PlanningEntityDifficultyWeightFactory;
import org.drools.planner.core.solution.Solution;
import kz.bee.drools.planner.schedule.domain.Course;
import kz.bee.drools.planner.schedule.solution.Schedule;
import kz.bee.drools.planner.schedule.domain.Lesson;
import kz.bee.drools.planner.schedule.domain.UnavailablePeriodConstraint;

public class LessonDifficultyWeightFactory implements PlanningEntityDifficultyWeightFactory {

    public Comparable createDifficultyWeight(Solution solution, Object planningEntity) {
        Schedule schedule = (Schedule) solution;
        Lesson lesson = (Lesson) planningEntity;
        Course course = lesson.getCourse();
        int unavailablePeriodConstraintCount = 0;
        for (UnavailablePeriodConstraint constraint : schedule.getUnavailablePeriodConstraintList()) {
            if (constraint.getCourse().equals(course)) {
                unavailablePeriodConstraintCount++;
            }
        }
        return new LectureDifficultyWeight(lesson, unavailablePeriodConstraintCount);
    }

    public static class LectureDifficultyWeight implements Comparable<LectureDifficultyWeight> {

        private final Lesson lesson;
        private final int unavailablePeriodConstraintCount;

        public LectureDifficultyWeight(Lesson lesson, int unavailablePeriodConstraintCount) {
            this.lesson = lesson;
            this.unavailablePeriodConstraintCount = unavailablePeriodConstraintCount;
        }

        public int compareTo(LectureDifficultyWeight other) {
            Course course = lesson.getCourse();
            Course otherCourse = other.lesson.getCourse();
            return new CompareToBuilder()
                    .append(unavailablePeriodConstraintCount, other.unavailablePeriodConstraintCount)
                    .append(course.getLessonCount(), otherCourse.getLessonCount())
//                    .append(course.getMinWorkingDaySize(), otherCourse.getMinWorkingDaySize())
                    .append(lesson.getId(), other.lesson.getId())
                    .toComparison();
        }

    }

}
