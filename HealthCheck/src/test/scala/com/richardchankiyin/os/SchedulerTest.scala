package com.richardchankiyin.os

import org.scalatest.FlatSpec
import com.typesafe.scalalogging.Logger
import org.slf4j.LoggerFactory
import com.richardchankiyin.log.LogKeeper
import com.richardchankiyin.log.NewLogItemRecorder


class SchedulerTest extends FlatSpec{
  val logger = Logger(LoggerFactory.getLogger(this.getClass))
  
  "Scheduler" should "schedule 2 jobs in 2 minutes" in {
    val logKeeper = Scheduler.logKeeper
    
    logKeeper.registerListener(NewLogItemRecorder)
    
    Scheduler.init
    
    Thread sleep 1000 * 60 * 2
    
    val items = logKeeper.getLogItems
    
    logger.debug("items: {}", items)
    
    assert(items.length == 4)
    assert(2 == items.count(_.isLogSuccess))
    assert(2 == items.count(!_.isLogSuccess))
  }
}