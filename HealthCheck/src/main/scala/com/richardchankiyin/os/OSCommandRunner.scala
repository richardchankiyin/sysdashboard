package com.richardchankiyin.os

import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit
import java.util.concurrent.ArrayBlockingQueue
import scala.concurrent._
import scala.concurrent.duration._
import scala.concurrent.duration.Duration
import scala.concurrent.forkjoin.ForkJoinPool
import scala.sys.process._
import scala.util.Failure
import scala.util.Success
import scala.util.Try
import com.typesafe.config.ConfigFactory
import org.slf4j.LoggerFactory
import com.typesafe.scalalogging.Logger


object OSCommandRunner {
  lazy val logger = Logger(LoggerFactory.getLogger(this.getClass))
  lazy val config = ConfigFactory.load("oscommandrunner")
  
  lazy val executor = new ThreadPoolExecutor(config.getInt("min_thread"),config.getInt("max_thread"),config.getInt("idle_seconds"),TimeUnit.SECONDS,new ArrayBlockingQueue(config.getInt("queue_size")))
  
  def run(command:String, timeout:Duration=30 seconds):Either[Boolean,Throwable] = {
    logger.debug("command: {} timeout: {}", command, timeout)
    var c = command run
    val e = ExecutionContext.fromExecutor(executor)
    val f:Future[Int] = Future { c.exitValue() }(e)
    Try(Await.result(f,timeout)) match {
      case Success(r) => {
        logger.debug("command: {} return code: {}", command, {r.toString})
        Left(r==0)
      }
      case Failure(t) => {
        c.destroy()
        logger.error(s"error encountered when running command: $command", t)
        Right(t)
      }
    }
  }
  
}