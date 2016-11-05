package controllers

import akka.actor.ActorSystem
import akka.pattern.ask
import akka.util.Timeout
import jobs.FetchingActor
import scala.concurrent.{ Future, ExecutionContext }

import play.api.mvc.{ AnyContent, Action, Controller }
import scala.util.Try
import services.CarjumpApiService._
import services.Compressed
import util.SLF4JLogging
import scala.concurrent.duration._

class IndexQueryController()(implicit val ec: ExecutionContext, implicit val as: ActorSystem) extends Controller with SLF4JLogging {
  private val fetchingActorSelection = as.actorSelection("/user/supervisor/fetch")
  private implicit val timeout = Timeout(2.seconds)

  def index(index: Int): Action[AnyContent] = Action.async { request ⇒
    val futureCompressedData = ask(fetchingActorSelection, FetchingActor.CacheValue).mapTo[Seq[Compressed[String]]]
    futureCompressedData
      .map(compressedData ⇒ Try(compressedData atIndex index))
      .map(_.map(dataAtIndex ⇒ Ok(dataAtIndex)).getOrElse(NotFound(s"Index $index out of bounds, or no cached data yet")))
  }
}
