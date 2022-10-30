import Lambda.lambdaFunction
import com.amazonaws.services.lambda.runtime.events.{APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent}
import com.amazonaws.services.lambda.runtime.{Context, RequestHandler}
import org.json4s.jackson.JsonMethods._
import scala.jdk.CollectionConverters.MapHasAsJava

import java.util.Base64

class Gateway extends RequestHandler[APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent]{
  override def handleRequest(input: APIGatewayProxyRequestEvent, context: Context): APIGatewayProxyResponseEvent = {
    implicit val formats = org.json4s.DefaultJsonFormats
    val jsonData = parse(input.getBody).values.asInstanceOf[Map[String, String]]
    val interval = jsonData("interval").toInt
    val time = jsonData("time")
    val pattern = jsonData("pattern")
    val resp = lambdaFunction(interval,time,pattern)
    val apiResponse = new APIGatewayProxyResponseEvent()
    if (resp.equals("false")){
      apiResponse.withStatusCode(404).withBody(s"""{
          "hash":"Pattern Not Found"
        }""")
    }
    else{
      apiResponse.withStatusCode(200)
        .withHeaders(Map("Content-Type" -> "application/json").asJava)
        .withIsBase64Encoded(false)
        .withBody(
          s"""{
          "hash":${resp}
        }""")
    }
    apiResponse
    }
  }

