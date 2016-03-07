package com.richardchankiyin.log

import com.typesafe.config.ConfigFactory
import org.slf4j.LoggerFactory
import com.typesafe.scalalogging.Logger
import java.util.concurrent.LinkedBlockingQueue
import scala.collection.mutable.MutableList
import scala.util.{Try,Success,Failure}

case class LogKeeper(configName:String="logkeeper") extends LogChangeListener{
  lazy val logger = Logger(LoggerFactory.getLogger(this.getClass))
  
  logger.debug("configName: {}", configName)
  
  private val config = ConfigFactory.load(configName)
  
  private val threshold = config.getInt("threshold")
  private val cleansingBatchSize = config.getInt("cleansingBatchSize") //cleansing batch size should not exceed threshold
  
  private var cleansingInProcess = false
  
  private val registeredListeners = MutableList[LogChangeListener]()
  
  private val logItemQueue = new LinkedBlockingQueue[LogItem]
  
  def registerListener(listener:LogChangeListener) = {
    logger.debug("incoming listener: {}", listener)
    registeredListeners.+=(listener)
    logger.debug("registeredListener: {}", {registeredListeners.toString()})
  }
  
  def getLogItems:List[LogItem] = logItemQueue.toArray(Array[LogItem]()).toList
  
  def getNoOfListeners:Int = registeredListeners.length
  
  def isCleansingInProcess:Boolean = cleansingInProcess
  
  def addLogItem(item:LogItem) {
    require(item!=null, "LogItem cannot be null")
    logger.info("New LogItem:[{}]", item)
    logItemQueue.add(item)
    registeredListeners.foreach {
      l=>l.onReceiveNewLog(item)
    }
  }
  
  // self registration
  registerListener(this)
  
  def onReceiveNewLog(item:LogItem) {
    
    if (logItemQueue.size() > threshold && !cleansingInProcess) {
      cleansingInProcess = true
      var thisBatchSize = cleansingBatchSize
      // create a new thread to clean up logItemQueue
      if (cleansingBatchSize > threshold)
        thisBatchSize = cleansingBatchSize
      val logItemDeleted = MutableList[LogItem]()
      val thread = new Thread {override def run = {
          Try(0 until thisBatchSize foreach {
              x=> {
                logItemDeleted.+=(logItemQueue.poll)
              }
            }
          ) match {
            case Success(e) => {
              cleansingInProcess = false
              logger.debug("cleansing complete. No of Items Left: {}", {logItemQueue.size().toString()})
              registeredListeners.foreach {
                l=>l.onReceiveDeletedLogs(logItemDeleted.toList)
              }
            }
            case Failure(t) => {
              cleansingInProcess = false
              logger.error("cleansing encountered error", t)
              registeredListeners.foreach {
                l=>l.onReceiveDeletedLogs(logItemDeleted.toList)
              }
            }
          }
        }
      }
      thread.start
      
    }
    else {
      logger.debug("no cleansing is happening")
    }
  }
  
  def onReceiveDeletedLogs(items:List[LogItem]) {
    // do nothing here
  }
  
}