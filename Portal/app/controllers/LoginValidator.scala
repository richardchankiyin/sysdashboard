package controllers
import com.typesafe.config.ConfigFactory

object LoginValidator {
  def validate(i_username:String, i_password:String):Boolean = {
    val config = ConfigFactory.load("app")
    val userName= config.getString("admin_user")
    val password = config.getString("admin_password")
    (i_username.equals(userName)) && (i_password.equals(password))
  }
}
