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

package kz.bee.drools.planner.schedule.solver.move;

import kz.bee.drools.planner.schedule.domain.Lesson;
import kz.bee.drools.planner.schedule.domain.Period;
import kz.bee.drools.planner.schedule.domain.Room;

import org.drools.WorkingMemory;
import org.drools.FactHandle;

public class CurriculumCourseMoveHelper {

    public static void movePeriod(WorkingMemory workingMemory, Lesson lesson, Period period) {
        FactHandle factHandle = workingMemory.getFactHandle(lesson);
        lesson.setPeriod(period);
        workingMemory.update(factHandle, lesson);
    }

    public static void moveRoom(WorkingMemory workingMemory, Lesson lesson, Room room) {
        FactHandle factHandle = workingMemory.getFactHandle(lesson);
        lesson.setRoom(room);
        workingMemory.update(factHandle, lesson);
    }

    public static void moveLesson(WorkingMemory workingMemory, Lesson lesson, Period period, Room room) {
        FactHandle factHandle = workingMemory.getFactHandle(lesson);
        lesson.setPeriod(period);
        lesson.setRoom(room);
        workingMemory.update(factHandle, lesson);
    }

    private CurriculumCourseMoveHelper() {
    }

}
