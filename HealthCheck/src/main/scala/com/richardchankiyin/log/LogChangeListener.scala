package com.richardchankiyin.log

trait LogChangeListener {
  
  def onReceiveNewLog(item:LogItem)
  
  def onReceiveDeletedLogs(items:List[LogItem])
}