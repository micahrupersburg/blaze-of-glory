package gdg.blaze.ext

import gdg.blaze._
import gdg.blaze.codec.JSONCodec
import org.apache.hadoop.io.{Text, LongWritable}
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat
import org.apache.spark.SparkContext
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SQLContext
import org.apache.spark.streaming.StreamingContext
import org.apache.spark.streaming.dstream.DStream

import scala.collection.mutable

/* add_field => ... # hash (optional), default: {}
    codec => ... # codec (optional), default: "plain"
    discover_interval => ... # number (optional), default: 15
    exclude => ... # array (optional)
    path => ... # array (required)
    sincedb_path => ... # string (optional)
    sincedb_write_interval => ... # number (optional), default: 15
    start_position => ... # string, one of ["beginning", "end"] (optional), default: "end"
    stat_interval => ... # number (optional), default: 1
    tags => ... # array (optional)
    type => ... # string (optional)*/
/*
  format = (text, or binary, etc)
 */
class HdfsInput(config: PluginConfig) extends BaseInput(config) {

  def filter(path: org.apache.hadoop.fs.Path) : Boolean = true
  override def stream(sc: StreamingContext): DStream[Message] = {
    val format = config.getString("format").getOrElse("text")
    val path = config.getString("path")
    if(path.isEmpty) {
      throw new IllegalStateException("hdfs plugin requires path field")
    }
    val codec = config.getString("codec").getOrElse("json")
    val newFiles = config.getBool("new_files_only").getOrElse(true)
    format match {
      case "text" => sc.fileStream[LongWritable, Text, TextInputFormat](path.get, filter, newFiles).map(_._2.toString).flatMap(stringCodec(codec).transform)
    }
  }

  def stringCodec(name: String): StringCodec = {
    name match {
      case "json" => new JSONCodec()
    }
  }
}

object HdfsInput extends PluginFactory[HdfsInput] {
  override def name = "hdfs"
  override def create(config: PluginConfig) = new HdfsInput(config)
}