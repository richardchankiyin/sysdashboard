package com.richardchankiyin.log

import org.scalatest.FlatSpec
import com.typesafe.scalalogging.Logger
import org.slf4j.LoggerFactory
import scala.util.{Try,Success,Failure}


class LogKeeperTest  extends FlatSpec{
  lazy val logger = Logger(LoggerFactory.getLogger(this.getClass))
  
  "LogKeeper" should "increase the size of listener after registering" in {
    val logchangeListener = new LogChangeListener {
      def onReceiveNewLog(item:LogItem) = {}
      def onReceiveDeletedLogs(items:List[LogItem]) = {}
    }
    
    val logKeeper = LogKeeper("logkeeper")
    
    val size = logKeeper.getNoOfListeners
    logKeeper.registerListener(logchangeListener)
    assert(size+1 == logKeeper.getNoOfListeners)
  }
  
  
  "LogKeeper" should "decrease the size of listener after unregistering" in {
    val logchangeListener1 = new LogChangeListener {
      def onReceiveNewLog(item:LogItem) = {}
      def onReceiveDeletedLogs(items:List[LogItem]) = {}
    }
    
    val logchangeListener2 = new LogChangeListener {
      def onReceiveNewLog(item:LogItem) = {}
      def onReceiveDeletedLogs(items:List[LogItem]) = {}
    }
    
    val logKeeper = LogKeeper("logkeeper")
    
    logKeeper.registerListener(logchangeListener1)
    logKeeper.registerListener(logchangeListener2)
    
    val size_init = logKeeper.getNoOfListeners
    
    logKeeper.unregisterListener(logchangeListener2)
    
    val size_final = logKeeper.getNoOfListeners
    
    assert(size_final == size_init - 1)
  }
  
  
  "LogKeeper" should "fire onReceiveNewLog event to registered listener" in {
    val now = java.time.Instant.now()
    val logItem = new LogItem(schedule="schedule", jobDesc="jobDesc", time=now, isSuccess=true, remarks="remarks")
    var times = 0
    val logchangeListener = new LogChangeListener {
      def onReceiveNewLog(item:LogItem) = {
        times +=1
        logger.debug("   item:[{}]", item)
        logger.debug("logItem:[{}]", logItem)
        assert(item.toString().equals(logItem.toString()))
      }
      def onReceiveDeletedLogs(items:List[LogItem]) = {}
    }
    
    val logchangeListener2 = new LogChangeListener {
      
      def onReceiveNewLog(item:LogItem) = {times = times + 1}
      def onReceiveDeletedLogs(items:List[LogItem]) = {}
    }
    
    val logKeeper = LogKeeper("logkeeper")
    
    logKeeper.registerListener(logchangeListener)
    logKeeper.registerListener(logchangeListener2)
    
    logKeeper.addLogItem(logItem)
    
    logger.debug("times: {}", {times.toString()})
    assert(times == 2)
    
    assert(logKeeper.getLogItems.length == 1)
  }
  
  "LogKeeper" should "fire onReceiveDeletedLogs event to registered listener" in {
    val logItems = Array(
        new LogItem(schedule="schedule1", jobDesc="jobDesc1", time=java.time.Instant.now(), isSuccess=true, remarks="remarks1")
        , new LogItem(schedule="schedule2", jobDesc="jobDesc2", time=java.time.Instant.now(), isSuccess=true, remarks="remarks2")
        , new LogItem(schedule="schedule3", jobDesc="jobDesc3", time=java.time.Instant.now(), isSuccess=true, remarks="remarks3")
        )
    
    val anotherLogItem = new LogItem(schedule="schedule4", jobDesc="jobDesc4", time=java.time.Instant.now(), isSuccess=true, remarks="remarks4")    
    var new_times = 0
    var delete_times = 0
    var deletedItems:List[LogItem] = null
    val logchangeListener = new LogChangeListener {
      def onReceiveNewLog(item:LogItem) = {
        new_times +=1
      }
      def onReceiveDeletedLogs(items:List[LogItem]) = {
        logger.debug("logchangeListener deleted items:\n {}", items)
        delete_times +=1
        deletedItems = items
      }
    }
    
    var logchangeListener2Notified = false
    val logchangeListener2 = new LogChangeListener {
      def onReceiveNewLog(item:LogItem) = {
        
      }
      def onReceiveDeletedLogs(items:List[LogItem]) = {
        logchangeListener2Notified = true
        
      }
    }
    
    val logKeeper = LogKeeper("logkeeper_test")
    
    logKeeper.registerListener(logchangeListener)
    logKeeper.registerListener(logchangeListener2)
    logKeeper.registerListener(DeletedLogItemsRecorder)
    
    logItems.foreach {
      item => logKeeper.addLogItem(item)
    }
    
    logKeeper.addLogItem(anotherLogItem)
    
    Thread sleep 1000

    assert(new_times == 4)
    assert(delete_times == 1)
    assert(logchangeListener2Notified)
    assert(deletedItems.size == 2)
    
    assert("jobDesc1".equals(deletedItems(0).getJobDesc))
    assert("jobDesc2".equals(deletedItems(1).getJobDesc))
    
    val remainingItems = logKeeper.getLogItems
    
    logger.debug("remainingItems: \n{}", remainingItems)
    assert("jobDesc3".equals(remainingItems(0).getJobDesc))
    assert("jobDesc4".equals(remainingItems(1).getJobDesc))
    
  }
  

}