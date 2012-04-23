package kz.bee.drools.planner.schedule.main;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import kz.bee.drools.planner.schedule.domain.Class;
import kz.bee.drools.planner.schedule.domain.Lesson;
import kz.bee.drools.planner.schedule.domain.Period;
import kz.bee.drools.planner.schedule.domain.Room;
import kz.bee.drools.planner.schedule.solution.Schedule;

import org.drools.ClassObjectFilter;
import org.drools.WorkingMemory;
import org.drools.planner.config.SolverFactory;
import org.drools.planner.config.XmlSolverFactory;
import org.drools.planner.core.Solver;
import org.drools.planner.core.event.BestSolutionChangedEvent;
import org.drools.planner.core.event.SolverEventListener;
import org.drools.planner.core.score.constraint.ConstraintOccurrence;
import org.drools.planner.core.score.director.ScoreDirector;
import org.drools.planner.core.score.director.drools.DroolsScoreDirector;
import org.drools.planner.core.solution.Solution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ScheduleMain {

	public static final String SOLVER_CONFIG = "/scheduleSolverConfig2.xml";

	protected final transient Logger logger = LoggerFactory.getLogger(getClass());
	 
	private volatile Solver solver;
	private ScoreDirector scoreDirector;
	
	public ScheduleMain() {
		init();
	}
	
	private void init() {
		SolverFactory solverFactory = new XmlSolverFactory(SOLVER_CONFIG);
		solver = solverFactory.buildSolver();
		scoreDirector = solver.getScoreDirectorFactory().buildScoreDirector();
		
		this.solver.addEventListener( new SolverEventListener() {
		    public void bestSolutionChanged(BestSolutionChangedEvent event) {
		        Schedule schedule = (Schedule) solver.getBestSolution();
		        System.out.println( "Score: " + schedule.getScore() + ", Time: " + ScheduleMain.this.solver.getTimeMillisSpend() );
		        //print(schedule);
		    }
		
		});
	}
	
	public void start() {
		System.out.println("Start solving 2 ...");
		
		this.solver.solve();
		
		Schedule schedule = (Schedule) solver.getBestSolution();
		this.scoreDirector.setWorkingSolution(schedule);
		
		System.out.println( "Score: " + schedule.getScore() + ", Time: " + this.solver.getTimeMillisSpend() );
        print(schedule);
        
        System.out.println("End solving ...");
        
    	System.out.println( "getScoreDetailList size:" + getScoreDetailList().size());
		System.out.println( "Broken constrains list:" );
		
		for(ScoreDetail sd : getScoreDetailList()) {
			System.out.println(sd);
		}
		
		System.out.println("THE END2");
	}
	
	public void setPlanningProblem(Solution solution) {
		this.scoreDirector.setWorkingSolution(solution);
		this.solver.setPlanningProblem(this.scoreDirector.getWorkingSolution());
	}
	
	private void print( Schedule schedule ) {
		List<Period> periodList = schedule.getPeriodList();
		List<Class> clazzList = schedule.getClazzList();
		List<Lesson> lessonList = schedule.getLessonList();
		List<Room> roomList = schedule.getRoomList();
		
		String [][]table = new String[periodList.size()][roomList.size()];
		
		for(int i = 0; i < periodList.size(); i++) {
			for(int j = 0; j < roomList.size(); j++) {
//			for(int j = 0; j < clazzList.size(); j++) {
				for( Lesson l : lessonList ) {
					if( periodList.get(i) == l.getPeriod() && roomList.get(j) == l.getRoom() ) {
						if(table[i][j] == null) table[i][j] = "";
						table[i][j] += "Course [#" + l.getCourse().getId() + "]<br/>Teacher[" + l.getCourse().getTeacher().getId() + "]<br/>Class[" 
													+ l.getCourse().getClazz().getId()+", level=" + l.getCourse().getClazz().geteLevel() + "];";
					}
				}
			}
		}
		
		String htmlTable = "<html><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\"></head><body><table border='1'><tr><td>Period\\Rooms</td>";
		
		for(int i = 0; i < roomList.size(); i++ ) {
			htmlTable += "<td> Room[" + roomList.get(i).getNumber()+"]</td>";
		}
		
		htmlTable += "</tr>";
		
		for( int i = 0; i < table.length; i++) {
			htmlTable += "<tr><td>" + periodList.get(i).getDay().getValue() + " : " + periodList.get(i).getTime().getValue() + "</td>";
			
			for(int j = 0; j < table[i].length; j++) {
				htmlTable += "<td>" + table[i][j] + "</td>";
			}
			htmlTable += "</tr>";
		}
		
		htmlTable += "</table></body></html>";
		System.out.println("===========================================================");
		for(Lesson l : lessonList) {
			System.out.println(l);
		}
		System.out.println("===========================================================");
		
		try {
			FileWriter fstream = new FileWriter("/Users/nurlan/Dev/diploma/timetable.html");
			BufferedWriter out = new BufferedWriter(fstream);
			out.write(htmlTable);
			out.close();
		} catch (Exception e) {
			System.err.println("Error: " + e.getMessage());
		}
		
		System.out.println( "Score: " + schedule.getScore() + ", Time: " + this.solver.getTimeMillisSpend() );
		System.out.println(htmlTable);
	}
	
	public List<ScoreDetail> getScoreDetailList() {
        if (!(scoreDirector instanceof DroolsScoreDirector)) {
        	System.out.println("if (!(scoreDirector instanceof DroolsScoreDirector))");
            return null;
        }
        Map<String, ScoreDetail> scoreDetailMap = new HashMap<String, ScoreDetail>();
        WorkingMemory workingMemory = ((DroolsScoreDirector) scoreDirector).getWorkingMemory();
        if (workingMemory == null) {
        	System.out.println("if (workingMemory == null)");
            return Collections.emptyList();
        }
        Iterator<ConstraintOccurrence> it = (Iterator<ConstraintOccurrence>) workingMemory.iterateObjects(
                new ClassObjectFilter(ConstraintOccurrence.class));
        
        while (it.hasNext()) {
        	System.out.println("it.hasNext()");
            ConstraintOccurrence constraintOccurrence = it.next();
            ScoreDetail scoreDetail = scoreDetailMap.get(constraintOccurrence.getRuleId());
            if (scoreDetail == null) {
            	System.out.println("if (scoreDetail == null)");
                scoreDetail = new ScoreDetail(constraintOccurrence.getRuleId(), constraintOccurrence.getConstraintType());
                scoreDetailMap.put(constraintOccurrence.getRuleId(), scoreDetail);
            }
            scoreDetail.addConstraintOccurrence(constraintOccurrence);
        }
        
        List<ScoreDetail> scoreDetailList = new ArrayList<ScoreDetail>(scoreDetailMap.values());
        System.out.println("scoreDetailList.size():" + scoreDetailList.size());
        Collections.sort(scoreDetailList);
        System.out.println("end of getScoreDetailList() method.");
        
        return scoreDetailList;
    }
}
