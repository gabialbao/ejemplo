package libWeb.util;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class JavaEmail 
{

	Properties emailProperties;
	Session mailSession;
	MimeMessage emailMessage;


	public void enviarPass(String email,String user, String pass)  {

		setMailServerProperties();
		try 
		{
			createEmailMessage(email,user, pass);
			sendEmail();
		} 
		catch (AddressException e)
		{
			e.printStackTrace();
		} 
		catch (MessagingException e) 
		{
			e.printStackTrace();
		}

	}

	public void setMailServerProperties() {

		String emailPort = "587";//gmail's smtp port

		emailProperties = System.getProperties();
		emailProperties.put("mail.smtp.port", emailPort);
		emailProperties.put("mail.smtp.auth", "true");
		emailProperties.put("mail.smtp.starttls.enable", "true");

	}

	public void createEmailMessage(String email,String user, String pass) throws AddressException,
	MessagingException {
		String[] toEmails = {email};
		String emailSubject = "Bienvenido a libWeb.";
		String emailBody = "Se�or usuario(a) "+user+" su contrase�a es: "+pass+".\nRecuerde cambiar su contrase�a.\nAtentamente: libWeb.";

		mailSession = Session.getDefaultInstance(emailProperties, null);
		emailMessage = new MimeMessage(mailSession);

		for (int i = 0; i < toEmails.length; i++) {
			emailMessage.addRecipient(Message.RecipientType.TO, new InternetAddress(toEmails[i]));
		}

		emailMessage.setSubject(emailSubject);
		emailMessage.setContent(emailBody, "text/html");//for a html email
		//emailMessage.setText(emailBody);// for a text email

	}

	public void sendEmail() throws AddressException, MessagingException {

		String emailHost = "smtp.gmail.com";
		String fromUser = "libweb2018";//just the id alone without @gmail.com
		String fromUserEmailPassword = "pollo0208";

		Transport transport = mailSession.getTransport("smtp");

		transport.connect(emailHost, fromUser, fromUserEmailPassword);
		transport.sendMessage(emailMessage, emailMessage.getAllRecipients());
		transport.close();

	}

}


