package controllers

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.Logger
import org.slf4j.LoggerFactory
import scala.util.{Try,Success,Failure}
import forms.LoginForm
import com.richardchankiyin.os.Scheduler

class PortalApp extends Controller{
  val logger = Logger(LoggerFactory.getLogger(this.getClass))
  private val config = ConfigFactory.load("app")
  private val session_var_user = "user"
  private val session_var_logintime = "logintime"
  private val session_var_last_activity_time = "lastactivitytime"
  
  private val portal_title = Try(config.getString("portal_title")) match {
    case Success(r) => r
    case Failure(t) => "This is your dashboard [Default]"
  }
  
  private val loginForm:Form[LoginForm] = Form(
    mapping(
      "userName" -> nonEmptyText,
      "password" -> nonEmptyText
    )(LoginForm.apply)(LoginForm.unapply)
  )

  
  def loginPage = Action {
    Ok(views.html.loginpage(portal_title,""))
  }
  
  def dashboard = Action { request =>
    //TODO add session checking
    
    // get logs from last n items of Scheduler.logKeeper
    val recentLogItems = Scheduler.logKeeper
      .getLogItems.takeRight(config.getInt("no_of_recent_log_shown"))
    
    logger.debug("recentLogItems: {}", {recentLogItems})  
      
    val user = request.session.get(session_var_user) match {
      case Some(e) => e
      case None => "unknown"
    }
    val logintime = request.session.get(session_var_logintime) match {
      case Some(e) => e
      case None => "unknown"
    }
    
      
    //TODO to be implemented with detail
    //Ok(s"logged in dashboard....session:${request.session}")
      
    Ok(views.html.dashboard(portal_title, recentLogItems, user, logintime))  
  }
  
  
  def loginProcess = Action { implicit request =>
      loginForm.bindFromRequest.fold(
          formWithErrors => {
             Ok(views.html.loginpage(portal_title, "invalid input"))
          },
          item => {
             if (LoginValidator.validate(item.userName,item.password))
               // set session
               Redirect(routes.PortalApp.dashboard)
                 .withSession(session_var_user->item.userName
                     , session_var_logintime->java.time.Instant.now().toString()
                     , session_var_last_activity_time->java.time.Instant.now().toString())
             else
               Ok(views.html.loginpage(portal_title, "Login Failed"))
          }
      )
  }
}