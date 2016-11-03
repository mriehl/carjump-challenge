package clients

import scala.concurrent.ExecutionContext

import play.api.Configuration
import play.api.libs.ws.WSClient

trait ClientsModule {
  def wsClient: WSClient

  implicit def configuration: Configuration

  implicit val executionContext: ExecutionContext
}
