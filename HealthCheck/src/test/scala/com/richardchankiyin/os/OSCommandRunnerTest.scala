package com.richardchankiyin.os

import org.scalatest.FlatSpec
import com.typesafe.scalalogging.Logger
import org.slf4j.LoggerFactory
import scala.concurrent.duration._

class OSCommandRunnerTest extends FlatSpec{
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
  
  "OSCommandRunner" should "encounter timeout" in {
    val command = commandToBeTested(isWin,timeoutCommand)
    logger.debug("command: {}", command)
    OSCommandRunner.run(command, 1 millisecond) match {
      case Left(x) => fail("should throw exception")
      case Right(x) => {logger.debug("expected!", x)}
    }
  }
  
  "OSCommandRunner" should "run the command successfully" in {
    val command = commandToBeTested(isWin,successCommand)
    logger.debug("command: {}", command)
    OSCommandRunner.run(command, 10 seconds) match {
      case Left(x) => {assert(true==x)}
      case Right(x) => {fail("unexpected!",x)}
    }
  }
  
  "OSCommandRunner" should "fail to run the command" in {
    val command = commandToBeTested(isWin,failCommand)
    logger.debug("command: {}", command)
    OSCommandRunner.run(command, 10 seconds) match {
      case Left(x) => {assert(false==x)}
      case Right(x) => {fail("unexpected!",x)}
    }
  }
}