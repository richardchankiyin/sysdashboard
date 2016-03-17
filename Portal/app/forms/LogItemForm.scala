package forms

case class LogItemForm(
    userName:String, password:String, schedule:Option[String]
    , jobDesc:String, remarks:Option[String], isSuccess:Option[String])