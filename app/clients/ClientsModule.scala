package clients

import akka.stream.Materializer
import java.net.URL
import scala.concurrent.ExecutionContext

import play.api.Configuration
import play.api.libs.ws.WSClient

trait ClientsModule {
  import ClientsModule._
  def wsClient: WSClient

  implicit def configuration: Configuration

  implicit val executionContext: ExecutionContext
  implicit val materializer: Materializer

  private def mandatoryPropertyMissing(path: String): Nothing = throw new UnconfiguredApplicationException(s"Missing mandatory property in application.conf: '$path'")

  lazy val carjumpBaseUrl: CarjumpBaseUrl = CarjumpBaseUrl(new URL(configuration.getString(carjumpUrlPath)
    .getOrElse(mandatoryPropertyMissing(carjumpUrlPath))))

  import com.softwaremill.macwire._
  lazy val carjumpClient: CarjumpClient = wire[CarjumpClient]
}

object ClientsModule {
  val carjumpUrlPath: String = "carjump.url"
}
