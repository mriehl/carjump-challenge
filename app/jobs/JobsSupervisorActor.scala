package jobs

import akka.actor._
import jobs.JobsSupervisorActor.{ JobDispatcherName, StartAll, StopAll }

class JobsSupervisorActor(
    fetchingActorThunk: ⇒ FetchingActor
) extends Actor {

  override val supervisorStrategy =
    OneForOneStrategy() {
      case _: ActorInitializationException ⇒ SupervisorStrategy.Stop
      case _: ActorKilledException         ⇒ SupervisorStrategy.Stop
      case _: DeathPactException           ⇒ SupervisorStrategy.Stop
      case _: Exception                    ⇒ SupervisorStrategy.Resume
    }

  val fetchingActor = context.actorOf(Props(fetchingActorThunk).withDispatcher("cj-dispatcher"))

  def receive: PartialFunction[Any, Unit] = {
    case StartAll ⇒ fetchingActor ! FetchingActor.Start
    case StopAll  ⇒ fetchingActor ! FetchingActor.Stop
  }
}

object JobsSupervisorActor {
  case object StartAll
  case object StopAll
  val JobDispatcherName = "cj-dispatcher"
}
