package controllers

import play.api.mvc._
import org.slf4j.LoggerFactory
import com.typesafe.scalalogging.Logger

object RequestActivityUpdater extends SessionConfig{
  val logger = Logger(LoggerFactory.getLogger(this.getClass))
  
  def updateLastActivityTime(session:Session):Session = {
    require(session != null)
    
    logger.debug("Session before: {}", session)
    session - session_var_last_activity_time
    val newSession = session + (session_var_last_activity_time -> java.time.Instant.now().toString())
    logger.debug("Session after: {}", newSession)
    val uuid = session.get(session_var_uuid) match {
      case Some(v) => SessionManager.updateSessionValue(v, newSession.data)
      case None => throw new IllegalArgumentException("uuid not found!")
    }
    newSession
  }
  
  def handleLogout(session:Session) {
    if (session != null) {
      logger.debug("Session to be logout: {}", session)
      val uuid = session.get(session_var_uuid) match {
        case Some(v) => SessionManager.dropSession(v)
        case None => logger.debug("not login")
      }
    }
  }
  
}