package clients

import scala.concurrent.{ExecutionContext, Future}

import akka.NotUsed
import akka.stream.Materializer
import akka.stream.scaladsl.{Flow, Framing, Source}
import akka.util.ByteString
import play.api.libs.ws.{StreamedResponse, WSClient}
import util.SLF4JLogging

class CarjumpClient(
    wsClient:       WSClient,
    carjumpBaseUrl: CarjumpBaseUrl
)(implicit val ec: ExecutionContext, mat: Materializer) extends SLF4JLogging {

  private val delimiterFlow: Flow[ByteString, ByteString, NotUsed] = Framing.delimiter(ByteString("\n"), 10, true)

  def fetchData(): Future[Source[String, Any]] = {
    val futureStreamedResponse = wsClient
      .url(s"$carjumpBaseUrl/test")
      .withQueryString("a" → "b")
      .withHeaders("Accept" → "text/plain")
      .withMethod("GET")
      .stream()
    futureStreamedResponse.map { response ⇒
      response.body
        .via(delimiterFlow)
        .map(_.utf8String)
    }
  }
}
