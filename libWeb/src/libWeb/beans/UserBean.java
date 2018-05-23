package libWeb.beans;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;
import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.primefaces.PrimeFaces;
import org.primefaces.component.export.ExcelOptions;
import org.primefaces.component.export.PDFOptions;

import com.lowagie.text.Document;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;

import libWeb.DAO.interfaces.UserDAO;
import libWeb.DAO.implementation.*;
import libWeb.entities.Audit;
import libWeb.entities.Leftoverbook;
import libWeb.entities.Missingbook;
import libWeb.entities.User;
import libWeb.util.RandomPassword;
import libWeb.util.Util;
import libWeb.util.JavaEmail;


@ManagedBean
@SessionScoped
public class UserBean
{
	private Leftoverbook leftO;
	private Leftoverbook leftOSeleccionado;
	private Missingbook Miss;
	private Missingbook missSeleccionado;
	private User usuario;
	private User usuarioSeleccionado;
	private String ipAddress ;
	private DataModel listaUsuarios;
	private DataModel listaAudit;
	private DataModel listaMiss;
	private DataModel listaLeft;
	private DataModel listaQuiero;
	private DataModel listaTengo;
	private JavaEmail mail;

	public UserBean()
	{
		usuario = new User();
		usuarioSeleccionado= new User();
		leftO = new Leftoverbook();
		Miss = new Missingbook();
		missSeleccionado= new Missingbook();
		leftOSeleccionado = new Leftoverbook();
	}

	public String prepararAdicionarUsuario() 
	{


		usuario = new User();
		usuario.setActive("A");
		usuario.setUserType("User");
		usuario.setDateLastPassword(new Date());


		return "/user/userRegister";
	}

	public String prepararModificarUsuario() 
	{
		usuario = (User) (listaUsuarios.getRowData());
		return "userRegister";
	}

	/*
	 * Esto es para el administrador
	 */
	public String eliminarUsuario()
	{
		User usuarioTemp = (User) (listaUsuarios.getRowData());
		UserDAO dao = new UserDAOimpl();
		usuarioTemp.setActive("I");
		dao.update(usuarioTemp);
		return "";
	}

	public String nombreSegunID()
	{

		UserDAOimpl a = new UserDAOimpl();
		User b= a.getUsuario(usuarioSeleccionado.getId());
		String nombre=b.getFullName();

		return nombre;
	}

	public String adicionarUsuario() 
	{
		UserDAO dao = new UserDAOimpl();

		String con = crearPass();
		String contrasena = Util.getStringMessageDigest(con, Util.MD5);
		usuario.setPassword(contrasena);
		dao.save(usuario);

		try {
			mail.createEmailMessage(usuario.getEmailAddress(), usuario.getFullName(), con);
		} catch (MessagingException e) {

			e.printStackTrace();
		}

		return "listarUsuarios";
	}

	public void modificarUsuario() 
	{


		UserDAO dao = new UserDAOimpl();
		dao.update(usuario);

		AuditDAOImpl daoAudit = new AuditDAOImpl();
		Audit auditoria = new Audit();
		auditoria.setCreateDate(new Date());
		auditoria.setOperation("Edito");
		auditoria.setTableId(usuario.getId());
		auditoria.setTableName("user");
		auditoria.setUserId(usuario.getId());
		auditoria.setIpAddress(clientIp());
		daoAudit.save(auditoria);

		FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, "Info.", "Edito el usuario satisfactoriamente");

