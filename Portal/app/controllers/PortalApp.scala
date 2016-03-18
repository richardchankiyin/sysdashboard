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
import forms.LogItemForm

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
  
  private val logItemForm:Form[LogItemForm] = Form (
    mapping(
      "userName" -> nonEmptyText,
      "password" -> nonEmptyText,
      "schedule" -> optional(text),
      "jobDesc" -> nonEmptyText,
      "remarks" -> optional(text),
      "isSuccess" -> optional(text)
    )(LogItemForm.apply)(LogItemForm.unapply)    
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
    Ok(views.html.loginpage(portal_title,"unauthorized access")).withNewSession
  }
  
  def logout = Action { req =>
    RequestActivityUpdater.handleLogout(req.session)
    Ok(views.html.loginpage(portal_title,"You have logged out")).withNewSession
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
          
          // update activity time
          //val newSession = RequestActivityUpdater.updateLastActivityTime(request.session)
          Try(RequestActivityUpdater.updateLastActivityTime(request.session)) match {
            case Failure(t) => {
              // something goes wrong when updating, probably session/cookie out-sync
              logger.warn("session/cookies out-sync", t)
              Redirect(routes.PortalApp.unauthAccess)
            }
            case Success(newSession) => {
              val user = request.session.get(session_var_user) match {
                case Some(e) => e
                case None => "unknown"
              }
              val logintime = request.session.get(session_var_logintime) match {
                case Some(e) => e
                case None => "unknown"
              }
              
              val host = request.host.toString
              logger.debug("host: {}", {host})
              
              Ok(views.html.dashboard(portal_title, recentLogItems, user, logintime, host)).withSession(newSession) 
            }
          }
           
        }
      }
      case Right(r) => {
        logger.debug("timeout error!")
        // drop session
        RequestActivityUpdater.handleLogout(request.session)
        Redirect(routes.PortalApp.unauthAccess)
      }
    }
  }
  
  def addLogItem = Action { implicit request =>
    logItemForm.bindFromRequest.fold(
        formWithErrors => {
          BadRequest("Error:[userName: mandatory][password: mandatory][schedule: optional][jobDesc: mandatory][remarks: optional][isSuccess: optional (Y for success else not success)]")
        }, 
        item => {
          val userName = item.userName
          val password = item.password
          LoginValidator.validate(userName, password) match {
            case false => Ok("{status:\"authentication failed\"}")
            case true => {
              val schedule = item.schedule match {
                 case Some(v) => v
                 case None => ""
              }
              val jobDesc = item.jobDesc
              val remarks = item.remarks match {
                 case Some(v) => v
                 case None => ""
              }
              val isSuccess = item.isSuccess match {
                case Some(v) => "Y".equals(v)
                case None => false
              }
              val logItem = new com.richardchankiyin.log.LogItem(schedule,jobDesc,java.time.Instant.now(),isSuccess,remarks)
              logger.info("incoming log item: {}", logItem)
              com.richardchankiyin.os.Scheduler.logKeeper.addLogItem(logItem)
              Ok("{\"status\":\"OK\"}") 
            }
          } //end LoginValidator.validate(userName, password)
        }
    )    
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