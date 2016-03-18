package controllers

object RequestIdGenerator {
     val seq = new java.util.concurrent.atomic.AtomicLong()

     def getUUID:String = java.util.UUID.randomUUID().toString() + seq.incrementAndGet.toString


}