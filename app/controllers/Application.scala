package controllers

import be.objectify.deadbolt.scala.ActionBuilders
import com.google.inject.{Inject, Singleton}
import configs.DeadboltConfig
import play.api.libs.ws.WSClient
import play.api.mvc.{AbstractController, ControllerComponents, Results}

import scala.concurrent.ExecutionContext

@Singleton
class Application @Inject()(
                             actionBuilders: ActionBuilders
                             , ws: WSClient
                             , deadboltConfig: DeadboltConfig
                             , cc: ControllerComponents
                           )(implicit ec: ExecutionContext) extends AbstractController(cc) {

  def index = Action { _ =>
    Results.Redirect(routes.UserController.login())
  }

  def error(m: String) = Action { _ =>
    Ok(views.html.error(m))
  }

  def authError(cause: String) = Action {
    Ok(views.html.index(s"Authentication error. Cause:$cause"))
  }

}
