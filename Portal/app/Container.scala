import play.api.Play.current
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.concurrent.Akka
import akka.actor.Props
import scala.concurrent.duration._
import controllers.SessionCleansingActor
import com.typesafe.config.ConfigFactory

object Global extends play.api.GlobalSettings {
     val scheduler = com.richardchankiyin.os.Scheduler
     override def onStart(app: play.api.Application) {
        // init Scheduler for checking jobs 
        scheduler.logKeeper.registerListener(com.richardchankiyin.alert.MailAlert("app"))
        scheduler.init
        
        val sessionCleansingActor = Akka.system.actorOf(Props[SessionCleansingActor], name = "session_cleansing_actor")
        
        val config = ConfigFactory.load("app")
        val session_cleansing_schedule_min = config.getInt("session_cleansing_schedule_min")
        // init Session Cleansing job
        Akka.system.scheduler.schedule(0.microsecond, session_cleansing_schedule_min.minute, sessionCleansingActor, "")
     }
}
