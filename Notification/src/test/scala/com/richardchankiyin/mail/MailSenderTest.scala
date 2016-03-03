package com.richardchankiyin.mail

import org.scalatest.FlatSpec
import com.typesafe.scalalogging.Logger
import org.slf4j.LoggerFactory
import com.dumbster.smtp.SimpleSmtpServer
import scala.util.{Try,Success,Failure}
import com.dumbster.smtp.SmtpMessage


class MailSenderTest extends FlatSpec{
  val logger = Logger(LoggerFactory.getLogger(this.getClass))
  
  "MailSender" should "fail to send mail due to port no incorrect" in {
    val host = "localhost"
    val port = 5000
    val from = "aaa@aaa.com"
    val to = Array("bbb@bbb.com","ccc@ccc.com")
    val subject = "Test_Subject"
    val text = "Test_Text"
    
    val smtpServer = SimpleSmtpServer.start(port+1)
    val mailSender = MailSender(host, port, null, null, null)
    
    Try(mailSender.sendMail(from, to, subject, text)) match {
      case Success(e)=>{logger.debug("sent message");fail("msg sent! unexpected")}
      case Failure(e)=>{logger.error("failed to send message", e)}
    }
    
    smtpServer.stop()
  }
  
  "MailSender" should "send message to two receivers" in {
    val host = "localhost"
    val port = 5000
    val from = "aaa@aaa.com"
    val to = Array("bbb@bbb.com","ccc@ccc.com")
    val subject = "Test_Subject"
    val text = "Test_Text"
    
    val smtpServer = SimpleSmtpServer.start(port)
    val mailSender = MailSender(host, port, null, null, null)
    
    Try(mailSender.sendMail(from, to, subject, text)) match {
      case Success(e)=>logger.debug("sent message")
      case Failure(e)=>{logger.error("failed to send message", e); fail("failed....")}
    }
    
    smtpServer.stop()
    
    logger.debug("to: {} length: {}", {to.toList}, {to.length.toString})
    
    assert(1==smtpServer.getReceivedEmailSize)
    
    val mail:SmtpMessage = smtpServer.getReceivedEmail.next().asInstanceOf[SmtpMessage]
    val title = mail.getHeaderValue("Subject")
    val body = mail.getBody
    
    logger.debug("title: {}", {title})
    logger.debug("body: {}", {body})
    assert(subject.equals(title))
    assert(text.equals(body))
    
    
  }
}