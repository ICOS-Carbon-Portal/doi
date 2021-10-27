package se.lu.nateko.cp.doi

import javax.mail._
import javax.mail.internet._
import java.util.Date
import java.util.Properties
import org.slf4j.LoggerFactory
import scala.util.Try
import scala.util.Success

class Mailer(config: EmailConfig) {

	private val log = LoggerFactory.getLogger(getClass)

	def send(to: Seq[String], subject: String, body: String): Unit = {

		try{
			val message: Message = {
				val properties = new Properties()
				properties.put("mail.smtp.auth", "true")
				properties.put("mail.smtp.starttls.enable", "true")
				properties.put("mail.smtp.host", config.smtpServer)
				properties.put("mail.smtp.port", "587")
				val session = Session.getDefaultInstance(properties, new Authenticator{
					override def getPasswordAuthentication = new PasswordAuthentication(config.username, config.password)
				})
				new MimeMessage(session)
			}

			message.setFrom(new InternetAddress(config.fromAddress))
			message.setReplyTo(Array(new InternetAddress("do_not_reply@icos-cp.eu")))
			to.foreach(r => message.addRecipient(Message.RecipientType.TO, new InternetAddress(r)))

			message.setSentDate(new Date())
			message.setSubject(subject)

			message.setContent(body, "text/html; charset=utf-8")

			Transport.send(message)
		}catch{
			case err: Throwable =>
				log.error("Mail sending failed", err)
				throw err
		}
	}
}
