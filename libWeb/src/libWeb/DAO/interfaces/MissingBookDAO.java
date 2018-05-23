package libWeb.DAO.interfaces;

import java.util.List;

import libWeb.entities.Audit;
import libWeb.entities.Missingbook;

public interface MissingBookDAO 
{
	public void save(Missingbook missBook);
	public Missingbook getMissingBook(Missingbook missBook);
	public void remove(Missingbook missBook);
	public void update(Missingbook missBook);
	public List<Missingbook> list();
	public List<Missingbook> librosUsuario(int id);
}
