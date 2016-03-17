package controllers

trait SessionUpdateListener {
  def onSessionUpdate(update:Map[String,String])
  
  def onSessionDrop()
}