package controllers

import com.google.inject.{Inject, Singleton}
import models.{User, UserWithId}
import play.api.libs.ws.WSClient
import play.api.mvc.{AbstractController, ControllerComponents}
import play.libs.Json
import services.UserService

@Singleton
class UserController @Inject()(ws: WSClient
                               , userService: UserService
                               , cc: ControllerComponents
                              ) extends AbstractController(cc) {

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
        case Right(v) => Ok(views.html.index(v.toString))
        case Left(_) => Ok(views.html.index("sorry"))
      }
    }.getOrElse {
      Ok(views.html.index("sorry"))
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
