package controllers

import scala.concurrent.{ Future, ExecutionContext }

import play.api.mvc.{ AnyContent, Action, Controller }
import util.SLF4JLogging

class StatusController()(implicit ec: ExecutionContext) extends Controller with SLF4JLogging {
  def status: Action[AnyContent] = Action.async {
    request ⇒
      Future.successful(Ok("OK"))
  }
}
