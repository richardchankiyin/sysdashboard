
object Global extends play.api.GlobalSettings {
     val scheduler = com.richardchankiyin.os.Scheduler
     override def onStart(app: play.api.Application) {
         
        scheduler.logKeeper.registerListener(com.richardchankiyin.alert.MailAlert("mailalert"))
        scheduler.init
     }
}
