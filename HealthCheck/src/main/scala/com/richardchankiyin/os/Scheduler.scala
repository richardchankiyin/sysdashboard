package com.richardchankiyin.os

import java.io.File
import java.time.Instant
import com.typesafe.config.ConfigFactory
import org.slf4j.LoggerFactory
import com.typesafe.scalalogging.Logger
import it.sauronsoftware.cron4j.CronParser
import it.sauronsoftware.cron4j.ProcessTask
import scala.util.{Try,Success,Failure}
import scala.concurrent._
import scala.concurrent.duration._
import scala.concurrent.duration.Duration
import com.richardchankiyin.log.LogKeeper
import com.richardchankiyin.log.LogItem



class Scheduler(configName:String="scheduler") {
  lazy val logger = Logger(LoggerFactory.getLogger(this.getClass))
  lazy val logKeeper = LogKeeper(configName)
  
  def init:Unit = {
    logger.debug("initializing Scheduler")
    lazy val config = ConfigFactory.load(configName)
    lazy val cronPath = config.getString("cron_path")
    lazy val job_timeout:Int = config.getInt("job_timeout")
    lazy val job_success_remarks = config.getString("job_success_remarks")
    lazy val cronFile = new File(cronPath)
    logger.debug("cron file: {}", {cronFile.getAbsolutePath})
    
    lazy val scheduler = new it.sauronsoftware.cron4j.Scheduler()
    logger.debug("cronPath: {} exists?: {}", cronPath, {cronFile.exists().toString()})
    require(cronFile.exists() && cronFile.isFile(), s"cron file $cronPath does not exist")
    
    val taskTable = CronParser.parse(cronFile)
    val noOfTask = taskTable.size
    logger.debug("no of tasks: {}", {noOfTask.toString()})
    
    0 until noOfTask foreach { i => {
        val task = taskTable.getTask(i).asInstanceOf[ProcessTask]
        val schedule = taskTable.getSchedulingPattern(i)
        val desc = if (task.getEnvs != null) task.getEnvs()(0) else task.getCommand()(0)
        var command = ""
        task.getCommand().foreach (x=>{command += " " + x})
        command = command.trim() 
        logger.debug("task: {} schedule: {} desc: {} command: {}", task, schedule, desc, command)
        
        scheduler.schedule(schedule.toString(), new Runnable {
          override def run {
            val job = new ScheduleJob(schedule.toString(), desc, command:String
            , job_timeout seconds, ()=>{
              logKeeper.addLogItem(new LogItem(schedule.toString(), desc
                  , Instant.now(), true, job_success_remarks))
            }, (t:Throwable)=>{
              logKeeper.addLogItem(new LogItem(schedule.toString(), desc
                  , Instant.now(), false, t.getMessage))
            })
            job.execute
          }
        })
        
        
        
      }
    }
    
    scheduler.start()
  }

}

object Scheduler {
  def apply(config:String) = new Scheduler(config)
}
