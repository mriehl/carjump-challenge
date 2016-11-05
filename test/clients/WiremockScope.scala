package clients

import akka.actor.ActorSystem
import akka.stream.scaladsl.Sink
import akka.stream.{ ActorMaterializer, Materializer }
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.core.WireMockConfiguration._
import java.net.URL
import org.specs2.mutable.After
import org.specs2.specification.Scope
import play.api.libs.ws.WSClient
import play.api.libs.ws.ahc.{ AhcWSClient, AhcWSClientConfig }
import scala.concurrent.{ Await, ExecutionContext }
import scala.concurrent.duration._

trait WiremockScope extends After {

  lazy implicit val as: ActorSystem = ActorSystem()
  lazy implicit val ec: ExecutionContext = as.dispatcher
  lazy implicit val mat: Materializer = ActorMaterializer()
  lazy val wsClient: WSClient = AhcWSClient(AhcWSClientConfig())

  lazy val port = 13089
  lazy val wireMockServer: WireMockServer = new WireMockServer(wireMockConfig().port(port))
  lazy val client = new CarjumpClient(wsClient, CarjumpBaseUrl(new URL(s"http://localhost:$port")))
  wireMockServer.start()

  override def after: Unit = {
    wireMockServer.stop()
    wsClient.close
    as.terminate()
    Await.result(as.whenTerminated.map(_ ⇒ ()), 1.seconds)
  }
}
