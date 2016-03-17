package controllers

import com.typesafe.scalalogging.Logger
import org.slf4j.LoggerFactory
import org.scalatest.FlatSpec
class SessionManagerTest extends FlatSpec{
  val logger = Logger(LoggerFactory.getLogger(this.getClass))
  val listener1 = new CustomListener((m:Map[String,String]) => {})
  val listener2 = new CustomListener((m:Map[String,String]) => {})

  "SessionManager" should "create session successfully" in {
    logger.debug("SessionManager Before: {}", SessionManager)
    assert(SessionManager.createSession("1", Map("item1"->"item1Val")))
    logger.debug("SessionManager After: {}", SessionManager)
    SessionManager.getSessionDetail("1") match {
      case Some(v) => assert(Map("item1"->"item1Val").equals(v._1))
      case None => fail("unexpected!")
    }
  }
  
  "SessionManager" should "fail to create session" in {
    logger.debug("SessionManager Before: {}", SessionManager)
    assert(!SessionManager.createSession("1", Map("item2"->"item2Val")))
    logger.debug("SessionManager After: {}", SessionManager)
    SessionManager.getSessionDetail("1") match {
      case Some(v) => assert(Map("item1"->"item1Val").equals(v._1))
      case None => fail("unexpected!")
    }
  }
  
  "SessionManager" should "update session values successfully" in {
    logger.debug("SessionManager Before: {}", SessionManager)
    SessionManager.createSession("2", Map("item2"->"item2Val"))
    SessionManager.updateSessionValue("1", Map("item1"->"item1NewVal"))
    SessionManager.updateSessionValue("2", Map("item2"->"item2NewVal"))
    logger.debug("SessionManager After: {}", SessionManager)
    SessionManager.getSessionDetail("1") match {
      case Some(v) => assert(Map("item1"->"item1NewVal").equals(v._1))
      case None => fail("unexpected!")
    }
    SessionManager.getSessionDetail("2") match {
      case Some(v) => assert(Map("item2"->"item2NewVal").equals(v._1))
      case None => fail("unexpected!")
    }
  }
  
  "SessionManager" should "register listeners successfully" in {
    logger.debug("SessionManager Before: {}", SessionManager)
    SessionManager.registerSessionUpdateListener("1", listener1)
    SessionManager.registerSessionUpdateListener("2", listener2)
    logger.debug("SessionManager After: {}", SessionManager)
    SessionManager.getSessionDetail("1") match {
      case Some(v) => assert(v._2.size == 1)
      case None => fail("unexpected!")
    }
    SessionManager.getSessionDetail("2") match {
      case Some(v) => assert(v._2.size == 1)
      case None => fail("unexpected!")
    }
  }
  
  "SessionManager" should "retrieve set of uuid successfully" in {
    logger.debug("SessionManager Before: {}", SessionManager)
    assert(SessionManager.getAllUUID.equals(Set("1","2")))
    logger.debug("SessionManager After: {}", SessionManager)
  }
  
  
  "SessionManager" should "be able to fire onSessionUpdate events to listeners" in {
    logger.debug("SessionManager Before: {}", SessionManager)
    listener1._f = (m:Map[String,String]) => {
      logger.debug("alerting listener1: {}", m)
      assert(m.equals(Map("item1"->"item1NewVal_v2")))
    }
    listener2._f = (m:Map[String,String]) => {
      logger.debug("alerting listener2: {}", m)
      assert(m.equals(Map("item2"->"item2NewVal_v2")))
    }
    SessionManager.updateSessionValue("1", Map("item1"->"item1NewVal_v2"))
    SessionManager.updateSessionValue("2", Map("item2"->"item2NewVal_v2"))
    logger.debug("SessionManager After: {}", SessionManager)
    assert(listener1.count.get == 1)
    assert(listener2.count.get == 1)
  
  }
  
  "SessionManager" should "encounter error when dropping a non-existing uuid" in {
    logger.debug("SessionManager Before: {}", SessionManager)
    SessionManager.dropSession("3") match {
      case true => fail("unexpect")
      case false => logger.debug("no dropping is done")
    }
    logger.debug("SessionManager After: {}", SessionManager)
  }
  
  "SessionManager" should "drop uuid successfully" in {
    logger.debug("SessionManager Before: {}", SessionManager)
    SessionManager.getSessionDetail("1") match {
      case Some(e) => logger.debug("expected")
      case None => fail("unexpected")
    }
    
    SessionManager.dropSession("1") match {
      case true => logger.debug("dropped successfully")
      case false => fail("unexpected")
    }
    
    assert(listener1.exist == false)
    
    SessionManager.getSessionDetail("1") match {
      case Some(e) => fail("unexpected")
      case None => logger.debug("expected")
    }
    
    logger.debug("SessionManager After: {}", SessionManager)
  }
}

class CustomListener(f:Map[String,String]=>Unit) extends SessionUpdateListener {
  val count = new java.util.concurrent.atomic.AtomicInteger()
  var exist = true
  var _f = f
  def onSessionUpdate(update:Map[String,String]) {
    count.incrementAndGet()
    _f(update)
  }
  def onSessionDrop() {
    exist = false
  }
}