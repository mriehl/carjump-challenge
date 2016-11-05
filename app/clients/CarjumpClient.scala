package clients

import scala.concurrent.{ ExecutionContext, Future }

import akka.NotUsed
import akka.stream.Materializer
import akka.stream.scaladsl.{ Flow, Framing, Source }
import akka.util.ByteString
import play.api.libs.ws.{ StreamedResponse, WSClient }
import util.SLF4JLogging

class CarjumpClient(
    wsClient:       WSClient,
    carjumpBaseUrl: CarjumpBaseUrl
)(implicit val ec: ExecutionContext, mat: Materializer) extends SLF4JLogging {

  private val delimiterFlow: Flow[ByteString, ByteString, NotUsed] = Framing.delimiter(ByteString("\n"), 10, true)

  def fetchData(): Future[Source[String, Any]] = {
    val futureStreamedResponse = wsClient
      .url(s"$carjumpBaseUrl/A")
      .withHeaders("Accept" → "text/plain")
      .withMethod("GET")
      .stream()
    futureStreamedResponse.flatMap { response ⇒
      val status = response.headers.status
      if (status >= 400)
        Future.failed(new CarjumpApiException(s"Carjump API call failed: got status $status"))
      else
        Future.successful(
          response.body
            .via(delimiterFlow)
            .map(_.utf8String)
        )
    }
  }
}

case class CarjumpApiException(msg: String) extends Exception(msg)
