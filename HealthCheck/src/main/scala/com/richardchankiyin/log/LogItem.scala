package com.richardchankiyin.log

import java.time.Instant

class LogItem(schedule:String, jobDesc:String, time:Instant, isSuccess:Boolean, remarks:String){
  def getSchedule:String = schedule
  def getJobDesc:String = jobDesc
  def getTime:Instant = time
  def isLogSuccess:Boolean = isSuccess
  def getRemarks:String = remarks
  
  
  override def toString =
    s"schedule:[$schedule] jobdesc:[$jobDesc] time:[$time] success:[$isSuccess] remarks:[$remarks]" 
}