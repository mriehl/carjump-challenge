package clients

import java.net.URL
import org.specs2.mutable.Specification
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.client.WireMock.{ matching ⇒ wmatching }
import play.api.http.Status.OK
import play.api.test.{ DefaultAwaitTimeout, FutureAwaits }

class CarjumpClientSpec extends Specification with FutureAwaits with DefaultAwaitTimeout {

  sequential

  "A carjump http client" should {

    "accept a 200 response" in new WiremockScope {
      wireMockServer.stubFor(get(urlEqualTo("/test?a=b"))
        .withHeader("Accept", WireMock.equalTo("text/plain"))
        .willReturn(aResponse()
          .withStatus(OK)
          .withHeader("Content-Type", "text/plain")
          .withBody("a\na\na\nb\nb")))
      val client = new CarjumpClient(wsClient, CarjumpBaseUrl(new URL(s"http://localhost:$port")))

      val contents: Seq[String] = await(client.fetchData())
      contents must be equalTo Seq("a", "a", "a", "b", "b")
    }
  }
}
