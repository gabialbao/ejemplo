package libWeb.DAO.implementation;
import libWeb.DAO.interfaces.UserDAO;
import libWeb.entities.User;
import libWeb.util.*;
import java.util.List;
import org.hibernate.Session;
import org.hibernate.Transaction;

public class UserDAOimpl implements UserDAO
{

	@Override
	public void save(User usuario) 
	{
		Session session = HibernateUtil.getSessionFactory().openSession();
		Transaction t = session.beginTransaction();
		session.save(usuario);
		t.commit();
	}
	
	public User getUsuarioLogin(String userName, String password)
	{
		User rta = null;
		
		Session session = HibernateUtil.getSessionFactory().openSession();
		Transaction t = session.beginTransaction();
		@SuppressWarnings("rawtypes")
		List lista = session.createQuery("from User where userName='"+userName+"' AND password='"+password+"'").list();
		t.commit();
		
		if(!lista.isEmpty())
		{
			rta = (User)lista.get(0);
		}
		
		return rta;
	}

	public User getUsuario(long id)
	{
		Session session = HibernateUtil.getSessionFactory().openSession();
		return (User) session.load(User.class, id);
	}

	@Override
	public void update(User usuario) 
	{
		Session session = HibernateUtil.getSessionFactory().openSession();
		Transaction t = session.beginTransaction();
		session.update(usuario);
		t.commit();
	}

	@Override
	public void remove(User usuario) 
	{
		Session session = HibernateUtil.getSessionFactory().openSession();
		Transaction t = session.beginTransaction();
		session.delete(usuario);
		t.commit();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<User> list() 
	{
		Session session = HibernateUtil.getSessionFactory().openSession();
		Transaction t = session.beginTransaction();
		@SuppressWarnings("rawtypes")
		List lista = session.createQuery("from User").list();
		t.commit();
		return lista;
	}

}
