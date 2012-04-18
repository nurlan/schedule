package kz.bee.drools.planner.schedule.main;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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
import kz.bee.drools.planner.schedule.domain.UnavailablePeriodConstraint;
import kz.bee.drools.planner.schedule.solution.Schedule;
import kz.bee.kudos.lesson.RingGroup;
import kz.bee.kudos.lesson.RingOrder;
import kz.bee.kudos.ou.Location;
import kz.bee.kudos.ou.School;
import kz.bee.wx.security.Group;
import kz.bee.wx.security.Role;
import kz.bee.wx.security.User;

import org.apache.log4j.xml.DOMConfigurator;
import org.drools.ClassObjectFilter;
import org.drools.WorkingMemory;
import org.drools.planner.config.XmlSolverConfigurer;
import org.drools.planner.core.Solver;
import org.drools.planner.core.event.BestSolutionChangedEvent;
import org.drools.planner.core.event.SolverEventListener;
import org.drools.planner.core.score.constraint.ConstraintOccurrence;
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
		        System.out.println( "Score: " + schedule.getScore() + ", Time: " + ScheduleMainJPA.this.solver.getTimeMillisSpend() );
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
		List<Course> courseList = new ArrayList<Course>(); //4
		List<Class> clazzList = new ArrayList<Class>(); //3
		List<Teacher> teacherList = new ArrayList<Teacher>(); //1
		List<Room> roomList = new ArrayList<Room>(); //2
		List<Period> periodList = new ArrayList<Period>();//5
		List<Day> dayList = new ArrayList<Day>();
		List<Time> timeList = new ArrayList<Time>();//5.1
		List<Lesson> lessonList = new ArrayList<Lesson>();
//		List<Long> studentList = new ArrayList<Long>();
		List<UnavailablePeriodConstraint> unavailablePeriodConstraintList = new ArrayList<UnavailablePeriodConstraint>();
		
		EntityManagerFactory emf = Persistence.createEntityManagerFactory("slrs");
		EntityManager em = emf.createEntityManager();
		
		em.getTransaction().begin();
		
		
		List<User> kudosTeachers = em.createQuery("select m.user from Membership m where m.group = :group and m.role = :role")
									.setParameter("group", em.find(Group.class, "BEE-Z-B-S48"))
									.setParameter("role", em.find(Role.class, "TEACHER"))
									.getResultList();
		
		List<Location> kudosRooms = em.createQuery("select l from Location l where l.school = :school")
										.setParameter("school", em.find(School.class, "BEE-Z-B-S48"))
										.getResultList();
		
		//282660
		List<kz.bee.kudos.ou.Class> kudosClasses = em.createQuery("select c from kz.bee.kudos.ou.Class c where c.parent.parent = :group and c.period = :period and c.level in (9,10,11)")
													.setParameter("group", em.find(Group.class, "BEE-Z-B-S48"))
													.setParameter("period", em.find(kz.bee.kudos.period.Period.class, 282660L))
													.getResultList();
		
		//282908
//		List<Long> idList = new ArrayList<Long>();
//		idList.add(282908L);
//		idList.add(282912L);
//		idList.add(282916L);
//		idList.add(282920L);
//		idList.add(282924L);
//		List<kz.bee.kudos.ou.Class> kudosClasses = em.createQuery("select c from kz.bee.kudos.ou.Class c where c.id  in (:id)")
//				.setParameter("id", idList)
//				.getResultList();
				
//		List<kz.bee.kudos.course.Course> kudosCourses = em.createQuery("select c from kz.bee.kudos.course.Course c where c.period = :period and c.clazz.parent.parent = :school")
//														.setParameter("period", em.find(kz.bee.kudos.period.Period.class, 282660L))
//														.setParameter("school", em.find(Group.class, "BEE-Z-B-S48"))
//														.getResultList();
		
		List<kz.bee.kudos.course.Course> kudosCourses = em.createQuery("select c from kz.bee.kudos.course.Course c where c.clazz in (:kudosClasses)")
														.setParameter("kudosClasses", kudosClasses)
														.getResultList();
		
		List<RingOrder> ringOrder = em.createQuery("select r from RingOrder r where r.group = :ringGroup order by r.order asc")
										.setParameter("ringGroup", em.find(RingGroup.class, 322828L))
										.getResultList();
		
//		List<RingOrder> ringOrder2 = em.createQuery("select r from RingOrder r where r.group = :ringGroup order by r.order asc")
//										.setParameter("ringGroup", em.find(RingGroup.class, 322829L))
//										.getResultList();
		
//		ringOrder.addAll(ringOrder2);
		
		for(User u : kudosTeachers) {
			Teacher t = new Teacher();
			t.setId(u.getName());
			t.setName(u.getLastname()+" "+u.getFirstname());
			teacherList.add(t);
		}
		
		for(Location l : kudosRooms) {
			Room r = new Room();
			r.setId(Long.parseLong(l.getName()));
			r.setNumber(l.getName());
			roomList.add(r);
		}
