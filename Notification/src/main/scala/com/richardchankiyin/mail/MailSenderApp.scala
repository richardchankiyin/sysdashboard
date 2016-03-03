package com.richardchankiyin.mail

object MailSenderApp {
  def main(args:Array[String]) {

    /* Gmail setting */
    /*
    val list:List[(String,String)] = List(
         ("mail.smtp.auth", "true")
        ,("mail.smtp.starttls.enable", "true")
        ,("mail.smtp.ssl.trust", "smtp.gmail.com")
        )
    */
    
    val senderLocal = MailSender("localhost",25000,null,null,null)
    senderLocal.sendMail("abc@abc.com", Array("def@abc.com","efg@ggg.com"), "subject", "text")
  }
}