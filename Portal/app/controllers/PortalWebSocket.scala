package controllers

import scala.concurrent.ExecutionContext.Implicits.global
import play.api.mvc.WebSocket
import play.api.libs.iteratee.Concurrent
import play.api.libs.iteratee.Iteratee
import com.typesafe.scalalogging.Logger
import org.slf4j.LoggerFactory

class PortalWebSocket extends SessionConfig with PortalWebSocketMessages{
  val logger = Logger(LoggerFactory.getLogger(this.getClass))
  def broadcastLog = WebSocket.using[String]{ request =>
    val (out,channel) = Concurrent.broadcast[String]
        
    val in = Iteratee.foreach[String] {
      msg => {
        // check session here
        SessionChecker.isLogin(request.session.data) match {
          case Left(r) => {
            if (!r) {
              logger.debug("unauthorized access. No login")
              channel.push(unauthorized_msg)
            }
            else {
              request.session.get(session_var_uuid) match {
                case Some(e) => {
                  // creating LogItemPusher
                  new LogItemPusher(e,com.richardchankiyin.os.Scheduler.logKeeper,channel)
                  channel.push(ok_msg)
                }
                case None => {
                  logger.warn("with session but uuid not found?! Weird")
                  channel.push(unauthorized_msg)
                }
              }
              
            }
          }
          case Right(r) => {
            logger.debug("unauthorized access. Timeout already....")
            channel.push(unauthorized_msg)
          }
          
        }
      }
   }
   (in,out)
  }
  
}