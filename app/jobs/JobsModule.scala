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
  def mandatoryPropertyMissing(path: String): Nothing
  def carjumpClient: CarjumpClient
  def apiService: CarjumpApiService

  lazy val fetchInterval: CarjumpFetchInterval = {
    import scala.concurrent.duration._
    val path = "carjump.fetchIntervalSeconds"
    val fetchIntervalSeconds = configuration.getInt(path).getOrElse(mandatoryPropertyMissing(path))
    CarjumpFetchInterval(fetchIntervalSeconds.seconds)
  }

  lazy val jobsSupervisorActor = {
    def fetchingActor = wire[FetchingActor]
    actorSystem.actorOf(Props(wire[JobsSupervisorActor]), "supervisor")
  }
}
