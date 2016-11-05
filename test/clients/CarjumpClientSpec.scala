package clients

import akka.stream.scaladsl.Sink
import java.net.URL
import org.specs2.mutable.Specification
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.client.WireMock.{ matching ⇒ wmatching }
import play.api.http.Status.{ OK, BAD_REQUEST }
import play.api.test.{ DefaultAwaitTimeout, FutureAwaits }

class CarjumpClientSpec extends Specification with FutureAwaits with DefaultAwaitTimeout {

  val prependingSink = Sink.fold[List[String], String](List.empty[String]) {
    case (accu, s) ⇒
      s :: accu
  }

  sequential

  "A carjump http client" should {

    "accept a 200 response" in new WiremockScope {

      wireMockServer.stubFor(get(urlEqualTo("/A"))
        .withHeader("Accept", WireMock.equalTo("text/plain"))
        .willReturn(aResponse()
          .withStatus(OK)
          .withHeader("Content-Type", "text/plain")
          .withBody("a\na\na\nb\nb")))
      val contents: Seq[String] = await(client.fetchData().flatMap(_.runWith(prependingSink)))
      contents.reverse must be equalTo List("a", "a", "a", "b", "b")
    }

    "fail when response >=400" in new WiremockScope {
      wireMockServer.stubFor(get(urlEqualTo("/A"))
        .withHeader("Accept", WireMock.equalTo("text/plain"))
        .willReturn(aResponse()
          .withStatus(BAD_REQUEST)))

      await(client.fetchData().flatMap(_.runWith(prependingSink))) must throwA[CarjumpApiException]
    }
  }
}
