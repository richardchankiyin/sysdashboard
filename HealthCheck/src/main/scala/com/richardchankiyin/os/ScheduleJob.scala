package com.richardchankiyin.os

import com.typesafe.config.ConfigFactory
import org.slf4j.LoggerFactory
import com.typesafe.scalalogging.Logger
import scala.concurrent.duration.Duration
import scala.util.Try
import scala.util.Success
import scala.util.Failure

class ScheduleJob(scheduleDesc:String, jobDesc:String, command:String, timeout:Duration, handleSuccess:()=>Unit, handleFailure:(Throwable)=>Unit) {
  val logger = Logger(LoggerFactory.getLogger(this.getClass))
  
  logger.debug("scheduleDesc: [{}] jobDesc: [{}] command: [{}]", scheduleDesc, jobDesc, command)

  def execute {
    // call OSCommandRunner to run and retrieve the result
    Try(OSCommandRunner.run(command, timeout)) match {
      case Success(e) => {
        // handling of different case
        e match {
          case Left(r) => {
            if (r == true)
              handleSuccessInternal()
            else
              handleFailureInternal(new RuntimeException("job failed"))
          }
          case Right(t) => {
            handleFailureInternal(t)
          }
        }
      }
      case Failure(t) => {
        // error logging
        handleFailureInternal(t)
      }
    }
  }
  
  private def handleSuccessInternal() {
    logger.debug("handleSuccessInternal start...")
    if (handleSuccess != null)
      handleSuccess()
  }
  
  private def handleFailureInternal(t:Throwable) {
    logger.debug("handleFailureInternal: {} start....", t)
    if (handleFailure != null)
      handleFailure(t)
  }

  override def toString:String = s"Job Desc:[$jobDesc] Schedule:[$scheduleDesc] Command:[$command] Timeout:[$timeout]"
  
}