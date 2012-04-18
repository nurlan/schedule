/*
 * Copyright 2010 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package kz.bee.drools.planner.schedule.solver.move.factory;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import kz.bee.drools.planner.schedule.domain.Lesson;
import kz.bee.drools.planner.schedule.solution.Schedule;
import kz.bee.drools.planner.schedule.solver.move.LessonSwitchMove;

import org.drools.planner.core.move.Move;
import org.drools.planner.core.move.factory.CachedMoveFactory;
import org.drools.planner.core.solution.Solution;

public class LessonSwitchMoveFactory extends CachedMoveFactory {

    public List<Move> createCachedMoveList(Solution solution) {
        Schedule schedule = (Schedule) solution;
        List<Lesson> lectureList = schedule.getLessonList();
        List<Move> moveList = new ArrayList<Move>();
        for (ListIterator<Lesson> leftIt = lectureList.listIterator(); leftIt.hasNext();) {
            Lesson leftLecture = leftIt.next();
            for (ListIterator<Lesson> rightIt = lectureList.listIterator(leftIt.nextIndex()); rightIt.hasNext();) {
                Lesson rightLecture = rightIt.next();
                if (!leftLecture.getCourse().equals(rightLecture.getCourse())) {
                    moveList.add(new LessonSwitchMove(leftLecture, rightLecture));
                }
            }
        }
        return moveList;
    }

}
