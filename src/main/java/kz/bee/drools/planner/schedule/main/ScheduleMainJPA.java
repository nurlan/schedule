package kz.bee.drools.planner.schedule.main;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import kz.bee.drools.planner.schedule.domain.Class;
import kz.bee.drools.planner.schedule.domain.Course;
import kz.bee.drools.planner.schedule.domain.Day;
import kz.bee.drools.planner.schedule.domain.Lesson;
import kz.bee.drools.planner.schedule.domain.Period;
import kz.bee.drools.planner.schedule.domain.Room;
import kz.bee.drools.planner.schedule.domain.Teacher;
import kz.bee.drools.planner.schedule.domain.Time;
import kz.bee.drools.planner.schedule.solution.Schedule;

import org.apache.log4j.xml.DOMConfigurator;
import org.drools.planner.config.XmlSolverConfigurer;
import org.drools.planner.core.Solver;
import org.drools.planner.core.event.BestSolutionChangedEvent;
import org.drools.planner.core.event.SolverEventListener;
import org.drools.planner.core.solution.Solution;

public class ScheduleMainJPA {

	public static final String SOLVER_CONFIG
	= "/scheduleSolverConfig.xml";

	private volatile Solver solver;
	
	public ScheduleMainJPA() {
		DOMConfigurator.configure(getClass().getResource("/log4j-test.xml"));
	}
	
	private void init() {
		XmlSolverConfigurer configurer = new XmlSolverConfigurer();
		configurer.configure(SOLVER_CONFIG);
		solver = configurer.buildSolver();
		this.solver.addEventListener( new SolverEventListener() {
		    public void bestSolutionChanged(BestSolutionChangedEvent event) {
		        Schedule schedule = (Schedule) solver.getBestSolution();
		        
		        //print(schedule);
		    }
		
		});
		setPlanningProblem();
	}
	
	private void start() {
		System.out.println("Start solving ...");
		this.solver.solve();
		
		Schedule schedule = (Schedule) solver.getBestSolution();
		System.out.println( "Score: " + schedule.getScore() + ", Time: " + this.solver.getTimeMillisSpend() );
        print(schedule);
        System.out.println("End solving ...");
	}
	
	private void setPlanningProblem() {
		List<Course> courseList = new ArrayList<Course>();
		List<Class> clazzList = new ArrayList<Class>();
		List<Teacher> teacherList = new ArrayList<Teacher>();
		List<Room> roomList = new ArrayList<Room>();
		List<Period> periodList = new ArrayList<Period>();
		List<Day> dayList = new ArrayList<Day>();
		List<Time> timeList = new ArrayList<Time>();
		List<Lesson> lessonList = new ArrayList<Lesson>();
		List<Long> studentList;
		
		EntityManagerFactory emf = Persistence.createEntityManagerFactory("slrs");
		EntityManager em = emf.createEntityManager();
		
		em.getTransaction().begin();
		
		for( int i = 1; i <= 3; i++ ) {
			Time t = new Time();
			t.setId(Long.valueOf(""+i));
			t.setValue(i);
			
			Day d = new Day();
			d.setId(Long.valueOf(""+i));
			d.setValue(i);
			
			studentList = new ArrayList<Long>();
			
			for(int j = 1; j <= 20; j++) {
				studentList.add(new Long((i-1)*20+j));
			}
			
			Class c = new Class();
			c.setId(Long.valueOf(""+i));
			c.setStudentList(studentList);
			
			if( i < 3 ) {
				Room r = new Room();
				r.setId(Long.valueOf(""+i));
				r.setNumber("R"+i);
				
				Teacher teacher = new Teacher();
				teacher.setId(Long.valueOf(""+i));
				teacher.setName("Professor #"+i);
				
				roomList.add(r);
				teacherList.add(teacher);
			}
			
			timeList.add(t);
			dayList.add(d);
			clazzList.add(c);
		}
		
		int j = 1;
		for( Day d : dayList ) {
			for( Time t : timeList ) {
				Period p = new Period();
				p.setId(Long.valueOf(""+j++));
				p.setDay(d);
				p.setTime(t);
				periodList.add(p);
			}
		}
		
		for( int i = 1; i <= 5; i++ ){
			Course course = new Course();
			course.setId(Long.valueOf(""+i));
			course.setLessonCount(2);
			course.setTeacher(teacherList.get(i % 2));
			course.setClazz(clazzList.get((new Random()).nextInt(3)));
			
			courseList.add(course);
		}
		
		j = 1;
		for( Course course : courseList ) {
			for( int i = 0; i < course.getLessonCount(); i++ ) {
				Lesson lesson = new Lesson();
				lesson.setId(Long.valueOf(""+j++));
				lesson.setCourse(course);
				lesson.setPeriod(periodList.get(0));
				lesson.setRoom(roomList.get(0));
				lessonList.add(lesson);
			}
		}
		
		em.getTransaction().commit();
		
		em.close();
		emf.close();
		
		
		Schedule schedule = new Schedule();
		schedule.setId(1L);
		schedule.setCourseList(courseList);
		schedule.setClazzList(clazzList);
		schedule.setTeacherList(teacherList);
		schedule.setRoomList(roomList);
		schedule.setPeriodList(periodList);
		schedule.setDayList(dayList);
		schedule.setTimeList(timeList);
		schedule.setLessonList(lessonList);
		
		this.solver.setPlanningProblem((Solution)schedule);
	}
	
	public void print( Schedule schedule ) {
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
						table[i][j] += "Course [#" + l.getCourse().getId() + "]<br/>Teacher[" + l.getCourse().getTeacher().getName() + "]<br/>Class[" + l.getCourse().getClazz().getId()+"];";
					}
				}
			}
		}
		
		String htmlTable = "<html><head></head><body><table border='1'><tr><td>Period\\Rooms</td>";
		
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
	
	public static void main(String [] args) {
		ScheduleMainJPA scheduleMainJPA = new ScheduleMainJPA();
		scheduleMainJPA.init();
		scheduleMainJPA.start();
	}
}
