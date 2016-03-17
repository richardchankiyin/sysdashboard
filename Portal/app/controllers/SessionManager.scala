package controllers

import scala.collection.mutable.ListBuffer
import scala.collection.mutable.Map
import com.typesafe.scalalogging.Logger
import org.slf4j.LoggerFactory
import scala.util.{Try,Success,Failure}

object SessionManager extends SessionConfig{
  val logger = Logger(LoggerFactory.getLogger(this.getClass))
  private val internalControl:Map[String, (scala.collection.immutable.Map[String,String],ListBuffer[SessionUpdateListener])] = Map()
  
  override def toString:String = s"SessionManager $internalControl"
  
  def createSession(uuid:String, value:scala.collection.immutable.Map[String,String]):Boolean = {
    internalControl.contains(uuid) match {
      case true => false
      case false => {
        internalControl.put(uuid,(value,new ListBuffer[SessionUpdateListener]()))
        true
      }
    }
  }
  
  def updateSessionValue(uuid:String, value:scala.collection.immutable.Map[String,String]) {
    require(internalControl.contains(uuid))
    
    val storage = internalControl.get(uuid)
    storage match {
      case Some(v) => {
        internalControl.put(uuid, (value,v._2))
        // notify listeners
        val listeners = v._2
        listeners.foreach {
          l => {
            Try(l.onSessionUpdate(value)) match {
              case Success(e) => logger.debug("uuid: {} changed. Notify listeners: {} successfully", uuid, l)
              case Failure(t) => logger.error("uuid: {} changed. Notify listeners: {} with error.", uuid, l, t)
            }
          }
        }
      }
      case None => throw new IllegalArgumentException("unexpected!")
    }
  }
  
  def dropSession(uuid:String):Boolean = {
    internalControl.contains(uuid) match {
      case false => false
      case true => {
        val storage = internalControl.get(uuid) match {
          case None => throw new IllegalArgumentException("unexpected!")
          case Some(e) => {
            e._2.foreach {
              l=> Try (l.onSessionDrop()) match {
                case Success(e) => logger.debug("uuid: {} to be dropped. Notify listeners: {} successfully", uuid, l)
                case Failure(t) => logger.error("uuid: {} to be dropped. Notify listeners: {} with error.", uuid, l, t)
              }
            }
          }
        }
        internalControl.-=(uuid)
        true
      }
    }
  }
  
  def getAllUUID():scala.collection.Set[String] = internalControl.keySet
  
  def getSessionDetail(uuid:String):Option[(scala.collection.immutable.Map[String,String],ListBuffer[SessionUpdateListener])] 
    = internalControl.get(uuid)
  
  def registerSessionUpdateListener(uuid:String, listener:SessionUpdateListener) {
    require(internalControl.contains(uuid) && listener != null)
    
    val storage = internalControl.get(uuid)
    storage match {
      case Some(v) => {
        v._2 += listener
      }
      case None => throw new IllegalArgumentException("unexpected!")
    }
  }
  
}