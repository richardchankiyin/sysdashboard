package controllers

object RequestIdGenerator {
     val seq = new java.util.concurrent.atomic.AtomicLong()

     def getUUID:String = seq.incrementAndGet.toString


}