package jobs

import akka.actor.{ Actor, ActorLogging, Cancellable, FSM }
import scala.concurrent.Await
import scala.util.Try
import services.{ CarjumpApiService, Compressed }
import scala.concurrent.duration.FiniteDuration
import scala.concurrent.duration._
import scala.util.control.NonFatal

class FetchingActor(interval: FiniteDuration, apiService: CarjumpApiService) extends Actor with ActorLogging with FSM[FetchingActor.State, FetchingActor.Data] {
  import FetchingActor._
  implicit val ec = context.dispatcher

  startWith(Stopped, Uninitialized)

  when(Stopped) {
    case Event(Start, _) ⇒
      log.info("Fetching actor starting up..")
      val scheduleHook = context.system.scheduler.schedule(1.seconds, interval)(self ! Fetch)
      goto(Idle) using FetchData(scheduleHook, Seq.empty)

    case Event(CacheValue, _) ⇒
      sender() ! Seq.empty[Compressed[String]]
      stay()
  }

  when(Idle) {
    case Event(Fetch, FetchData(hook, emptyCache)) ⇒
      log.info("Fetching initial data...")
      val transition = try {
        val cache = Await.result(apiService.fetchCompressedData(), 3.seconds)
        log.info(cache.toString)
        goto(IdleWithCache) using FetchData(hook, cache)
      } catch {
        case NonFatal(e) ⇒
          log.error(e.getMessage)
          stay()
      }
      transition

    case Event(CacheValue, _) ⇒
      sender() ! Seq.empty[Compressed[String]]
      stay()

    case Event(Stop, FetchData(hook, _)) ⇒
      hook.cancel()
      goto(Stopped) using Uninitialized
  }

  when(IdleWithCache) {
    case Event(Fetch, FetchData(hook, oldCache)) ⇒
      log.info("Fetching data to replace existing cache..")
      log.info("Current: " + oldCache)
      val newCache = Try(Await.result(apiService.fetchCompressedData(), 3.seconds))
      stay() using FetchData(hook, newCache.getOrElse(oldCache))

    case Event(Stop, FetchData(hook, _)) ⇒
      hook.cancel()
      goto(Stopped) using Uninitialized

    case Event(CacheValue, FetchData(_, cache)) ⇒
      sender() ! cache
      stay()
  }
}

object FetchingActor {
  sealed trait Msg
  case object Start extends Msg
  case object Stop extends Msg
  case object Fetch extends Msg
  case object CacheValue extends Msg

  sealed trait State
  case object Idle extends State
  case object IdleWithCache extends State
  case object Stopped extends State

  sealed trait Data
  case object Uninitialized extends Data
  final case class FetchData(scheduleHook: Cancellable, d: Seq[Compressed[String]]) extends Data
}
