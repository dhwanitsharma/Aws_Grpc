package grpc
import java.util.logging.{LogManager, Logger}
import scala.concurrent.{ExecutionContext, Future}
import io.grpc.{ManagedChannel, ManagedChannelBuilder, Server, ServerBuilder, StatusRuntimeException}
import Log.{LogProcessorGrpc, LogProto, LogReply, LogRequest}

import scala.util.{Failure, Success, Try}
import com.typesafe.config.{Config, ConfigFactory}
import scalaj.http.{Http, HttpOptions}
import org.json4s.jackson.JsonMethods._

class grpcServer (executionContext: ExecutionContext) {
  self =>

  private[this] var server: Server = null
  private val logger = Logger.getLogger(classOf[grpcServer].getName)


  private def start(): Unit = {
    server = ServerBuilder.forPort(grpcServer.port).addService(LogProcessorGrpc.bindService(new Logimpl, executionContext)).build.start()
    logger.info("Server started, listening on " + grpcServer.port)
    sys.addShutdownHook {
      System.err.println("shutting down gRPC server since JVM is shutting down")
      self.stop()
      System.err.println("server shut down")
    }
  }

  private def stop(): Unit =
    if (server != null) server.shutdown()

  private def blockUntilShutdown(): Unit =
    if (server != null) server.awaitTermination()

  class Logimpl extends LogProcessorGrpc.LogProcessor {
    override def findLog(request: LogRequest): Future[LogReply] = {
      val data   =
        s"""{"time" :"${request.time}",
           |"interval" :"${request.interval}",
           |"pattern" :"${request.pattern}"}""".stripMargin
      val resp = Http(grpcServer.url).postData(data).header("content-type", "application/json").option(HttpOptions.readTimeout(20000))
      val jsonData = parse(resp.asString.body).values.asInstanceOf[Map[String, String]]
      println(jsonData)
      val reply = LogReply(message = jsonData("hash"))
      Future.successful(reply)
      }
    }

}

object grpcServer {
  val logger: Logger = Logger.getLogger(classOf[App].getName)
  val port = 8081
  val user_config: Config = ConfigFactory.load("S3.conf")
  val url = (user_config.getString("S3Conf.url"))
  val bucket = (user_config.getString("S3Conf.Bucket"))
  val key = (user_config.getString("S3Conf.Key"))

  def main(args: Array[String]): Unit = {
    val server = new grpcServer(ExecutionContext.global)
    server.start
    server.blockUntilShutdown
  }
}
