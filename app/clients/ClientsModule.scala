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
  def mandatoryPropertyMissing(path: String): Nothing

  implicit val executionContext: ExecutionContext
  implicit val materializer: Materializer

  lazy val carjumpBaseUrl: CarjumpBaseUrl = CarjumpBaseUrl(new URL(configuration.getString(carjumpUrlPath)
    .getOrElse(mandatoryPropertyMissing(carjumpUrlPath))))

  import com.softwaremill.macwire._
  lazy val carjumpClient: CarjumpClient = wire[CarjumpClient]
}

object ClientsModule {
  val carjumpUrlPath: String = "carjump.url"
}