//		roomList = roomList.subList(0, 3);
		
		System.out.println("Room size: " + roomList.size());
		
		for(kz.bee.kudos.ou.Class c : kudosClasses) {
			Class clazz = new Class();
			clazz.setId(c.getId());
//			clazz.setStudentList(studentList);
			clazz.seteLevel(c.getLevel().intValue());
			clazzList.add(clazz);
		}
		
		//UnavailablePeriodConstraint unavailablePeriodConstraint = new UnavailablePeriodConstraint();
		
		for(kz.bee.kudos.course.Course c : kudosCourses) {
			Course course = new Course();
			course.setId(c.getId());
			course.setLessonCount(c.getWeeklyHours().intValue());
			course.setClazz(getClazz(clazzList,c.getClazz().getId()));
			course.setTeacher(getTeacher(teacherList, c.getTeacher().getName()));
			courseList.add(course);
			
			if(course.getId().equals(284700L)) {
//				unavailablePeriodConstraint.setId(1L);
//				unavailablePeriodConstraint.setCourse(course);
			}
		}
		
		for(RingOrder r : ringOrder) {
			Time t = new Time();
			t.setId(r.getId());
			t.setValue(r.getOrder());
			timeList.add(t);
		}
		
		for(int i = 1; i < 7; i++ ) {
			Day d = new Day();
			d.setId(Long.parseLong(""+i));
			d.setValue(i);
			dayList.add(d);
		}
		
		int j = 1;
		for( Day d : dayList ) {
			for( Time t : timeList ) {
				Period p = new Period();
				p.setId(Long.valueOf(""+j++));
				p.setDay(d);
				p.setTime(t);
				periodList.add(p);
				
				if(d.getValue() == 1 && t.getValue() == 2) {
//					unavailablePeriodConstraint.setPeriod(p);
				}
			}
		}
		
//		unavailablePeriodConstraintList.add(unavailablePeriodConstraint);
		
		j = 1;
		int k = 0;
		for( Course course : courseList ) {
			for( int i = 0; i < course.getLessonCount(); i++ ) {
				Lesson lesson = new Lesson();
				lesson.setId(Long.valueOf(""+j++));
//				lesson.setCourseId(course.getId());
//				lesson.setTeacherId(course.getTeacher().getId());
//				lesson.setClassId(course.getClazz().getId());
				lesson.setCourse(course);
				lesson.setPeriod(periodList.get(k % periodList.size()));
				lesson.setRoom(roomList.get((k / periodList.size()) % roomList.size()));
				lessonList.add(lesson);
				k++;
			}
		}
		System.out.println("Lessons size:" + j);
		
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
		schedule.setUnavailablePeriodConstraintList(unavailablePeriodConstraintList);
		
		print(schedule);
		
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
	
//	public List<ScoreDetail> getScoreDetailList() {
//        if (!(guiScoreDirector instanceof DroolsScoreDirector)) {
//            return null;
//        }
//        Map<String, ScoreDetail> scoreDetailMap = new HashMap<String, ScoreDetail>();
//        WorkingMemory workingMemory = ((DroolsScoreDirector) guiScoreDirector).getWorkingMemory();
//        if (workingMemory == null) {
//            return Collections.emptyList();
//        }
//        Iterator<ConstraintOccurrence> it = (Iterator<ConstraintOccurrence>) workingMemory.iterateObjects(
//                new ClassObjectFilter(ConstraintOccurrence.class));
//        while (it.hasNext()) {
//            ConstraintOccurrence constraintOccurrence = it.next();
//            ScoreDetail scoreDetail = scoreDetailMap.get(constraintOccurrence.getRuleId());
//            if (scoreDetail == null) {
//                scoreDetail = new ScoreDetail(constraintOccurrence.getRuleId(), constraintOccurrence.getConstraintType());
//                scoreDetailMap.put(constraintOccurrence.getRuleId(), scoreDetail);
//            }
//            scoreDetail.addConstraintOccurrence(constraintOccurrence);
//        }
//        List<ScoreDetail> scoreDetailList = new ArrayList<ScoreDetail>(scoreDetailMap.values());
//        Collections.sort(scoreDetailList);
//        return scoreDetailList;
//    }
	
	public Teacher getTeacher(List<Teacher> teachers, String id) {
		for(Teacher t : teachers) {
			if(t.getId().equals(id)) {
				return t;
			}
		}
		
		return null;
	}
	
	public Class getClazz(List<Class> classes, Long id) {
		for(Class c : classes) {
			if(c.getId() == id) {
				return c;
			}
		}
		
		return null;
	}
	
	public static void main(String [] args) {
		ScheduleMainJPA scheduleMainJPA = new ScheduleMainJPA();
		scheduleMainJPA.init();
		scheduleMainJPA.start();
	}
}
