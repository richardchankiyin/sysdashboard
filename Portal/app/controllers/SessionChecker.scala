package controllers

import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.Logger
import org.slf4j.LoggerFactory
import scala.util.Either

object SessionChecker extends SessionConfig{
  val logger = Logger(LoggerFactory.getLogger(this.getClass))
  val config = ConfigFactory.load("app")
  val timeout = config.getInt("session_timeout_second")
  // result Either[loginIn,timeout]
  def isLogin(data:Map[String,String]):Either[Boolean,Boolean] = {
    logger.debug("data: {}", data)
    if (data == null) {
      Left(false)
    } else {
      data.get(session_var_user) match {
        case Some(e) => {
          //further checking whether timeout
          data.get(session_var_last_activity_time) match {
            case Some(e) => {
              val lastActivityTime = java.time.Instant.parse(e)
              isTimeout(lastActivityTime, timeout) match {
                case true => Right(true)
                case false => Left(true)
              }
            }
            case None => Left(false)
          }
        }
        case None => {
          Left(false)
        }
      }
    }
  }
  
  def isTimeout(lastActivityTime:java.time.Instant, maxTimeOfInactiveInSec:Int):Boolean = {
    val current = java.time.Instant.now
    val expectedLastActivityTime = current.minusSeconds(maxTimeOfInactiveInSec)
    logger.debug("lastActivityTime: {} expectedLastActivityTime: {}", lastActivityTime, expectedLastActivityTime)
    val result = lastActivityTime.isBefore(expectedLastActivityTime)
    logger.debug("result: {}", {result.toString()})
    result
  }
  
  def cleanse {
    logger.info("cleansingSession started....")
    logger.info("Session Manager: {}", SessionManager)
    val uuids = SessionManager.getAllUUID()
    uuids.foreach {
      e => {
        SessionManager.getSessionDetail(e) match {
          case Some(value) => {
            isLogin(value._1) match {
              case Right(r) => {
                r match {
                  case true => {
                    logger.info("cleaning start for uuid: {}", e)
                    SessionManager.dropSession(e)
                  }
                  case false => logger.warn("something weird -- uuid: {}. What do you mean by timeout is false??", e)
                }
              }
              case _ => logger.debug("ignoring uuid: {}", e)
            }
          }
          case None => logger.warn("something weird -- uuid: {} cannot be found in the SessionManager", e)
        }
      }
    }
  }
}