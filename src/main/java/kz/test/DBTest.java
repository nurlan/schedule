package kz.test;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import kz.bee.kudos.course.Course;
import kz.bee.wx.security.Group;
import kz.bee.wx.security.User;

public class DBTest {


	public static void main(String[] args) {
		
		EntityManagerFactory emf = Persistence.createEntityManagerFactory("slrs");
		EntityManager em = emf.createEntityManager();
		em.getTransaction().begin();
		
		List<Course> courses = em.createQuery("select c from Course c").getResultList();
		List<kz.bee.kudos.ou.Class> classes = em.createQuery("select c from Class c").getResultList();
		List<User> teachers = em.createQuery("select m.user from Membership m where m.role.name='TEACHER'").getResultList();
		
		List<Group> groups = em.createQuery("select g from Group g").getResultList();
		
		for(Group i : groups) {
			System.out.println("Group: " + i.getName());
		}
		
		em.getTransaction().commit();
		em.close();
		emf.close();
		
		
	}
	

}
