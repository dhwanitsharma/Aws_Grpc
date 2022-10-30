import Lambda.lambdaFunction
import com.amazonaws.services.lambda.runtime.events.{APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent}
import com.amazonaws.services.lambda.runtime.{Context, RequestHandler}
import org.json4s.jackson.JsonMethods._
import scala.jdk.CollectionConverters.MapHasAsJava

/**
 * Gateway for the Lambda Function
 */
class Gateway extends RequestHandler[APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent]{
  val FALSE = "false"
  val STATUS_404 = 404
  val STATUS_200 = 200
  val INTERVAL = "interval"
  val TIME = "time"
  val PATTERN = "pattern"

  /**
   * Request Handler for the lambda function
   * @param input : Json with three parameters : "interval","time","pattern"
   * @param context : Interface Context
   * @return APIResponse : JSON output  "hash":"value"
   */
  override def handleRequest(input: APIGatewayProxyRequestEvent, context: Context): APIGatewayProxyResponseEvent = {
    implicit val formats = org.json4s.DefaultJsonFormats
    val logger = context.getLogger
    logger.log(s"Request ${input.toString}")
    val jsonData = parse(input.getBody).values.asInstanceOf[Map[String, String]]
    val interval = jsonData(INTERVAL)
    val time = jsonData(TIME)
    val pattern = jsonData(PATTERN)
    val resp = lambdaFunction(interval,time,pattern)
    val apiResponse = new APIGatewayProxyResponseEvent()
    if (resp.equals(FALSE)){
      logger.log(s"Response FALSE : ${resp}")
      apiResponse.withStatusCode(STATUS_404).withBody(s"""{
          "hash":"Pattern Not Found"
        }""")
    }
    else{
      logger.log(s"Response With status 200 : ${resp}")
      apiResponse.withStatusCode(STATUS_200)
        .withHeaders(Map("Content-Type" -> "application/json").asJava)
        .withIsBase64Encoded(false)
        .withBody(
          s"""{
          "hash":"${resp}"
        }""")
    }
    apiResponse
    }
  }

