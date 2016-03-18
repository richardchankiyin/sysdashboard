package controllers

import org.scalatest.FlatSpec
import com.richardchankiyin.log.LogKeeper
import play.api.libs.iteratee.Concurrent.Channel
import org.scalamock.scalatest.MockFactory
import org.slf4j.LoggerFactory
import com.typesafe.scalalogging.Logger
import scala.util.{Try,Success,Failure}
import com.richardchankiyin.log.LogItem
import scalaz.stream.time
import play.api.libs.iteratee.Input
import com.fasterxml.jackson.databind.ObjectMapper


class LogItemPusherTest extends FlatSpec with SessionConfig with MockFactory{
  lazy val logger = Logger(LoggerFactory.getLogger(this.getClass))
  lazy val logKeeper = LogKeeper("app")
  
  SessionManager.createSession("111", Map(session_var_user->"user"
                     , session_var_logintime->java.time.Instant.now().toString()
                     , session_var_last_activity_time->java.time.Instant.now().toString()
                     , session_var_uuid->"111"))
  
  class ChannelFake extends Channel[String] {
      var _str = "";
      override def push(str:String) = {
        logger.debug("channel.push: {}", str)
        _str = str
      }
      
      def currentPush:String = _str
      
      override def end = {}
      override def end(t:Throwable) = {}
      override def push(i:Input[String]) = {}
  }

                     
  "LogItemPusher" should "reject for any null input" in {
    lazy val channel = mock[Channel[String]]
    
    var pusher = null
    
    Try(new LogItemPusher(null, logKeeper, channel)) match {
      case Failure(t) => logger.debug("expected")
      case Success(e) => fail("unexpected")
    }
    
    Try(new LogItemPusher("111", null, channel)) match {
      case Failure(t) => logger.debug("expected")
      case Success(e) => fail("unexpected")
    }
    
    Try(new LogItemPusher("111", logKeeper, null)) match {
      case Failure(t) => logger.debug("expected")
      case Success(e) => fail("unexpected")
    }
    
    Try(new LogItemPusher("111", logKeeper, channel)) match {
      case Failure(t) => {logger.error("", t);fail("unexpected")}
      case Success(e) => {logger.debug("expected"); logKeeper.unregisterListener(e)}
    }
    
    
  }
  
  "LogItemPusher" should "receive LogItem json from onReceiveNewLog event" in {
    val channel = new ChannelFake()
    
    val logPusher = new LogItemPusher("111", logKeeper, channel)
    
    val currentTime = java.time.Instant.now
    
    val logItem = new LogItem(schedule="schedule", jobDesc="jobDesc", time=currentTime, isSuccess=true, remarks="remarks")
    
    val json = "{\"time\":\"" + currentTime + "\",\"jobDesc\":\"jobDesc\",\"logSuccess\":true,\"schedule\":\"schedule\",\"remarks\":\"remarks\"}"
    
    logKeeper.addLogItem(logItem)
    
    val result = channel.currentPush
    
    logger.debug("Channel Fake return: {}", result)

    logger.debug("expected return: {}", json)
    
    lazy val mapper = new ObjectMapper()
    
    val originObject:LogItemPresentation = mapper.readValue(result, java.lang.Class.forName("controllers.LogItemPresentation")).asInstanceOf[LogItemPresentation]
    
    logger.debug("originObject: {}", {originObject.toString})
    
    assert(originObject._jobDesc.equals("jobDesc"))
    assert(originObject._schedule.equals("schedule"))
    assert(originObject._remarks.equals("remarks"))
    assert(originObject._logSuccess == true)
  }
}