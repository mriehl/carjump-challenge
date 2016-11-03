package jobs

import akka.actor._
import jobs.JobsSupervisorActor.{ JobDispatcherName, StartAll, StopAll }

class JobsSupervisorActor() extends Actor {

  override val supervisorStrategy =
    OneForOneStrategy() {
      case _: ActorInitializationException ⇒ SupervisorStrategy.Stop
      case _: ActorKilledException         ⇒ SupervisorStrategy.Stop
      case _: DeathPactException           ⇒ SupervisorStrategy.Stop
      case _: Exception                    ⇒ SupervisorStrategy.Resume
    }

  def receive: PartialFunction[Any, Unit] = {
    case StartAll ⇒ ()
    case StopAll  ⇒ ()
  }
}

object JobsSupervisorActor {
  case object StartAll
  case object StopAll
  val JobDispatcherName = "carjump-dispatcher"
}
