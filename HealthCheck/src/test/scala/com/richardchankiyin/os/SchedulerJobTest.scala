package com.richardchankiyin.os

import org.scalatest.FlatSpec
import com.typesafe.scalalogging.Logger
import org.slf4j.LoggerFactory
import scala.concurrent.duration._
import scala.concurrent.duration.Duration

class SchedulerJobTest extends FlatSpec{
  val logger = Logger(LoggerFactory.getLogger(this.getClass))
  

  val osname = System.getProperty("os.name").toLowerCase()
  logger.debug("osname.indexOf(\"win\"): {}", {osname.indexOf("win").toString()})
  val isWin = osname.indexOf("win") >= 0
  
  logger.debug("isWin: {} osname: {}", {isWin.toString()}, {osname})
  val timeoutCommand = ("timeout 5","sleep 5")
  val successCommand = ("java -version","java -version")
  val failCommand = ("java -a", "java -a")
  
  def commandToBeTested(isWinEnv:Boolean,commands:(String,String)):String = {
    if (isWinEnv)
      commands._1
    else
      commands._2
  }
  
  "Schedule Job" should "run handleSuccess" in {
    val job = new ScheduleJob("", "success testing", commandToBeTested(isWin,successCommand), 10 seconds
        , ()=>{logger.debug("OK")}, (t)=>{fail("unexpected")})
    job.execute
  }
  
  "Schedule Job" should "run handleFailure" in {
    val job = new ScheduleJob("", "failed testing", commandToBeTested(isWin,failCommand), 10 seconds
        , ()=>{fail("unexpected!")}, (t)=>{logger.debug("OK")})
    job.execute
  }
  
  "Schedule Job" should "run handleFailure in timeout" in {
    val job = new ScheduleJob("", "timeout testing", commandToBeTested(isWin,timeoutCommand), 1 milliseconds
        , ()=>{fail("unexpected!")}, (t)=>{logger.debug("OK")})
  }
  
}