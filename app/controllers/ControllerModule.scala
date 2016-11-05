package controllers

import akka.actor.ActorSystem
import scala.concurrent.ExecutionContext

import com.softwaremill.macwire._
import play.api.Configuration

trait ControllerModule {

  implicit def configuration: Configuration
  implicit def actorSystem: ActorSystem

  implicit val executionContext: ExecutionContext

  lazy val statusController: StatusController = wire[StatusController]
  lazy val indexQueryController: IndexQueryController = wire[IndexQueryController]
}
