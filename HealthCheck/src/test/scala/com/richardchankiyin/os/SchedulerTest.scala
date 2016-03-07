package com.richardchankiyin.os

import org.scalatest.FlatSpec
import com.typesafe.scalalogging.Logger
import org.slf4j.LoggerFactory
import com.richardchankiyin.log.LogKeeper


class SchedulerTest extends FlatSpec{
  val logger = Logger(LoggerFactory.getLogger(this.getClass))
  
  "Scheduler" should "schedule 2 jobs in 2 minutes" in {
    Scheduler.init
    
    Thread sleep 1000 * 60 * 2
    
    val logKeeper = Scheduler.logKeeper
    val items = logKeeper.getLogItems
    
    logger.debug("items: {}", items)
    
    assert(items.length == 4)
    assert(2 == items.count(_.isLogSuccess))
    assert(2 == items.count(!_.isLogSuccess))
  }
}