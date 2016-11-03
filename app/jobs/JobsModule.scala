package jobs

import scala.concurrent.ExecutionContext

import akka.actor.{ ActorSystem, Props }
import akka.stream.Materializer
import com.softwaremill.macwire._
import play.api.Configuration

trait JobsModule {

  implicit val actorSystem: ActorSystem

  implicit val executionContext: ExecutionContext

  implicit def configuration: Configuration

  implicit def materializer: Materializer

  lazy val jobsSupervisorActor = {
    actorSystem.actorOf(Props(wire[JobsSupervisorActor]))
  }
}
