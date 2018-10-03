package controllers

import be.objectify.deadbolt.scala.ActionBuilders
import com.google.inject.{Inject, Singleton}
import configs.DeadboltConfig
import models.auth.Role
import models.{User, UserWithId}
import play.api.libs.ws.WSClient
import play.api.mvc.{AbstractController, ControllerComponents}
import play.libs.Json
import services.UserService

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class UserController @Inject()(
                                actionBuilders: ActionBuilders
                                , ws: WSClient
                                , deadboltConfig: DeadboltConfig
                                , userService: UserService
                                , cc: ControllerComponents
                              )(implicit ec: ExecutionContext) extends AbstractController(cc) {

  def loginForm = Action { implicit request =>
    request.body.asFormUrlEncoded.map(_.map { case (k, vs) => k -> vs.head }).map { formData =>
      Ok(views.html.index(userService.login(formData("loginField"), formData("passwdField")).unsafeRunSync().toString))
    }.getOrElse {
      Ok(views.html.index("sorry"))
    }
  }

  def registerForm = Action { implicit request =>
    request.body.asFormUrlEncoded.map(_.map { case (k, vs) => k -> vs.head }).map { formData =>
      userService.register(User(
        formData("loginField"),
        formData("passwdField"),
        None,
        None,
        None)
      ).unsafeRunSync() match {
        case Right(v) =>
          import deadboltConfig._
          Redirect(routes.FeedController.hottest()).withSession(
            authTokenKey -> "",
            roleKey -> Role.apply("admin").name,
            identifierKey -> v.id.toString
          )
        case Left(_) => Redirect(routes.Application.authError("err"))
      }
    }.getOrElse {
      BadRequest("Form is invalid")
    }
  }

  def updateForm = Action {
    Ok(views.html.index("Your new application is ready."))
  }

  def login = Action { implicit request =>
    Ok(views.html.login("Please sign in"))
  }

  def register = Action { implicit request =>
    Ok(views.html.signup())
  }

  def update(userWithId: UserWithId) = Action {
    Ok(views.html.index("Your new application is ready."))
  }
}
