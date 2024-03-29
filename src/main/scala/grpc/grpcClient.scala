package grpc
import Helper.CreateLogger
import Log.{LogProcessorGrpc, LogProto, LogReply, LogRequest}
import io.grpc.{ManagedChannel, ManagedChannelBuilder}

import java.util.logging.{Level, Logger}
import com.typesafe.config.{Config, ConfigFactory}

import java.util.concurrent.TimeUnit

/**
 * gRPC Client object to call the server
 */
  object grpcClient {
    val user_config: Config = ConfigFactory.load("S3.conf")
    val log = CreateLogger(classOf[grpcClient])
    val logger = Logger.getLogger(classOf[grpcClient].getName)
    val grpc_config: Config = ConfigFactory.load("S3.grpc")
    val port = user_config.getString("grpc.port").toInt
    val time = user_config.getString("grpc.time")
    val interval = user_config.getString("grpc.interval")
    val pattern = user_config.getString("grpc.detect_patter")
    val host = user_config.getString("grpc.host")


    def apply(host: String, port: Int): grpcClient = {
      val channel = ManagedChannelBuilder.forAddress(host, port).usePlaintext().build
      val blockingStub = LogProcessorGrpc.blockingStub(channel)
      new grpcClient(channel, blockingStub)
    }

    /**
     * Driver function
     *
     * @param args CMD arguments
     */
    def main(args: Array[String]): Unit = {
      logger.info("Starting grpcClient:" + "" +
        "port\t: " + port)
      log.info("Starting grpcClient:" + "" +
        "port\t: " + port)
      val client = grpcClient(host, port)
      try client.find(interval, time, pattern)
      finally client.shutdown()
    }
  }

/**
 * gRPC Client class to call the server
 */
class grpcClient(private val channel: ManagedChannel,private val blockingStub: LogProcessorGrpc.LogProcessorBlockingStub){
  val logger = Logger.getLogger(classOf[grpcClient].getName)
  val log = CreateLogger(classOf[grpcClient])

  /**
   * function to shutdown the client
   */
  def shutdown(): Unit = {
    logger.info("Trying to shutdown")
    log.info("Trying to shutdown")
    channel.shutdown.awaitTermination(20.toLong, TimeUnit.SECONDS)
  }

  /**
   * Function to call the GRPC server
   * @param interval : Time interval
   * @param time : Specific time
   * @param pattern : Pattern to match the log messages
   */
  def find(interval: String, time: String, pattern: String): Unit = {
    val request = LogRequest(time, interval, pattern)
    logger.info("Request created")
    try {
      val response = blockingStub.findLog(request)
      logger.info("Response: " + response.message)
      if (response.message.equals("Pattern Not Found")) {
        logger.info("No log statements found for given parameters")
      }
      else {
        logger.info("Log messages are found with hash " + response.message)
      }
    } catch {
      case e: Exception => logger.log(Level.WARNING, "RPC Call FAILED", e.getMessage)
    }
  }
}
