package controllers

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import com.typesafe.config.ConfigFactory
import scala.util.{Try,Success,Failure}
import forms.LoginForm

class PortalApp extends Controller{
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

  
  def loginPage = Action {
    Ok(views.html.loginpage(portal_title,""))
  }
  
  def dashboard = Action { request =>
    //TODO to be implemented with detail
    Ok(s"logged in dashboard....session:${request.session}")
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
                 .withSession("connected"->item.userName, "logintime"->java.time.Instant.now().toString())
             else
               Ok(views.html.loginpage(portal_title, "Login Failed"))
          }
      )
  }
}