		PrimeFaces.current().dialog().showMessageDynamic(message);

	}

	public User getUsuario() 
	{
		return usuario;
	}

	public void setUsuario(User usuario) 
	{
		this.usuario = usuario;
	}

	public User getUsuarioSeleccionado() {
		return usuarioSeleccionado;
	}

	public void setUsuarioSeleccionado(User usuarioSeleccionado) {
		this.usuarioSeleccionado = usuarioSeleccionado;
	}


	public DataModel ListarAuditoria()
	{
		List<Audit> lista = new AuditDAOImpl().list();
		listaAudit = new ListDataModel(lista);
		return listaAudit;

	}
	public DataModel ListarUsuarios() 
	{
		List<User> lista = new UserDAOimpl().list();
		listaUsuarios = new ListDataModel(lista);
		return listaUsuarios;
	}

	public DataModel listarMiss()
	{
		List<Missingbook> lista = new MissingBookDAOimpl().list();
		listaMiss= new ListDataModel(lista);
		return listaMiss	;
	}
	public DataModel listarMissActual()
	{
		List<Missingbook> lista = new MissingBookDAOimpl().librosUsuario(usuario.getId());
		listaQuiero= new ListDataModel(lista);
		return listaQuiero	;
	}
	public DataModel listarLeft()
	{
		List<Leftoverbook>lista = new LeftOverBookDAOimpl().list();
		listaLeft= new ListDataModel(lista);
		return listaLeft;
	}
	public DataModel listarLeftActual()
	{
		List<Leftoverbook>lista = new LeftOverBookDAOimpl().librosUsuario(usuario.getId());
		listaTengo= new ListDataModel(lista);
		return listaTengo;
	}

	public void validarEntrada()
	{

		UserDAO dao = new UserDAOimpl();

		String passHex = libWeb.util.Util.getStringMessageDigest(usuario.getPassword(), "MD5");

		User u = dao.getUsuarioLogin(usuario.getUserName(), passHex);

		if(u != null)
		{
			usuario = u;

			if(usuario.getUserName().equals("admin") && usuario.getPassword() == u.getPassword())
			{
				AuditDAOImpl daoAudit = new AuditDAOImpl();
				Audit auditoria = new Audit();
				auditoria.setCreateDate(new Date());
				auditoria.setOperation("Ingreso");
				auditoria.setTableId(usuario.getId());
				auditoria.setTableName("user");
				auditoria.setUserId(usuario.getId());
				auditoria.setIpAddress(clientIp());
				daoAudit.save(auditoria);


				try
				{
					FacesContext.getCurrentInstance().getExternalContext().redirect("./admin/adminPage.xhtml");
				}
				catch (IOException e) 
				{
					e.printStackTrace();
				}
			}
			else
			{
				AuditDAOImpl daoAudit = new AuditDAOImpl();
				Audit auditoria = new Audit();
				auditoria.setCreateDate(new Date());
				auditoria.setOperation("Ingreso");
				auditoria.setTableId(usuario.getId());
				auditoria.setTableName("user");
				auditoria.setUserId(usuario.getId());
				auditoria.setIpAddress(clientIp());
				daoAudit.save(auditoria);
				try
				{
					FacesContext.getCurrentInstance().getExternalContext().redirect("./user/userPage.xhtml");
				}
				catch (IOException e) 
				{
					e.printStackTrace();
				}
			}
		}
		else
		{

		}

	}

	public void verificarRegistro()
	{
		if(usuario.getUserName().equals(""))
		{
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Warning!", "El nombre de usuario es vacio"));
		}
		else if(usuario.getFullName().equals(""))
		{
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Warning!", "El nombre es vacio"));
		}
		else if(usuario.getEmailAddress().equals(""))
		{
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Warning!", "El correo es vacio"));
		}
		else if(usuario.getPhoneNumber().equals(""))
		{
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Warning!", "El número de celular es vacio"));
		}
		else if(usuario.getPassword().equals(""))
		{
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Warning!", "La contraseña es vacia"));
		}
		else
		{
			UserDAO dao = new UserDAOimpl();
			String pass = libWeb.util.Util.getStringMessageDigest(usuario.getPassword(), "MD5");
			usuario.setPassword(pass);
			dao.save(usuario);

			try
			{
				FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, "Info.", "Se registro exitosamente");

				PrimeFaces.current().dialog().showMessageDynamic(message);
				FacesContext.getCurrentInstance().getExternalContext().redirect("./user/userPage.xhtml");

			}
			catch (IOException e) 
			{
				e.printStackTrace();
			}
		}

	}
	public void bloquearUsuario()
	{

		AuditDAOImpl daoAudit = new AuditDAOImpl();
		Audit auditoria = new Audit();
		auditoria.setCreateDate(new Date());
		auditoria.setOperation("Bloqueo");
		auditoria.setTableId(usuario.getId());
		auditoria.setTableName("user");
		auditoria.setUserId(usuario.getId());
		auditoria.setIpAddress(clientIp());
		daoAudit.save(auditoria);

		usuarioSeleccionado.setActive("I");
		UserDAOimpl dao = new UserDAOimpl();
		dao.update(usuarioSeleccionado);
		FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, "Info.", "Se bloqueo al usuario.");

		PrimeFaces.current().dialog().showMessageDynamic(message);

	}
	public void desbloquearUsuario()
	{
		AuditDAOImpl daoAudit = new AuditDAOImpl();
		Audit auditoria = new Audit();
		auditoria.setCreateDate(new Date());
		auditoria.setOperation("Desbloqueo");
		auditoria.setTableId(usuario.getId());
		auditoria.setTableName("user");
		auditoria.setUserId(usuario.getId());
		auditoria.setIpAddress(clientIp());
		daoAudit.save(auditoria);

		usuarioSeleccionado.setActive("A");
		UserDAOimpl dao = new UserDAOimpl();
		dao.update(usuarioSeleccionado);

		FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, "Info.", "Se desbloqueo al usuario.");

		PrimeFaces.current().dialog().showMessageDynamic(message);

	}
	public String clientIp()
	{

		HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
		ipAddress = request.getHeader("X-FORWARDED-FOR");
		if (ipAddress == null) {
			ipAddress = request.getRemoteAddr();

		}
		System.out.println("ipAddress:" + ipAddress);

		return ipAddress;
	}

	public String getIpAddress() {
		return ipAddress;
	}

	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}

	public DataModel getListaUsuarios() {
		return listaUsuarios;
	}

	public void setListaUsuarios(DataModel listaUsuarios) {
		this.listaUsuarios = listaUsuarios;
	}

	public DataModel getListaAudit() {
		return listaAudit;
	}

	public void setListaAudit(DataModel listaAudit) {
		this.listaAudit = listaAudit;
	}

	public String salir()
	{
		AuditDAOImpl daoAudit = new AuditDAOImpl();
		Audit auditoria = new Audit();
		auditoria.setCreateDate(new Date());
		auditoria.setOperation("Salio ");
		auditoria.setTableId(usuario.getId());
		auditoria.setTableName("user");
		auditoria.setUserId(usuario.getId());
		auditoria.setIpAddress(clientIp());
		daoAudit.save(auditoria);

		usuario = null ;
		return "/login";
	}


	//--------------------------------------------------------------------------------------------------------------------------------------
	//
	//                            Desarrollo de documento PDF
	//        
	//--------------------------------------------------------------------------------------------------------------------------------------

	private PDFOptions pdfOpt;

	@PostConstruct
	public void init() 
	{
		inicio();
	}
	public void inicio()
	{
		excelOpt = new ExcelOptions();
		excelOpt.setFacetBgColor("#F88017");
		excelOpt.setFacetFontSize("10");
		excelOpt.setFacetFontColor("#0000ff");
		excelOpt.setFacetFontStyle("BOLD");
		excelOpt.setCellFontColor("#00ff00");
		excelOpt.setCellFontSize("8");

		pdfOpt = new PDFOptions();
		pdfOpt.setFacetBgColor("#F88017");
		pdfOpt.setFacetFontColor("#0000ff");
		pdfOpt.setFacetFontStyle("BOLD");
		pdfOpt.setCellFontSize("12");
	}
	public void preProcessPDF(Object document) {
		AuditDAOImpl daoAudit = new AuditDAOImpl();
		Audit auditoria = new Audit();
		auditoria.setCreateDate(new Date());
		auditoria.setOperation("GeneroPDF");
		auditoria.setTableId(usuario.getId());
		auditoria.setTableName("user");
		auditoria.setUserId(usuario.getId());
		auditoria.setIpAddress(clientIp());
		daoAudit.save(auditoria);
		try {
			Document pdf = (Document) document;
			pdf.open();
			pdf.setPageSize(PageSize.A4);

			ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
			String logo = externalContext.getRealPath("") + File.separator +   "loguito.png";

			pdf.add(Image.getInstance(logo));




		} catch (Exception e) {

		}       
	}

	public PDFOptions getPdfOpt() {
		return pdfOpt;
	}

	public void setPdfOpt(PDFOptions pdfOpt) {
		this.pdfOpt = pdfOpt;
	}

	//--------------------------------------------------------------------------------------------------------------------------------------
	//
	//                            Desarrollo de documento Excel
	//        
	//--------------------------------------------------------------------------------------------------------------------------------------
	private ExcelOptions excelOpt;

	public void postProcessXLS(Object document) {

		AuditDAOImpl daoAudit = new AuditDAOImpl();
		Audit auditoria = new Audit();
		auditoria.setCreateDate(new Date());
		auditoria.setOperation("Genero.XLS");
		auditoria.setTableId(usuario.getId());
		auditoria.setTableName("user");
		auditoria.setUserId(usuario.getId());
		auditoria.setIpAddress(clientIp());
		daoAudit.save(auditoria);

		HSSFWorkbook wb = (HSSFWorkbook) document;
		HSSFSheet sheet = wb.getSheetAt(0);
		HSSFRow header = sheet.getRow(0);

		HSSFCellStyle cellStyle = wb.createCellStyle();  
		cellStyle.setFillForegroundColor(HSSFColor.GREEN.index);
		cellStyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);

		for(int i=0; i < header.getPhysicalNumberOfCells();i++) {
			HSSFCell cell = header.getCell(i);

			cell.setCellStyle(cellStyle);
		}
	}

	public ExcelOptions getExcelOpt() {
		return excelOpt;
	}

	public void setExcelOpt(ExcelOptions excelOpt) {
		this.excelOpt = excelOpt;
	}



	//--------------------------------------------------------------------------------------------------------------------------------------
	//
	//                            Leftoverbook
	//        
	//--------------------------------------------------------------------------------------------------------------------------------------






	public Leftoverbook getLeftO() {
		return leftO;
	}

	public void setLeftO(Leftoverbook leftO) {
		this.leftO = leftO;
	}

	public void agregarqueQuiero()
	{
		LeftOverBookDAOimpl daoLefto =new LeftOverBookDAOimpl();
		leftO.setAuthor(leftO.getAuthor());
		leftO.getDate();
		leftO.setEdition(leftO.getEdition());
		leftO.setEditorial(leftO.getEditorial());
		leftO.setName(leftO.getName());
		leftO.setUserId(usuario.getId());
		daoLefto.save(leftO);


		AuditDAOImpl daoAudit = new AuditDAOImpl();
		Audit auditoria = new Audit();
		auditoria.setCreateDate(new Date());
		auditoria.setOperation("LibQuiere");
		auditoria.setTableId(usuario.getId());
		auditoria.setTableName("Left");
		auditoria.setUserId(usuario.getId());
		auditoria.setIpAddress(clientIp());
		daoAudit.save(auditoria);

		FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, "Info.", "Agrego satisfactoriamente un libro que quiere");

		PrimeFaces.current().dialog().showMessageDynamic(message);


	}


	//--------------------------------------------------------------------------------------------------------------------------------------
	//
	//                            Missingbook
	//        
	//--------------------------------------------------------------------------------------------------------------------------------------

	public Missingbook getMiss() {
		return Miss;
	}

	public void setMiss(Missingbook miss) {
		Miss = miss;
	}


	public void agregarqueTengo()
	{
		MissingBookDAOimpl daoMiss = new MissingBookDAOimpl();
		//		Miss.setAuthor(Miss.getAuthor());
		//		Miss.getDate();
		//		Miss.setEdition(Miss.getEdition());
		//		Miss.setEditorial(Miss.getEditorial());
		//		Miss.setName(Miss.getName());
		Miss.setUserId(usuario.getId());
		daoMiss.save(Miss);


		AuditDAOImpl daoAudit = new AuditDAOImpl();
		Audit auditoria = new Audit();
		auditoria.setCreateDate(new Date());
		auditoria.setOperation("LibTengo");
		auditoria.setTableId(usuario.getId());
		auditoria.setTableName("Miss");
		auditoria.setUserId(usuario.getId());
		auditoria.setIpAddress(clientIp());
		daoAudit.save(auditoria);

		FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, "Info.", "Agrego satisfactoriamente un libro que tiene");

		PrimeFaces.current().dialog().showMessageDynamic(message);



	}
	//--------------------------------------------------------------------------------------------------------------------------------------
	//
	//                            Contactar
	//        
	//--------------------------------------------------------------------------------------------------------------------------------------



	public void comunicarporCorreo() 
	{

	}

	public void comunicarporTelefono() 
	{

	}

	public void ubicacionUsuario() 
	{

	}
	//--------------------------------------------------------------------------------------------------------------------------------------
	//
	//                           contraseña
	//        
	//--------------------------------------------------------------------------------------------------------------------------------------

	public String crearPass()
	{
		String pass= (int)(100000 * Math.random())+"";



		return pass;
	}

	//--------------------------------------------------------------------------------------------------------------------------------------
	//
	//                           prueba
	//        
	//--------------------------------------------------------------------------------------------------------------------------------------
