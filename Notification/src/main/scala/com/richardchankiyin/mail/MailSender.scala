package com.richardchankiyin.mail

import java.util.Properties
import com.typesafe.scalalogging.Logger
import org.slf4j.LoggerFactory
import javax.mail.Session
import javax.mail.internet.MimeMessage
import javax.mail.internet.InternetAddress
import javax.mail.Message
import javax.mail.Transport
import javax.mail.Authenticator
import javax.mail.PasswordAuthentication

case class MailSender(val host:String="localhost", val port:Int=443
    , val extraConfig:List[(String,String)], val username:String, val password:String) {
  val logger = Logger(LoggerFactory.getLogger(this.getClass))
  private val prop:Properties = new Properties()
  prop.setProperty("mail.smtp.host",host)
  prop.setProperty("mail.smtp.port",port.toString())
  if (extraConfig != null) {
    extraConfig.foreach{f:((String,String))=>prop.setProperty(f._1, f._2)}
  }
  
  var authenticator:Authenticator = null
  
  if (username!=null && password!= null) {
    authenticator = new Authenticator {
      override def getPasswordAuthentication:PasswordAuthentication 
        = new PasswordAuthentication(username,password)
    }
  }
  
  private val session = Session.getInstance(prop, authenticator)
  
  def sendMail(from:String, to:Array[String], subject:String, text:String) {
    logger.debug("\nfrom: {}\nto: {}\nsubject: {}\ntext:{}\n", from, to, subject, text)
    var actualFrom = ""
    if (from == null || "".equals(from.trim()))
      actualFrom = username
    else
      actualFrom = from
    
    require(actualFrom != null && to != null && !"".equals(actualFrom.trim()) && !to.contains(""), "from and to cannot be blank!")
    val message = new MimeMessage(session)
    message.setFrom(new InternetAddress(from))
    
    to.foreach { x => message.addRecipients(Message.RecipientType.BCC, x) }
    message.setSubject(subject)
    message.setText(text)
    Transport.send(message)
    logger.debug("message sent successfully at: {}", {new java.util.Date().toString()})
  }
}