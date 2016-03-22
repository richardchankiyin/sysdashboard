package controllers


import play.api.libs.iteratee.Concurrent.Channel
import com.fasterxml.jackson.databind.ObjectMapper

import org.slf4j.LoggerFactory
import com.typesafe.scalalogging.Logger
import java.io.StringWriter
import com.richardchankiyin.log.LogItem
import com.richardchankiyin.log.LogChangeListener
import com.richardchankiyin.log.LogKeeper
import scala.util.{Try,Success,Failure}


class LogItemPusher(uuid:String, logKeeper:LogKeeper, channel:Channel[String]) extends LogChangeListener with SessionUpdateListener with PortalWebSocketMessages{
  lazy val logger = Logger(LoggerFactory.getLogger(this.getClass))
  
  require(uuid != null && logKeeper != null && channel != null, "all parameters must be non-null")
  
  logKeeper.registerListener(this)
  SessionManager.registerSessionUpdateListener(uuid, this)
  
  def onReceiveNewLog(item:LogItem) {
    if (item != null) {
      lazy val out = new StringWriter
      lazy val mapper = new ObjectMapper()
      
      mapper.writeValue(out, new LogItemPresentation(item))
      lazy val json = out.toString()
      Try(channel.push(json)) match {
        case Success(e) => logger.debug("msg: [{}] sent", json)
        case Failure(t) => logger.error("msg: [{}] sending got issue:", json, t)
      }
    }
  }
  
  def onReceiveDeletedLogs(items:List[LogItem]) {}
  
  def onSessionUpdate(update:Map[String,String]) {}
  
  def onSessionDrop() {
    // disconnect
    logger.debug("session has been dropped for uuid: {}", uuid)
    logKeeper.unregisterListener(this)
    channel.push(unauthorized_msg)
  }

  
}

class LogItemPresentation(item:LogItem) {
  
  def this() = this(null)
  
  def getSchedule = item.getSchedule
  def getJobDesc = item.getJobDesc
  def getTime = item.getTime.toString()
  def getRemarks = item.getRemarks
  def isLogSuccess = item.isLogSuccess
  
  var _schedule = ""
  var _jobDesc = ""
  var _time = ""
  var _remarks = ""
  var _logSuccess = false
  
  def setSchedule(str:String) { _schedule = str }
  def setJobDesc(str:String) { _jobDesc = str }
  def setTime(str:String) {_time = str}
  def setRemarks(str:String) = {_remarks = str}
  def setLogSuccess(bool:Boolean) = {_logSuccess = bool}
}