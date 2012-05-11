package kz.bee.drools.planner.schedule.main;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

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
import kz.bee.drools.planner.schedule.validate.ScoreDetail;
import kz.bee.drools.planner.schedule.validate.Validate;
import kz.bee.kudos.course.CoursePlanItem;
import kz.bee.kudos.lesson.Lesson.Status;
import kz.bee.kudos.lesson.RingGroup;
import kz.bee.kudos.lesson.RingOrder;
import kz.bee.kudos.ou.Location;
import kz.bee.kudos.ou.School;
import kz.bee.wx.security.Group;
import kz.bee.wx.security.Role;
import kz.bee.wx.security.User;

import org.drools.ClassObjectFilter;
import org.drools.WorkingMemory;
import org.drools.planner.config.SolverFactory;
import org.drools.planner.config.XmlSolverFactory;
import org.drools.planner.core.Solver;
import org.drools.planner.core.bestsolution.BestSolutionRecaller;
import org.drools.planner.core.event.BestSolutionChangedEvent;
import org.drools.planner.core.event.SolverEventListener;
import org.drools.planner.core.score.Score;
import org.drools.planner.core.score.constraint.ConstraintOccurrence;
import org.drools.planner.core.score.director.ScoreDirector;
import org.drools.planner.core.score.director.drools.DroolsScoreDirector;
import org.drools.planner.core.solver.DefaultSolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ScheduleMainJPA {

	public static final String SOLVER_CONFIG = "/scheduleSolverConfig.xml";
	public static long prevTime = 0;
	protected final transient Logger logger = LoggerFactory.getLogger(getClass());
	 
	private volatile Solver solver;
	private ScoreDirector scoreDirector;
	
	private Map<Long,Course> courseMap;
	private Map<String,Teacher> teacherMap;
	private Map<Long, Lesson> lessonMap;
	
	private List<kz.bee.kudos.course.Course> kudosCourseList;
	
	public ScheduleMainJPA() {
		
	}
	
	private void init() {
		SolverFactory solverFactory = new XmlSolverFactory(SOLVER_CONFIG);
		solver = solverFactory.buildSolver();
		
		scoreDirector = solver.getScoreDirectorFactory().buildScoreDirector();
		
		this.solver.addEventListener( new SolverEventListener() {
		    public void bestSolutionChanged(BestSolutionChangedEvent event) {
		        Schedule schedule = (Schedule) solver.getBestSolution();
		        logger.info("Score: " + schedule.getScore() + ", Time: " + ScheduleMainJPA.this.solver.getTimeMillisSpend());
		        //we can make something with this solution
		    }
		});
	}
	
	public void createSolution(String schoolName, Long ringGroupId, Long periodId) {
		try {
			List<kz.bee.drools.planner.schedule.domain.School> schoolList = new ArrayList<kz.bee.drools.planner.schedule.domain.School>(); 	//0
			List<Teacher> teacherList = new ArrayList<Teacher>(); 	//1
			List<Room> roomList = new ArrayList<Room>(); 			//2
			List<Class> clazzList = new ArrayList<Class>(); 		//3
			List<Course> courseList = new ArrayList<Course>(); 		//4
			List<Time> timeList = new ArrayList<Time>(); 			//5.1
			List<Day> dayList = new ArrayList<Day>(); 				//5.2
			List<Period> periodList = new ArrayList<Period>();		//5.3
			List<Lesson> lessonList = new ArrayList<Lesson>(); 		//6 
			List<UnavailablePeriodConstraint> unavailablePeriodConstraintList = new ArrayList<UnavailablePeriodConstraint>();
			
			EntityManagerFactory emf = Persistence.createEntityManagerFactory("slrs");
			EntityManager em = emf.createEntityManager();
			
			em.getTransaction().begin();
			
			School school = em.find(School.class, schoolName);
			RingGroup ringGroup = em.find(RingGroup.class, ringGroupId);
			kz.bee.kudos.period.Period period = em.find(kz.bee.kudos.period.Period.class, periodId);
			
			timestamp("t start:");
			List<kz.bee.kudos.ou.Class> kudosClassList = em.createQuery("select c from kz.bee.kudos.ou.Class c where c.parent.parent = :group and c.period = :period and c.level in (5,6,7,8)")
					.setParameter("group", em.find(Group.class, schoolName))
					.setParameter("period", period)
					.getResultList();
			timestamp("t get classes:");
			kudosCourseList = em.createQuery("select c from kz.bee.kudos.course.Course c where c.clazz in (:kudosClasses) and c.teacher is not null")
					.setParameter("kudosClasses", kudosClassList)
					.getResultList();
			timestamp("t courses:");
			List<RingOrder> ringOrderList = em.createQuery("select r from RingOrder r where r.group = :ringGroup order by r.order asc")
											.setParameter("ringGroup", ringGroup)
											.getResultList();
			timestamp("t ring orders:");
			List<kz.bee.kudos.lesson.Lesson> kudosLessonList = em.createQuery("select " +
																					"l " +
																			"from " +
																				"kz.bee.kudos.lesson.Lesson l " +
																			"where " +
																				"l.ring in (:ringOrders) " +
																			"and " +
																				"l.course in (:courses)" 
																			)
																			.setParameter("ringOrders", ringOrderList)
																			.setParameter("courses", kudosCourseList)
																			.getResultList();
			timestamp("t lessons:");
			System.out.println("Lesson count: " + kudosLessonList.size());

			List<User> kudosTeacherList = em.createQuery("select distinct c.teacher from CoursePlanItem c where c.course in (:courseList)")
					.setParameter("courseList", kudosCourseList)
					.getResultList();
			
			timestamp("t3:");
			List<Location> kudosRoomList = em.createQuery("select l from Location l where l.school = :school")
											.setParameter("school", school)
											.getResultList();
			
			
			kz.bee.drools.planner.schedule.domain.School timetableSchool = new kz.bee.drools.planner.schedule.domain.School();
			timetableSchool.setId(schoolName);
			timetableSchool.setType(school.getType());
			schoolList.add(timetableSchool);
			
			
			timestamp("t8:");
			teacherMap = new HashMap<String, Teacher>();
			for(User u : kudosTeacherList) {
				Teacher teacher = new Teacher();
				teacher.setId(u.getName());
				teacher.setName(u.getLastname()+" "+u.getFirstname());
				
				teacherList.add(teacher);
				teacherMap.put(u.getName(), teacher);
			}
			timestamp("t9:");
			Map<Long,Room> roomMap = new HashMap<Long,Room>();
			for(Location l : kudosRoomList) {
				Room room = new Room();
				room.setId(l.getId());
				room.setNumber(l.getName());
				
				roomList.add(room);
				roomMap.put(l.getId(),room);
			}
			timestamp("t10:");
			Map<Long,Class> classMap = new HashMap<Long, Class>();
			for(kz.bee.kudos.ou.Class c : kudosClassList) {
				Class clazz = new Class();
				clazz.setId(c.getId());
				clazz.setWxGroupName(c.getParent().getName());
				clazz.setLevel(c.getLevel().intValue());
				
				clazzList.add(clazz);
				classMap.put(c.getId(), clazz);
			}
			timestamp("t11:");
			courseMap = new HashMap<Long, Course>();
			for(kz.bee.kudos.course.Course c : kudosCourseList) {
				Course course = new Course();
				course.setId(c.getId());
				course.setName(c.getName());
				course.setLessonCount(c.getWeeklyHours().intValue());
				course.setClazz(classMap.get(c.getClazz().getId()));
				
				courseList.add(course);
				courseMap.put(c.getId(),course);
			}
			timestamp("t12:");
			for(int i = 1; i < 7; i++ ) {
				Day d = new Day();
				d.setId(Long.parseLong(""+i));
				d.setValue(i);
				dayList.add(d);
			}
			
			for(RingOrder r : ringOrderList) {
				Time t = new Time();
				t.setId(r.getId());
				t.setOrder(r.getOrder());
				timeList.add(t);
			}
			
			Map<Long,Period> periodMap = new HashMap<Long,Period>();
			for( Day d : dayList ) {
				for( Time t : timeList ) {
					Period p = new Period();
					p.setId(Long.parseLong(d.getId()+""+t.getId()));
					p.setDay(d);
					p.setTime(t);
					
					periodList.add(p);
					periodMap.put(p.getId(), p);
				}
			}
			
			timestamp("t13:");
			lessonMap = new HashMap<Long, Lesson>();
			for(kz.bee.kudos.lesson.Lesson lesson : kudosLessonList) {
				Lesson l = new Lesson();
				l.setId(lesson.getId());
				l.setCourse(courseMap.get(lesson.getCourse().getId()));
				l.setTeacher(teacherMap.get(lesson.getTeacher().getName()));
				Period p = periodMap.get(Long.parseLong(lesson.getBegin().getDay()+""+lesson.getRing().getId()));
				if( p != null ) {
					l.setPeriod(p);
				}
				Location location =  lesson.getLocation();
				if( location != null ) {
					l.setRoom(roomMap.get(location.getId()));
				}
				l.setPinned(true);
				
				lessonList.add(l);
				lessonMap.put(lesson.getId(), l);
			}
			
			timestamp("t14:");
			
			em.getTransaction().commit();
			
			em.close();
			emf.close();
			
			
			Schedule schedule = new Schedule();
			schedule.setId(1L);
			schedule.setSchoolList(schoolList);
			schedule.setCourseList(courseList);
			schedule.setClazzList(clazzList);
			schedule.setTeacherList(teacherList);
			schedule.setRoomList(roomList);
			schedule.setPeriodList(periodList);
			schedule.setDayList(dayList);
			schedule.setTimeList(timeList);
			schedule.setLessonList(lessonList);
			schedule.setUnavailablePeriodConstraintList(unavailablePeriodConstraintList);
			
			
			this.scoreDirector.setWorkingSolution(schedule);
			this.solver.setPlanningProblem(this.scoreDirector.getWorkingSolution());
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	private void start() {
		try {
			System.out.println("Start solving ...");
			
			this.solver.solve();
			
			Schedule schedule = (Schedule) solver.getBestSolution();
			this.scoreDirector.setWorkingSolution(schedule);
			this.scoreDirector.calculateScore();
			
			System.out.println( "Score: " + schedule.getScore() + ", Time: " + this.solver.getTimeMillisSpend() );
	        print(schedule);
	        
	        System.out.println("End solving ...");
	        
//	    	System.out.println( "getScoreDetailList size:" + getScoreDetailList().size());
//			System.out.println( "Broken constrains list:" );
//			
//			for(ScoreDetail sd : getScoreDetailList()) {
//				System.out.println(sd);
//				System.out.println("=>"+sd.buildConstraintOccurrenceListText());
////				System.out.println("%>"+sd.buildConstraintOccurrenceList());
//			}
	
			EntityManagerFactory emf = Persistence.createEntityManagerFactory("slrs");
			EntityManager em = emf.createEntityManager();
			
			em.getTransaction().begin();
			for(Lesson lesson : schedule.getLessonList()) {
				kz.bee.kudos.lesson.Lesson kudosLesson = new kz.bee.kudos.lesson.Lesson();
				Calendar cl = Calendar.getInstance();
				cl.set(Calendar.DAY_OF_WEEK,lesson.getPeriod().getDay().getValue()+1);
				RingOrder ring = em.find(RingOrder.class, lesson.getPeriod().getTime().getId());
				StringTokenizer st = new StringTokenizer(ring.getBegin());
				cl.set(Calendar.HOUR_OF_DAY, Integer.parseInt(st.nextToken(":")));
				cl.set(Calendar.MINUTE, Integer.parseInt(st.nextToken(":")));
				kudosLesson.setBegin(cl.getTime());
				
				st = new StringTokenizer(ring.getEnd());
				cl.set(Calendar.HOUR_OF_DAY, Integer.parseInt(st.nextToken(":")));
				cl.set(Calendar.MINUTE, Integer.parseInt(st.nextToken(":")));
				kudosLesson.setEnd(cl.getTime());
				kudosLesson.setCourse(em.find(kz.bee.kudos.course.Course.class,lesson.getCourse().getId()));
				kudosLesson.setRing(ring);
				kudosLesson.setOrder(lesson.getPeriod().getTime().getOrder());
				kudosLesson.setStatus(Status.PLANNED);
				kudosLesson.setLocation(em.find(Location.class, lesson.getRoom().getId()));
				kudosLesson.setType(lesson.getLessonType());
				kudosLesson.setTeacher(em.find(User.class, lesson.getTeacher().getId()));
				
				em.persist(kudosLesson);
			}
			
			em.getTransaction().commit();
			System.out.println("THE END");
			
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public void print( Schedule schedule ) {
		try {
			List<Period> periodList = schedule.getPeriodList();
			List<Class> clazzList = schedule.getClazzList();
			List<Lesson> lessonList = schedule.getLessonList();
			List<Room> roomList = schedule.getRoomList();
			
	//		String [][]table = new String[periodList.size()][roomList.size()];
			String [][]table = new String[periodList.size()][clazzList.size()];
			
			for(int i = 0; i < periodList.size(); i++) {
	//			for(int j = 0; j < roomList.size(); j++) {
				for(int j = 0; j < clazzList.size(); j++) {
					for( Lesson l : lessonList ) {
	//					if( periodList.get(i) == l.getPeriod() && roomList.get(j) == l.getRoom() ) {
	//						if(table[i][j] == null) table[i][j] = "";
	//						table[i][j] += "Course [#" + l.getCourse().getId() + "]<br/>Teacher[" + l.getCourse().getTeacher().getId() + "]<br/>Class[" 
	//													+ l.getCourse().getClazz().getId()+", level=" + l.getCourse().getClazz().geteLevel() + "];";
	//					}
						if( periodList.get(i) == l.getPeriod() && clazzList.get(j) == l.getCourse().getClazz() ) {
							if(table[i][j] == null) table[i][j] = "";
							table[i][j] += "Course [#" + l.getCourse().getId() + " " + l.getLessonType() + " d:"+l.getTimeValue()+"]<br/>Teacher[" + l.getTeacher().getId() + "]<br/>Room[" 
														+ l.getCourse().getRoom()+", level=" + l.getCourse().getClazz().getLevel() + "];";
						}
					}
				}
			}
			
	//		String htmlTable = "<html><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\"></head><body><table border='1'><tr><td>Period\\Rooms</td>";
			String htmlTable = "<html><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\"></head><body><table border='1'><tr><td>Period\\Classes</td>";
			
	//		for(int i = 0; i < roomList.size(); i++ ) {
	//			htmlTable += "<td> Room[" + roomList.get(i).getNumber()+"]</td>";
	//		}
			for(int i = 0; i < clazzList.size(); i++ ) {
				htmlTable += "<td> class[" + clazzList.get(i).getId()+"]</td>";
			}
			htmlTable += "</tr>";
			
			for( int i = 0; i < table.length; i++) {
				htmlTable += "<tr><td>" + periodList.get(i).getDay().getValue() + " : " + periodList.get(i).getTime().getOrder() + " ["+periodList.get(i).getTime().getValue()+"]" + "</td>";
				
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
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	@SuppressWarnings("unchecked")
	public void setPlanningProblem2(String schoolName, Long ringGroupId, Long periodId) {
		try {
			List<kz.bee.drools.planner.schedule.domain.School> schoolList = new ArrayList<kz.bee.drools.planner.schedule.domain.School>(); 	//0
			List<Teacher> teacherList = new ArrayList<Teacher>(); 	//1
			List<Room> roomList = new ArrayList<Room>(); 			//2
			List<Class> clazzList = new ArrayList<Class>(); 		//3
			List<Course> courseList = new ArrayList<Course>(); 		//4
			List<Time> timeList = new ArrayList<Time>(); 			//5.1
			List<Day> dayList = new ArrayList<Day>(); 				//5.2
			List<Period> periodList = new ArrayList<Period>();		//5.3
			List<Lesson> lessonList = new ArrayList<Lesson>(); 		//6 
			List<UnavailablePeriodConstraint> unavailablePeriodConstraintList = new ArrayList<UnavailablePeriodConstraint>();
			
			EntityManagerFactory emf = Persistence.createEntityManagerFactory("slrs");
			EntityManager em = emf.createEntityManager();
			
			em.getTransaction().begin();
			
			School school = em.find(School.class, schoolName);
			RingGroup ringGroup = em.find(RingGroup.class, ringGroupId);
			kz.bee.kudos.period.Period period = em.find(kz.bee.kudos.period.Period.class, periodId);
			
			List<kz.bee.kudos.ou.Class> kudosClassList = em.createQuery("select c from kz.bee.kudos.ou.Class c where c.parent.parent = :group and c.period = :period and c.level in (5,6,7,8)")
					.setParameter("group", em.find(Group.class, schoolName))
					.setParameter("period", period)
					.getResultList();
			timestamp("t get classes:");
			List<kz.bee.kudos.course.Course> kudosCourseList = em.createQuery("select c from kz.bee.kudos.course.Course c where c.clazz in (:kudosClasses) and c.teacher is not null")
					.setParameter("kudosClasses", kudosClassList)
					.getResultList();
			timestamp("t courses:");
			List<RingOrder> ringOrderList = em.createQuery("select r from RingOrder r where r.group = :ringGroup order by r.order asc")
											.setParameter("ringGroup", ringGroup)
											.getResultList();
			timestamp("t ring orders:");
			List<kz.bee.kudos.lesson.Lesson> kudosLessonList = em.createQuery("select " +
																					"l " +
																			"from " +
																				"kz.bee.kudos.lesson.Lesson l " +
																			"where " +
																				"l.ring in (:ringOrders) " +
																			"and " +
																				"l.course in (:courses)" 
																			)
																			.setParameter("ringOrders", ringOrderList)
																			.setParameter("courses", kudosCourseList)
																			.getResultList();
			timestamp("t lessons:");
			System.out.println("Kudos Lesson count: " + kudosLessonList.size());

			List<User> kudosTeacherList = em.createQuery("select distinct c.teacher from CoursePlanItem c where c.course in (:courseList)")
					.setParameter("courseList", kudosCourseList)
					.getResultList();
			
			timestamp("t3:");
			List<Location> kudosRoomList = em.createQuery("select l from Location l where l.school = :school")
											.setParameter("school", school)
											.getResultList();
			
			timestamp("t7:");
			List<CoursePlanItem> kudosCoursePlanItemList = new ArrayList<CoursePlanItem>();
			int lessonTypeCount = 0;
			List<CoursePlanItem> coursePlanItemList;
			for(kz.bee.kudos.course.Course c : kudosCourseList) {
				lessonTypeCount = em.createQuery("select c.lessonType from CoursePlanItem c where c.course = :course group by c.lessonType").setParameter("course", c).getResultList().size();
				coursePlanItemList = em.createQuery("select c from CoursePlanItem c where c.course = :course order by c.id desc").setParameter("course", c)
										.setMaxResults(lessonTypeCount)
										.getResultList();
				
				kudosCoursePlanItemList.addAll(coursePlanItemList);
			}
			timestamp("t7.1:");
			
			
			kz.bee.drools.planner.schedule.domain.School timetableSchool = new kz.bee.drools.planner.schedule.domain.School();
			timetableSchool.setId(schoolName);
			timetableSchool.setType(school.getType());
			schoolList.add(timetableSchool);
			
			
			timestamp("t8:");
			Map<String,Teacher> teacherMap = new HashMap<String, Teacher>();
			for(User u : kudosTeacherList) {
				Teacher teacher = new Teacher();
				teacher.setId(u.getName());
				teacher.setName(u.getLastname()+" "+u.getFirstname());
				
				teacherList.add(teacher);
				teacherMap.put(u.getName(), teacher);
			}
			timestamp("t9:");
			Map<Long,Room> roomMap = new HashMap<Long,Room>();
			for(Location l : kudosRoomList) {
				Room room = new Room();
				room.setId(l.getId());
				room.setNumber(l.getName());
				
				roomList.add(room);
				roomMap.put(l.getId(),room);
			}
			timestamp("t10:");
			Map<Long,Class> classMap = new HashMap<Long, Class>();
			for(kz.bee.kudos.ou.Class c : kudosClassList) {
				Class clazz = new Class();
				clazz.setId(c.getId());
				clazz.setWxGroupName(c.getParent().getName());
				clazz.setLevel(c.getLevel().intValue());
				
				clazzList.add(clazz);
				classMap.put(c.getId(), clazz);
			}
			timestamp("t11:");
			Map<Long,Course> courseMap = new HashMap<Long, Course>();
			for(kz.bee.kudos.course.Course c : kudosCourseList) {
				Course course = new Course();
				course.setId(c.getId());
				course.setName(c.getName());
				course.setLessonCount(c.getWeeklyHours().intValue());
				course.setClazz(classMap.get(c.getClazz().getId()));
				
				courseList.add(course);
				courseMap.put(c.getId(),course);
			}
			timestamp("t12:");
			for(int i = 1; i < 7; i++ ) {
				Day d = new Day();
				d.setId(Long.parseLong(""+i));
				d.setValue(i);
				dayList.add(d);
			}
			
			for(RingOrder r : ringOrderList) {
				Time t = new Time();
				t.setId(r.getId());
				t.setOrder(r.getOrder());
				timeList.add(t);
			}
			
			Map<Long,Period> periodMap = new HashMap<Long,Period>();
			for( Day d : dayList ) {
				for( Time t : timeList ) {
					Period p = new Period();
					p.setId(Long.parseLong(d.getId()+""+t.getId()));
					p.setDay(d);
					p.setTime(t);
					
					periodList.add(p);
					periodMap.put(p.getId(), p);
				}
			}
			
			timestamp("t13:");
			Map<Long, Lesson> lessonMap = new HashMap<Long, Lesson>();
			for(kz.bee.kudos.lesson.Lesson lesson : kudosLessonList) {
				Lesson l = new Lesson();
				l.setId(lesson.getId());
				l.setCourse(courseMap.get(lesson.getCourse().getId()));
				l.setTeacher(teacherMap.get(lesson.getTeacher().getName()));
				Period p = periodMap.get(Long.parseLong(lesson.getBegin().getDay()+""+lesson.getRing().getId()));
				if( p != null ) {
					l.setPeriod(p);
				}
				Location location =  lesson.getLocation();
				if( location != null ) {
					l.setRoom(roomMap.get(location.getId()));
				}
				l.setPinned(true);
				
				lessonList.add(l);
				lessonMap.put(lesson.getId(), l);
			}
			
			timestamp("t14:");
			Long j = 0L;
			for( CoursePlanItem cpi : kudosCoursePlanItemList ) {
				int reminder = cpi.getHours() % cpi.getCount();
				
				for(int i = 0; i < cpi.getCount(); i++) {
					Lesson lesson = new Lesson();
					while(lessonMap.containsKey(j)) {
						j++;
					}
					lesson.setId(j++);
					lesson.setCourse(courseMap.get(cpi.getCourse().getId()));
					lesson.setTeacher(teacherMap.get(cpi.getTeacher().getName()));
					lesson.setLessonType(cpi.getLessonType());
					lesson.setTimeValue((cpi.getHours() / cpi.getCount())+((reminder>0)?1:0));
					lesson.setPinned(false);
//					lesson.setPeriod(periodList.get(k % periodList.size()));
//					lesson.setRoom(roomList.get((k / periodList.size()) % roomList.size()));
					lessonList.add(lesson);
					
					if(reminder > 0) {
						reminder--;
					}
				}
			}
			
			System.out.println("Lessons size:" + j);
			
			em.getTransaction().commit();
			
			em.close();
			emf.close();
			
			
			Schedule schedule = new Schedule();
			schedule.setId(1L);
			schedule.setSchoolList(schoolList);
			schedule.setCourseList(courseList);
			schedule.setClazzList(clazzList);
			schedule.setTeacherList(teacherList);
			schedule.setRoomList(roomList);
			schedule.setPeriodList(periodList);
			schedule.setDayList(dayList);
			schedule.setTimeList(timeList);
			schedule.setLessonList(lessonList);
			schedule.setUnavailablePeriodConstraintList(unavailablePeriodConstraintList);
			
//			print(schedule);
			
			this.scoreDirector.setWorkingSolution(schedule);
			this.solver.setPlanningProblem(this.scoreDirector.getWorkingSolution());
		}
		catch(Exception e) {
			e.printStackTrace();
		}
 	}
	
	public void setPlanningProblem3(String schoolName, Long ringGroupId, Long periodId) {
		try {
			createSolution(schoolName, ringGroupId, periodId);
			Schedule schedule = (Schedule) this.solver.getBestSolution();
			
			List<Lesson> lessonList = schedule.getLessonList();
			
			EntityManagerFactory emf = Persistence.createEntityManagerFactory("slrs");
			EntityManager em = emf.createEntityManager();
			
			em.getTransaction().begin();
			
			List<CoursePlanItem> kudosCoursePlanItemList = new ArrayList<CoursePlanItem>();
			int lessonTypeCount = 0;
			List<CoursePlanItem> coursePlanItemList;
			
			timestamp("t7.0.1:");
			for(kz.bee.kudos.course.Course c : kudosCourseList ) {
				lessonTypeCount = em.createQuery("select c.lessonType from CoursePlanItem c where c.course = :course group by c.lessonType").setParameter("course", c).getResultList().size();
				coursePlanItemList = em.createQuery("select c from CoursePlanItem c where c.course = :course order by c.id desc").setParameter("course", c)
										.setMaxResults(lessonTypeCount)
										.getResultList();
				
				kudosCoursePlanItemList.addAll(coursePlanItemList);
			}
			timestamp("t7.1:");
			
			Long j = 0L;
			for( CoursePlanItem cpi : kudosCoursePlanItemList ) { //TODO lesson count --
				int reminder = cpi.getHours() % cpi.getCount();
				
				for(int i = 0; i < cpi.getCount(); i++) {
					Lesson lesson = new Lesson();
					while(lessonMap.containsKey(j)) {
						j++;
					}
					lesson.setId(j++);
					lesson.setCourse(courseMap.get(cpi.getCourse().getId()));
					lesson.setTeacher(teacherMap.get(cpi.getTeacher().getName()));
					lesson.setLessonType(cpi.getLessonType());
					lesson.setTimeValue((cpi.getHours() / cpi.getCount())+((reminder>0)?1:0));
					lesson.setPinned(false);
					lessonList.add(lesson);
					
					if(reminder > 0) {
						reminder--;
					}
				}
			}
			
			System.out.println("Lessons size:" + j);
			
			em.getTransaction().commit();
			
			em.close();
			emf.close();
			
			schedule.setLessonList(lessonList);
			
			this.scoreDirector.setWorkingSolution(schedule);
			this.solver.setPlanningProblem(this.scoreDirector.getWorkingSolution());
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	@SuppressWarnings("unchecked")
	public List<Object[]> validate(String schoolName, Long ringGroupId, Long periodId) {
		List<Object[]> brokenConstraintList = new ArrayList<Object[]>();
		try {
			EntityManagerFactory emf = Persistence.createEntityManagerFactory("slrs");
			EntityManager em = emf.createEntityManager();
			
			em.getTransaction().begin();
			
			School school = em.find(School.class, schoolName);
			RingGroup ringGroup = em.find(RingGroup.class, ringGroupId);
			kz.bee.kudos.period.Period period = em.find(kz.bee.kudos.period.Period.class, periodId);

//			timestamp("t start:");
			List<kz.bee.kudos.ou.Class> kudosClassList = em.createQuery("select c from kz.bee.kudos.ou.Class c where c.parent.parent = :group and c.period = :period and c.level in (5,6,7,8)")
					.setParameter("group", em.find(Group.class, schoolName))
					.setParameter("period", period)
					.getResultList();
//			timestamp("t get classes:");
			List<kz.bee.kudos.course.Course> kudosCourseList = em.createQuery("select c from kz.bee.kudos.course.Course c where c.clazz in (:kudosClasses) and c.teacher is not null")
					.setParameter("kudosClasses", kudosClassList)
					.getResultList();
//			timestamp("t courses:");
			List<RingOrder> ringOrderList = em.createQuery("select r from RingOrder r where r.group = :ringGroup order by r.order asc")
											.setParameter("ringGroup", ringGroup)
											.getResultList();
//			timestamp("t ring orders:");
			List<kz.bee.kudos.lesson.Lesson> kudosLessonList = em.createQuery("select " +
																					"l " +
																			"from " +
																				"kz.bee.kudos.lesson.Lesson l " +
																			"where " +
																				"l.ring in (:ringOrders) " +
																			"and " +
																				"l.course in (:courses)" 
																			)
																			.setParameter("ringOrders", ringOrderList)
																			.setParameter("courses", kudosCourseList)
																			.getResultList();
//			timestamp("t lessons:");
			System.out.println("Lesson count: " + kudosLessonList.size());

			List<User> kudosTeacherList = em.createQuery("select distinct c.teacher from CoursePlanItem c where c.course in (:courseList)")
					.setParameter("courseList", kudosCourseList)
					.getResultList();
			
//			timestamp("t3:");
			List<Location> kudosRoomList = em.createQuery("select l from Location l where l.school = :school")
											.setParameter("school", school)
											.getResultList();
			
//			timestamp("t7:");
			List<kz.bee.drools.planner.schedule.domain.School> schoolList = new ArrayList<kz.bee.drools.planner.schedule.domain.School>(); 	//0
			List<Course> courseList = new ArrayList<Course>(); //4
			List<Class> clazzList = new ArrayList<Class>(); //3
			List<Teacher> teacherList = new ArrayList<Teacher>(); //1
			List<Room> roomList = new ArrayList<Room>(); //2
			List<Period> periodList = new ArrayList<Period>();//5
			List<Day> dayList = new ArrayList<Day>();
			List<Time> timeList = new ArrayList<Time>();//5.1
			List<Lesson> lessonList = new ArrayList<Lesson>();
			List<UnavailablePeriodConstraint> unavailablePeriodConstraintList = new ArrayList<UnavailablePeriodConstraint>();

			kz.bee.drools.planner.schedule.domain.School timetableSchool = new kz.bee.drools.planner.schedule.domain.School();
			timetableSchool.setId(schoolName);
			timetableSchool.setType(school.getType());
			schoolList.add(timetableSchool);
			
//			timestamp("t8:");
			Map<String,Teacher> teacherMap = new HashMap<String, Teacher>();
			for(User u : kudosTeacherList) {
				Teacher teacher = new Teacher();
				teacher.setId(u.getName());
				teacher.setName(u.getLastname()+" "+u.getFirstname());
				
				teacherList.add(teacher);
				teacherMap.put(u.getName(), teacher);
			}
//			timestamp("t9:");
			Map<Long,Room> roomMap = new HashMap<Long,Room>();
			for(Location l : kudosRoomList) {
				Room room = new Room();
				room.setId(l.getId());
				room.setNumber(l.getName());
				
				roomList.add(room);
				roomMap.put(l.getId(),room);
			}
//			timestamp("t10:");
			Map<Long,Class> classMap = new HashMap<Long, Class>();
			for(kz.bee.kudos.ou.Class c : kudosClassList) {
				Class clazz = new Class();
				clazz.setId(c.getId());
				clazz.setWxGroupName(c.getParent().getName());
				clazz.setLevel(c.getLevel().intValue());
				
				clazzList.add(clazz);
				classMap.put(c.getId(), clazz);
			}
//			timestamp("t11:");
			Map<Long,Course> courseMap = new HashMap<Long, Course>();
			for(kz.bee.kudos.course.Course c : kudosCourseList) {
				Course course = new Course();
				course.setId(c.getId());
				course.setName(c.getName());
				course.setLessonCount(c.getWeeklyHours().intValue());
				course.setClazz(classMap.get(c.getClazz().getId()));
//				course.setTeacher(teacherMap.get(c.getTeacher().getName()));
				
				courseList.add(course);
				courseMap.put(c.getId(),course);
			}
//			timestamp("t12:");
			for(int i = 1; i < 7; i++ ) {
				Day d = new Day();
				d.setId(Long.parseLong(""+i));
				d.setValue(i);
				dayList.add(d);
			}
			
			for(RingOrder r : ringOrderList) {
				Time t = new Time();
				t.setId(r.getId());
				t.setOrder(r.getOrder());
				timeList.add(t);
			}
			
			Map<Long,Period> periodMap = new HashMap<Long,Period>();
			for( Day d : dayList ) {
				for( Time t : timeList ) {
					Period p = new Period();
					p.setId(Long.parseLong(d.getId()+""+t.getId()));
					p.setDay(d);
					p.setTime(t);
					
					periodList.add(p);
					periodMap.put(p.getId(), p);
				}
			}
			
//			timestamp("t13:");
			for(kz.bee.kudos.lesson.Lesson lesson : kudosLessonList) {
				Lesson l = new Lesson();
				l.setId(lesson.getId());
				l.setCourse(courseMap.get(lesson.getCourse().getId()));
				l.setTeacher(teacherMap.get(lesson.getTeacher().getName()));
				Period p = periodMap.get(Long.parseLong(lesson.getBegin().getDay()+""+lesson.getRing().getId()));
				if( p != null ) {
					l.setPeriod(p);
				}
				Location location =  lesson.getLocation();
				if( location != null ) {
					l.setRoom(roomMap.get(location.getId()));
				}
				lessonList.add(l);
			}
				
			em.getTransaction().commit();
			
			Schedule schedule = new Schedule();
			schedule.setId(9L);
			schedule.setSchoolList(schoolList);
			schedule.setCourseList(courseList);
			schedule.setClazzList(clazzList);
			schedule.setTeacherList(teacherList);
			schedule.setRoomList(roomList);
			schedule.setPeriodList(periodList);
			schedule.setDayList(dayList);
			schedule.setTimeList(timeList);
			schedule.setLessonList(lessonList);
			schedule.setUnavailablePeriodConstraintList(unavailablePeriodConstraintList);
//			timestamp("t14:");
			Validate validate = new Validate(scoreDirector, schedule);
			brokenConstraintList = validate.validate();
//			timestamp("t15:");
			for(Object[] objectArray : brokenConstraintList) {
				System.out.println(objectArray[0] + " : " + objectArray[1] + " : " + objectArray[2] + " <=> " + objectArray[3]);
			}
//			timestamp("t16:");
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		
		return brokenConstraintList;
	}

	public List<Object[]> validate2(String schoolName, Long ringGroupId, Long periodId) {
		List<Object[]> brokenConstraintList = new ArrayList<Object[]>();
		try {
			createSolution(schoolName, ringGroupId, periodId);
			timestamp("t14:");
			Validate validate = new Validate(scoreDirector);
			brokenConstraintList = validate.validate();
			timestamp("t15:");
			for(Object[] objectArray : brokenConstraintList) {
				System.out.println(objectArray[0] + " : " + objectArray[1] + " : " + objectArray[2] + " <=> " + objectArray[3]);
			}
			timestamp("t16:");
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		
		return brokenConstraintList;
	}
	
	private void timestamp(String string ) {
		long currentTimeMillis = System.currentTimeMillis();
		System.out.println(string + (currentTimeMillis - prevTime));
		prevTime = currentTimeMillis;
	}
	
	public static void main(String [] args) {
		ScheduleMainJPA scheduleMainJPA = new ScheduleMainJPA();
		scheduleMainJPA.init();
//		scheduleMainJPA.setPlanningProblem();
//		scheduleMainJPA.setPlanningProblem2("EDU-A-J-S118", 328448L, 67162L);
		
//		scheduleMainJPA.setPlanningProblem3("EDU-A-J-S118", 328448L, 67162L);
//		scheduleMainJPA.start();
//		scheduleMainJPA.validate("EDU-A-J-S118", 328448L, 67162L);
		scheduleMainJPA.validate2("EDU-A-J-S118", 328448L, 67162L);
	}
}
