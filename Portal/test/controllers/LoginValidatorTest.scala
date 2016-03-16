package controllers

import org.scalatest.FlatSpec
import com.typesafe.config.ConfigFactory

class LoginValidatorTest extends FlatSpec{
  "LoginValidator" should "retrieve admin_user and admin_password from app.conf to validate" in {
    val config = ConfigFactory.load("app")
    
    assert(true==LoginValidator.validate(config.getString("admin_user"),config.getString("admin_password")))
  }
  
  "LoginValidator" should "fail validation if password is not correct" in {
    val config = ConfigFactory.load("app")
    
    assert(false==LoginValidator.validate(config.getString("admin_user"),config.getString("admin_password") + "ccc"))
  }
  
  "LoginValidator" should "fail validation if username is not correct but password is correct" in {
    val config = ConfigFactory.load("app")
    
    assert(false==LoginValidator.validate(config.getString("admin_user")+"aaa",config.getString("admin_password") + "ccc"))
  }
  
  "LoginValidator" should "fail validation if username is blank" in {
    val config = ConfigFactory.load("app")
    
    assert(false==LoginValidator.validate("",config.getString("admin_password")))
  }
  
  "LoginValidator" should "fail validation if password is blank" in {
    val config = ConfigFactory.load("app")
    
    assert(false==LoginValidator.validate(config.getString("admin_user"),""))
  }
}