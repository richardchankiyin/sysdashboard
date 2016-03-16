package controllers

import org.scalatest.FlatSpec
import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.Logger
import org.slf4j.LoggerFactory

class SessionCheckerTest extends FlatSpec with SessionConfig{
  val logger = Logger(LoggerFactory.getLogger(this.getClass))
  
  "SessionChecker" should "return false login result if map is null" in {
    SessionChecker.isLogin(null) match {
      case Left(r) => assert(!r)
      case Right(r) => fail("unexpected")
    }
  }
  
  "SessionChecker" should "return false login result if map is empty" in {
    SessionChecker.isLogin(Map()) match {
      case Left(r) => assert(!r)
      case Right(r) => fail("unexpected")
    }
  }
  
  "SessionChecker" should "return timeout result if last activity time is far away" in {
    val config = ConfigFactory.load("app")
    val timeout = config.getInt("session_timeout_second")
    val current = java.time.Instant.now()
    val login = current.minusSeconds(1000)
    val lastActivity = current.minusSeconds(timeout)
    val actualLastActivity = lastActivity.minusSeconds(10)
    
    logger.debug("lastActivity: {} actualLastActivity: {}", lastActivity, actualLastActivity)
    
    val map = Map[String,String] (
      session_var_user -> "xxx"
      , session_var_logintime -> login.toString()
      , session_var_last_activity_time -> actualLastActivity.toString()
    )
    
    SessionChecker.isLogin(map) match {
      case Left(r) => {logger.debug("result: {}", {r.toString()}); fail("unexpected")}
      case Right(r) => assert(r)
    }
  }
  
  "SessionChecker" should "return true login result" in {
    val config = ConfigFactory.load("app")
    val timeout = config.getInt("session_timeout_second")
    val current = java.time.Instant.now()
    val login = current.minusSeconds(1000)
    val lastActivity = current.minusSeconds(timeout)
    val actualLastActivity = lastActivity.plusSeconds(10)
    
    logger.debug("lastActivity: {} actualLastActivity: {}", lastActivity, actualLastActivity)
    
    val map = Map[String,String] (
      session_var_user -> "xxx"
      , session_var_logintime -> login.toString()
      , session_var_last_activity_time -> actualLastActivity.toString()
    )
    
    SessionChecker.isLogin(map) match {
      case Left(r) => assert(r)
      case Right(r) => {logger.debug("result: {}", {r.toString()}); fail("unexpected")}
    }
  }
}