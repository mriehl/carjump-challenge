package launch

import scala.concurrent.{ ExecutionContext, Future, blocking }

import clients.ClientsModule
import controllers.ControllerModule
import jobs.{ JobsModule, JobsSupervisorActor }
import play.api.Configuration
import play.api.inject.ApplicationLifecycle
import services.ServicesModule
import util.SLF4JLogging

trait CarjumpModule
    extends ControllerModule
    with ServicesModule
    with JobsModule
    with ClientsModule
    with SLF4JLogging {

  def configuration: Configuration
  def applicationLifecycle: ApplicationLifecycle
  override implicit val executionContext: ExecutionContext

  def startJobs(): Unit = {
    jobsSupervisorActor ! JobsSupervisorActor.StartAll

    applicationLifecycle.addStopHook(() ⇒ stopActors())
  }

  private def stopActors(): Future[Unit] = {
    Future {
      jobsSupervisorActor ! JobsSupervisorActor.StopAll
    }
  }
}
