package controllers

import scala.concurrent.ExecutionContext

import com.softwaremill.macwire._
import play.api.Configuration

trait ControllerModule {

  implicit def configuration: Configuration

  implicit val executionContext: ExecutionContext

  lazy val statusController: StatusController = wire[StatusController]
}