//	public boolean filterByPrice(Object value, Object filter, Locale locale) {
//		String filterText = (filter == null) ? null : filter.toString().trim();
//		if(filterText == null||filterText.equals("")) {
//			return true;
//		}
//
//		if(value == null) {
//			return false;
//		}
//
//		return ((Comparable) value).compareTo(Integer.valueOf(filterText)) > 0;
//	}
//	public List<Missingbook> getFilteredbook() {
//		List<Missingbook> filteredbook = null;
//		return filteredbook;
//	}

	//--------------------------------------------------------------------------------------------------------------------------------------
	//
	//                         Eliminar y editar libros
	//        
	//--------------------------------------------------------------------------------------------------------------------------------------



	public void eliminarQuiero() 
	{
		
//		AuditDAOImpl daoAudit = new AuditDAOImpl();
//		Audit auditoria = new Audit();
//		auditoria.setCreateDate(new Date());
//		auditoria.setOperation("EliminoLibQ");
//		auditoria.setTableId(usuario.getId());
//		auditoria.setTableName("user");
//		auditoria.setUserId(usuario.getId());
//		auditoria.setIpAddress(clientIp());
//		daoAudit.save(auditoria);
//		
//		
//		MissingBookDAOimpl mi = new MissingBookDAOimpl();
//		mi.remove(missSeleccionado);

		FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, "Info.", "Elimino satisfactoriamente un libro que quiere");

		PrimeFaces.current().dialog().showMessageDynamic(message);
	}

	public Missingbook getMissSeleccionado() {
		return missSeleccionado;
	}

	public void setMissSeleccionado(Missingbook missSeleccionado) {
		this.missSeleccionado = missSeleccionado;
	}
	
	public void eliminarTengo() 
	{
//		
//		AuditDAOImpl daoAudit = new AuditDAOImpl();
//		Audit auditoria = new Audit();
//		auditoria.setCreateDate(new Date());
//		auditoria.setOperation("ElimLibT");
//		auditoria.setTableId(usuario.getId());
//		auditoria.setTableName("user");
//		auditoria.setUserId(usuario.getId());
//		auditoria.setIpAddress(clientIp());
//		daoAudit.save(auditoria);
//		
//		
//		LeftOverBookDAOimpl le = new LeftOverBookDAOimpl();
//		le.remove(leftOSeleccionado);
		FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, "Info.", "Elimino satisfactoriamente un libro que tiene");

		PrimeFaces.current().dialog().showMessageDynamic(message);
		
	}

	public Leftoverbook getLeftOSeleccionado() {
		return leftOSeleccionado;
	}

	public void setLeftOSeleccionado(Leftoverbook leftOSeleccionado) {
		this.leftOSeleccionado = leftOSeleccionado;
	}
	
	
}