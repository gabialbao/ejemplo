package libWeb.DAO.interfaces;

import java.util.List;

import libWeb.entities.User;

public interface UserDAO 
{
	public void save(User usuario);
	public User getUsuario(long id);
	public void remove(User usuario);
	public void update(User usuario);
	public User getUsuarioLogin(String userName, String password);
	public List<User> list();
}
