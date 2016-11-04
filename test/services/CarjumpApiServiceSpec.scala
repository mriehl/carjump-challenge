package services

import akka.stream.scaladsl.Sink
import clients.{ CarjumpBaseUrl, CarjumpClient, WiremockScope }
import java.net.URL
import org.specs2.mutable.Specification
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.client.WireMock.{ matching ⇒ wmatching }
import play.api.http.Status.OK
import play.api.test.{ DefaultAwaitTimeout, FutureAwaits }

class CarjumpApiServiceSpec extends Specification with FutureAwaits with DefaultAwaitTimeout {

  sequential

  "A carjump api service" should {

    "fetch compressed data" in new WiremockScope {
      wireMockServer.stubFor(get(urlEqualTo("/test?a=b"))
        .withHeader("Accept", WireMock.equalTo("text/plain"))
        .willReturn(aResponse()
          .withStatus(OK)
          .withHeader("Content-Type", "text/plain")
          .withBody("a\na\na\nb\nb\nc\na\na\n")))
      val client = new CarjumpClient(wsClient, CarjumpBaseUrl(new URL(s"http://localhost:$port")))
      import com.softwaremill.macwire._
      val carjumpService: CarjumpApiService = wire[CarjumpApiService]

      val compressedContents = await(carjumpService.fetchCompressedData())
      compressedContents must be equalTo List(Repeat(3, "a"), Repeat(2, "b"), Single("c"), Repeat(2, "a"))
    }
  }
}
