package com.richardchankiyin.log

import com.typesafe.config.ConfigFactory
import org.slf4j.LoggerFactory
import com.typesafe.scalalogging.Logger

object DeletedLogItemsRecorder extends LogChangeListener{
  lazy val logger = Logger(LoggerFactory.getLogger(this.getClass))
  
  def onReceiveNewLog(item:LogItem) {
    
  }
  
  def onReceiveDeletedLogs(items:List[LogItem]) {
    if (items != null && items.length != 0) {
      logger.info("The following items are deleted:")
      items.foreach {x=>logger.info({x.toString()})}
    }  
  }
}