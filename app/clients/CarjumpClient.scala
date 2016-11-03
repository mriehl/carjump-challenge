package clients

import akka.stream.Materializer
import akka.stream.scaladsl.Sink
import akka.util.ByteString
import play.api.libs.ws.WSClient
import scala.concurrent.{ ExecutionContext, Future }
import util.SLF4JLogging

class CarjumpClient(
    wsClient:       WSClient,
    carjumpBaseUrl: CarjumpBaseUrl
)(implicit val ec: ExecutionContext, mat: Materializer) extends SLF4JLogging {

  def fetchData(): Future[Seq[String]] = {
    val futureStreamedResponse = wsClient
      .url(s"$carjumpBaseUrl/test")
      .withQueryString("a" → "b")
      .withHeaders("Accept" → "text/plain")
      .withMethod("GET")
      .stream()
    futureStreamedResponse.flatMap { response ⇒
      response.body.runWith(Sink.fold[Seq[String], ByteString](Seq.empty[String]) {
        case (accu, bytes) ⇒ accu ++ bytes.utf8String.split("\n")
      })
    }
  }
}
