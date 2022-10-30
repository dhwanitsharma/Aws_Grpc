package grpc
import Log.{LogProcessorGrpc, LogProto, LogReply, LogRequest}
import io.grpc.{ManagedChannel, ManagedChannelBuilder}
import java.util.logging.{Level, Logger}
import com.typesafe.config.{Config, ConfigFactory}
import java.util.concurrent.TimeUnit



  object grpcClient {
    val logger = Logger.getLogger(classOf[grpcClient].getName)
    val port = 8081
    val time ="2022-10-28-19-22-00:000"
    val interval = "2"
    val pattern = "Rsxg"


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
      val client = grpcClient("localhost", port)
      try client.find(interval, time, pattern)
      finally client.shutdown()
    }
  }
class grpcClient(private val channel: ManagedChannel,private val blockingStub: LogProcessorGrpc.LogProcessorBlockingStub){
  val logger = Logger.getLogger(classOf[grpcClient].getName)

  def shutdown(): Unit = {
    logger.info("Trying to shutdown")
    channel.shutdown.awaitTermination(20.toLong, TimeUnit.SECONDS)
  }

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
