package com.richardchankiyin.alert

import org.scalatest.FlatSpec
import com.typesafe.scalalogging.Logger
import org.slf4j.LoggerFactory
import com.richardchankiyin.log.LogKeeper
import com.richardchankiyin.log.LogItem
import com.dumbster.smtp.SimpleSmtpServer
import com.dumbster.smtp.SmtpMessage

class MailAlertTest extends FlatSpec{
  val logger = Logger(LoggerFactory.getLogger(this.getClass))
  
  "MailAlert" should "send 1 issue email when receiving 1 success and 1 unsuccess log" in {
    
    val smtpServer = SimpleSmtpServer.start(5008)
    
    val alert = MailAlert("mailalert_1")
    val logKeeper = LogKeeper("logkeeper")
    logKeeper.registerListener(alert)
    val okLogItem = new LogItem("", "ok job", java.time.Instant.now(), true, "ok job remarks")
    val errorLogItem = new LogItem("", "error job", java.time.Instant.now(), false, "error job remarks")
    logKeeper.addLogItem(okLogItem)
    logKeeper.addLogItem(errorLogItem)
    
    smtpServer.stop()
    
    assert(smtpServer.getReceivedEmailSize == 1)
    val mail:SmtpMessage = smtpServer.getReceivedEmail.next().asInstanceOf[SmtpMessage]
    val title = mail.getHeaderValue("Subject")
    val body = mail.getBody
    
    logger.debug("title: {}", title)
    logger.debug("body: {}", body)
    
    assert(title.contains("ERR"))
    assert("error job remarks".equals(body))
    
  }
}