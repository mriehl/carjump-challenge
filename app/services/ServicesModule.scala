package services

import java.time.{ Clock, ZoneId }

import scala.concurrent.ExecutionContext

import play.api.Configuration

trait ServicesModule {

  implicit def configuration: Configuration

  implicit val executionContext: ExecutionContext

  lazy implicit val clock = Clock.system(ZoneId.of("Europe/Berlin"))
}
