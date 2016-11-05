package jobs

import clients.CarjumpClient
import services.CarjumpApiService
import scala.concurrent.ExecutionContext

import akka.actor.{ ActorSystem, Props }
import akka.stream.Materializer
import com.softwaremill.macwire._
import play.api.Configuration

trait JobsModule {

  implicit val actorSystem: ActorSystem

  implicit val executionContext: ExecutionContext

  implicit def configuration: Configuration

  implicit val materializer: Materializer

  def carjumpClient: CarjumpClient
  def apiService: CarjumpApiService
  import scala.concurrent.duration._
  val duration = 30.seconds

  lazy val jobsSupervisorActor = {
    def fetchingActor = wire[FetchingActor]
    actorSystem.actorOf(Props(wire[JobsSupervisorActor]), "supervisor")
  }
}
