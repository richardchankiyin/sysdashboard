package com.richardchankiyin.os

import scala.concurrent.duration.Duration
import com.typesafe.scalalogging.Logger
import org.slf4j.LoggerFactory

object OSCommandRunner {
  val logger = Logger(LoggerFactory.getLogger(this.getClass))
  //TODO implement this
  def run(command:String, timeout:Duration):Either[Boolean,Throwable] = Left(false)
  
}