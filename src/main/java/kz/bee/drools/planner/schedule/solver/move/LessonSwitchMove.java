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

import java.util.Arrays;
import java.util.Collection;

import kz.bee.drools.planner.schedule.domain.Lesson;
import kz.bee.drools.planner.schedule.domain.Period;
import kz.bee.drools.planner.schedule.domain.Room;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.drools.WorkingMemory;
import org.drools.planner.core.localsearch.decider.acceptor.tabu.TabuPropertyEnabled;
import org.drools.planner.core.move.Move;

public class LessonSwitchMove implements Move, TabuPropertyEnabled {

    private Lesson leftLesson;
    private Lesson rightLesson;

    public LessonSwitchMove(Lesson leftLesson, Lesson rightLesson) {
        this.leftLesson = leftLesson;
        this.rightLesson = rightLesson;
    }

    public boolean isMoveDoable(WorkingMemory workingMemory) {
        return !(ObjectUtils.equals(leftLesson.getPeriod(), rightLesson.getPeriod())
                && ObjectUtils.equals(leftLesson.getRoom(), rightLesson.getRoom()));
    }

    public Move createUndoMove(WorkingMemory workingMemory) {
        return new LessonSwitchMove(rightLesson, leftLesson);
    }

    public void doMove(WorkingMemory workingMemory) {
        Period oldLeftPeriod = leftLesson.getPeriod();
        Period oldRightPeriod = rightLesson.getPeriod();
        Room oldLeftRoom = leftLesson.getRoom();
        Room oldRightRoom = rightLesson.getRoom();
        if (oldLeftPeriod.equals(oldRightPeriod)) {
            CurriculumCourseMoveHelper.moveRoom(workingMemory, leftLesson, oldRightRoom);
            CurriculumCourseMoveHelper.moveRoom(workingMemory, rightLesson, oldLeftRoom);
        } else if (oldLeftRoom.equals(oldRightRoom)) {
            CurriculumCourseMoveHelper.movePeriod(workingMemory, leftLesson, oldRightPeriod);
            CurriculumCourseMoveHelper.movePeriod(workingMemory, rightLesson, oldLeftPeriod);
        } else {
            CurriculumCourseMoveHelper.moveLesson(workingMemory, leftLesson, oldRightPeriod, oldRightRoom);
            CurriculumCourseMoveHelper.moveLesson(workingMemory, rightLesson, oldLeftPeriod, oldLeftRoom);
        }
    }

    public Collection<? extends Object> getTabuProperties() {
        return Arrays.<Lesson>asList(leftLesson, rightLesson);
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o instanceof LessonSwitchMove) {
            LessonSwitchMove other = (LessonSwitchMove) o;
            return new EqualsBuilder()
                    .append(leftLesson, other.leftLesson)
                    .append(rightLesson, other.rightLesson)
                    .isEquals();
        } else {
            return false;
        }
    }

    public int hashCode() {
        return new HashCodeBuilder()
                .append(leftLesson)
                .append(rightLesson)
                .toHashCode();
    }

    public String toString() {
        return leftLesson + " <=> " + rightLesson;
    }

}
