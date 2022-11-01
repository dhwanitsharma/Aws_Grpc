import com.typesafe.config.{Config, ConfigFactory}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import java.text.SimpleDateFormat
import java.time.LocalTime
import java.time.format.DateTimeParseException

class ConfigTests extends AnyFlatSpec with Matchers {
  it should "S3.conf S3conf should not contain blank values" in{
    val user_config: Config = ConfigFactory.load("S3.conf")
    val bucket = user_config.getString("S3Conf.Bucket")
    val key = user_config.getString("S3Conf.Key")
    val secret = user_config.getString("S3Conf.Secret")
    val url = user_config.getString("S3Conf.url")
    assert(!bucket.isBlank)
    assert(!key.isBlank)
    assert(!secret.isBlank)
    assert(!url.isBlank)
  }
  it should "S3.conf grpc should not contain blank values" in{
    val user_config: Config = ConfigFactory.load("S3.conf")
    val port = user_config.getString("grpc.port")
    val time = user_config.getString("grpc.time")
    val interval = user_config.getString("grpc.interval")
    val pattern = user_config.getString("grpc.detect_patter")
    val host = user_config.getString("grpc.host")
    assert(!port.isBlank)
    assert(!time.isBlank)
    assert(!interval.isBlank)
    assert(!pattern.isBlank)
    assert(!host.isBlank)
  }
  it should "interval should be 00:00:00 format" in{
    val user_config: Config = ConfigFactory.load("S3.conf")
    val interval = user_config.getString("grpc.interval")
    val dateFormatter = new SimpleDateFormat("HH:mm:ss")
    try {
      val interval_time = dateFormatter.parse(interval)
    }catch {
      case e : NullPointerException => assert(false)
      case e : DateTimeParseException => assert(false)
    }
  }
  it should "Port number should be Int" in{
    val user_config: Config = ConfigFactory.load("S3.conf")
    try {
      val port = user_config.getString("grpc.port").toInt
      val b = 1
    }catch {
      case e : Exception => assert(false)
    }
  }
}
