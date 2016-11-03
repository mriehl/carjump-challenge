package launch

import com.softwaremill.macwire._
import controllers.Assets
import play.api._
import play.api.ApplicationLoader.Context
import play.api.inject.Injector
import play.api.libs.crypto.AESCTRCrypter
import play.api.libs.logback.LogbackLoggerConfigurator
import play.api.libs.ws.ahc.AhcWSComponents
import play.api.routing.Router
import router.Routes
import util.SLF4JLogging

class CarjumpLoader extends ApplicationLoader {
  def load(context: Context): Application = {
    LoggerConfigurator(context.environment.classLoader).foreach(_.configure(context.environment))

    val module: BuiltInComponentsFromContext with CarjumpComponents = new BuiltInComponentsFromContext(context) with CarjumpComponents
    module.startJobs()
    module.application
  }
}

trait CarjumpComponents extends BuiltInComponents with CarjumpModule with AhcWSComponents {

  lazy val assets: Assets = wire[Assets]

  lazy val router: Router = {
    def prefixedRoutes(prefix: String): Routes = {
      wire[Routes]
    }
    prefixedRoutes("/")
  }

  override implicit lazy val executionContext = actorSystem.dispatcher
}
