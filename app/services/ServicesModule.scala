package services

import akka.stream.Materializer
import clients.CarjumpClient
import java.time.{ Clock, ZoneId }

import scala.concurrent.ExecutionContext

import play.api.Configuration
import com.softwaremill.macwire._

trait ServicesModule {

  implicit def configuration: Configuration

  implicit val executionContext: ExecutionContext
  implicit val materializer: Materializer

  lazy implicit val clock = Clock.system(ZoneId.of("Europe/Berlin"))

  def carjumpClient: CarjumpClient

  lazy val apiService = wire[CarjumpApiService]
}
