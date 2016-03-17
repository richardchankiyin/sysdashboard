package controllers

import akka.actor.Actor

class SessionCleansingActor extends Actor{
  def receive = {
    case _ => SessionChecker.cleanse
  }
}