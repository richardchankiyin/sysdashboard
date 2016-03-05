package com.richardchankiyin.os

import com.typesafe.config.ConfigFactory
import org.slf4j.LoggerFactory
import com.typesafe.scalalogging.Logger
import it.sauronsoftware.cron4j.CronParser
import java.io.File
import it.sauronsoftware.cron4j.ProcessTask

object Scheduler {
  lazy val logger = Logger(LoggerFactory.getLogger(this.getClass))
  
  def init:Unit = {
    logger.debug("initializing Scheduler")
    lazy val config = ConfigFactory.load("scheduler")
    lazy val cronPath = config.getString("cron_path")
    lazy val cronFile = new File(cronPath)
    logger.debug("cronPath: {} exists?: {}", cronPath, {cronFile.exists().toString()})
    require(cronFile.exists() && cronFile.isFile(), s"cron file $cronPath does not exist")
    
    val taskTable = CronParser.parse(cronFile)
    val noOfTask = taskTable.size
    logger.debug("no of tasks: {}", {noOfTask.toString()})
    
    0 until noOfTask foreach { i => {
        val task = taskTable.getTask(i).asInstanceOf[ProcessTask]
        val schedule = taskTable.getSchedulingPattern(i)
        
        //TODO to be implemented
      }
    }
  }
}