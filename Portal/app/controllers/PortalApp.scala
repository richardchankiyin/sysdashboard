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

class PortalApp extends Controller with SessionConfig{
  val logger = Logger(LoggerFactory.getLogger(this.getClass))
  private val config = ConfigFactory.load("app")
  
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

  def index = Action { request =>
    SessionChecker.isLogin(request.session.data) match {
      case Left(r) => {
        if (!r) {
          logger.debug("not logged in...")
          Redirect(routes.PortalApp.loginPage)
        } else {
          logger.debug("logged in...")
          Redirect(routes.PortalApp.dashboard)
        }
      }
      case Right(r) => {
        logger.debug("timeout error!")
        Redirect(routes.PortalApp.unauthAccess)
      }
    }
  }
  
  def loginPage = Action {
    Ok(views.html.loginpage(portal_title,""))
  }
  
  def unauthAccess = Action{
    Ok(views.html.loginpage(portal_title,"unauthorized access"))
  }
  
  def dashboard = Action { request =>
    
    SessionChecker.isLogin(request.session.data) match {
      case Left(r) => {
        if (!r) {
          logger.debug("not logged in...")
          Redirect(routes.PortalApp.unauthAccess)
        } else {
          // logged in
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
              
          Ok(views.html.dashboard(portal_title, recentLogItems, user, logintime))  
        }
      }
      case Right(r) => {
        logger.debug("timeout error!")
        Redirect(routes.PortalApp.unauthAccess)
      }
    }
    

  }
  
  
  def loginProcess = Action { implicit request =>
      loginForm.bindFromRequest.fold(
          formWithErrors => {
             Ok(views.html.loginpage(portal_title, "invalid input"))
          },
          item => {
             if (LoginValidator.validate(item.userName,item.password)) {
               lazy val uuid = RequestIdGenerator.getUUID
               
               // register session information session controller
               lazy val data = Map[String,String] (session_var_user->item.userName
                     , session_var_logintime->java.time.Instant.now().toString()
                     , session_var_last_activity_time->java.time.Instant.now().toString()
                     , session_var_uuid->uuid)
               
               lazy val session = Session(data)
               
               SessionManager.createSession(uuid, data)
                     
               Redirect(routes.PortalApp.dashboard).withSession(session)
             }    
             else
               Ok(views.html.loginpage(portal_title, "Login Failed"))
          }
      )
  }
}