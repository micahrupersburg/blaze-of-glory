package gdg.blaze.ext.io

import gdg.blaze._
import org.apache.spark.streaming.dstream.DStream

class StdOut() extends Output {
  override def apply(dStream: DStream[Message]) = {
    dStream.foreachRDD { rdd =>
      println("RDD - >")
      if(!rdd.isEmpty()) {
        rdd.collect().foreach(println)
      }
    }
  }
}

object StdOut extends PluginFactory[StdOut] {
  override def apply(config: PluginConfig, sc: BlazeContext): StdOut = new StdOut()
}
