package com.richardchankiyin.log

import com.typesafe.config.ConfigFactory
import org.slf4j.LoggerFactory
import com.typesafe.scalalogging.Logger

object NewLogItemRecorder extends LogChangeListener{
  lazy val logger = Logger(LoggerFactory.getLogger(this.getClass))
  
  def onReceiveNewLog(item:LogItem) {
    if (item != null) {
      item.isLogSuccess match {
        case true => logger.info("A new incoming log: {}", item)
        case false => logger.error("ERROR FOUND!! Log Detail: {}", item)
      }
    }
  }
  
  def onReceiveDeletedLogs(items:List[LogItem]) {
    //do nothing
  }
}