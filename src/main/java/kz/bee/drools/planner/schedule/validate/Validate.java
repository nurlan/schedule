package kz.bee.drools.planner.schedule.validate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import kz.bee.drools.planner.schedule.solution.Schedule;

import org.drools.ClassObjectFilter;
import org.drools.WorkingMemory;
import org.drools.planner.core.Solver;
import org.drools.planner.core.score.Score;
import org.drools.planner.core.score.constraint.ConstraintOccurrence;
import org.drools.planner.core.score.director.ScoreDirector;
import org.drools.planner.core.score.director.drools.DroolsScoreDirector;

/**
 * @author nurlan
 *
 */
public class Validate {

	private Score score;
	private ScoreDirector scoreDirector;
	
	public Validate(ScoreDirector scoreDirector, Schedule solution) {
		this.scoreDirector = scoreDirector;
		this.scoreDirector.setWorkingSolution(solution);
		this.score = this.scoreDirector.calculateScore();
	}
	
	public Validate(ScoreDirector scoreDirector) {
		this.scoreDirector = scoreDirector;
		this.score = this.scoreDirector.calculateScore();
		System.out.println("score:" + score);
	}
	
	/*
	 * Returns list of all broken constraints(hard) for a given solution. (e.g.[[ruleId,constraintType,lessonId,lessonId], ...])
	 */
	public List<Object[]> validate() {
		List<Object[]> brokenConstraintList = new ArrayList<Object[]>();
		try {
			for(ScoreDetail sd : getScoreDetailList()) {
				brokenConstraintList.addAll(sd.buildHardConstraintOccurrenceList());
			}
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		
		return brokenConstraintList;
	}
	
	public List<ScoreDetail> getScoreDetailList() {
        if (!(scoreDirector instanceof DroolsScoreDirector)) {
            return null;
        }
        Map<String, ScoreDetail> scoreDetailMap = new HashMap<String, ScoreDetail>();
        WorkingMemory workingMemory = ((DroolsScoreDirector) scoreDirector).getWorkingMemory();
        if (workingMemory == null) {
            return Collections.emptyList();
        }
        Iterator<ConstraintOccurrence> it = (Iterator<ConstraintOccurrence>) workingMemory.iterateObjects(
                new ClassObjectFilter(ConstraintOccurrence.class));
        
        while (it.hasNext()) {
            ConstraintOccurrence constraintOccurrence = it.next();
            ScoreDetail scoreDetail = scoreDetailMap.get(constraintOccurrence.getRuleId());
            if (scoreDetail == null) {
                scoreDetail = new ScoreDetail(constraintOccurrence.getRuleId(), constraintOccurrence.getConstraintType());
                scoreDetailMap.put(constraintOccurrence.getRuleId(), scoreDetail);
            }
            scoreDetail.addConstraintOccurrence(constraintOccurrence);
        }
        
        List<ScoreDetail> scoreDetailList = new ArrayList<ScoreDetail>(scoreDetailMap.values());
        Collections.sort(scoreDetailList);
        
        return scoreDetailList;
    }
}
