package com.richardchankiyin.alert

import com.typesafe.config.ConfigFactory
import org.slf4j.LoggerFactory
import com.typesafe.scalalogging.Logger
import com.richardchankiyin.log.LogItem
import com.richardchankiyin.log.LogChangeListener
import scala.collection.JavaConversions._
import scala.collection.mutable.MutableList
import com.richardchankiyin.mail.MailSender
import scala.util.{Try,Success,Failure}

class MailAlert(configName:String) extends LogChangeListener{
  // only set to false when debugging!!!!
  lazy val maskingPasswordRequired = true
  
  lazy val logger = Logger(LoggerFactory.getLogger(this.getClass))
  
  logger.debug("configName: {}", configName)
  
  private val config = ConfigFactory.load(configName)
  
  private val username = config.getString("username")
  private val password = config.getString("password")
  private val host = config.getString("host")
  private val port = config.getInt("port")
  private val receivers = config.getStringList("receivers")
  
  private val anyExtra = config.getIsNull("mailserver_extra")
  private var extraMailConfig:List[(String,String)] = null
  if (anyExtra == false) {
    val listInProgress = MutableList[(String,String)]()
    // get extra fields here
    val extraFields = config.getStringList("mailserver_extra").toList
    extraFields.foreach{x=> {
      listInProgress += ((x,config.getString(x)))
    }}
    logger.debug("listInProgress: {}", listInProgress)
    extraMailConfig = listInProgress.toList
  }
  
  logger.debug("username: {}, password: {}, host: {}, port: {}, extraconfig: {}",
      username, {if (maskingPasswordRequired) "*****" else password}, host, {port.toString()}
      , extraMailConfig)
  
  
  // setting up mail server here
  val mailServer = MailSender(host, port, extraMailConfig, username, password)
  
  
  
  def onReceiveNewLog(item:LogItem) {
    notify(item)
  }
  
  def onReceiveDeletedLogs(items:List[LogItem]) {
    
  }
  
  def getErrorMailAlertSubject(item:LogItem):String = {
    if (item != null)
      s"[ERR!!!][Incident Time:${item.getTime}]Issue found when running [${item.getJobDesc}]"
    else
      ""
  }
  
  def notify(item:LogItem) {
    if (item != null) {
      item.isLogSuccess match {
        case true => {
          // not sending mail
          logger.debug("do not send mail")
        } //end case true
        case false => {
          Try(mailServer.sendMail(username, receivers.toArray(Array[String]())
              ,getErrorMailAlertSubject(item),item.getRemarks)) match {
            case Success(e) => {logger.debug("email sent...")}
            case Failure(t) => {logger.error("email sending encountered problem", t)}
          } //end try
          
        } //end case false
      } //end item.isLogSuccess match
    } //end if item != null
  }
}


object MailAlert {
  def apply(configName:String) = new MailAlert(configName)
}