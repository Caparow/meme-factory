package controllers

import be.objectify.deadbolt.scala.ActionBuilders
import com.google.inject.{Inject, Singleton}
import configs.DeadboltConfig
import models.auth.{Role, UserRole}
import models.{ResultExt, User, UserWithId}
import play.api.libs.ws.WSClient
import play.api.mvc.{AbstractController, ControllerComponents}
import services.UserService

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class UserController @Inject()(
                                actionBuilders: ActionBuilders
                                , ws: WSClient
                                , deadboltConfig: DeadboltConfig
                                , userService: UserService
                                , cc: ControllerComponents
                              )(implicit ec: ExecutionContext) extends AbstractController(cc) with ResultExt {

  private implicit val p = cc.parsers

  private def authAction = actionBuilders.RestrictAction(UserRole.name).defaultHandler()

  def loginForm = Action { implicit request =>
    request.body.asFormUrlEncoded.map(_.map { case (k, vs) => k -> vs.head }).map { formData =>
      userService.login(formData("loginField"), formData("passwdField")).convert { user =>
        import deadboltConfig._
        Redirect(routes.FeedController.hottest()).withSession(
          authTokenKey -> "",
          roleKey -> Role.apply("user").name,
          identifierKey -> user.id.toString
        )
      }.unsafeRunSync()
    }.getOrElse {
      BadRequest("Form is invalid")
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
      ).convert { v =>
        import deadboltConfig._
        Redirect(routes.FeedController.hottest()).withSession(
          authTokenKey -> "",
          roleKey -> Role.apply("user").name,
          identifierKey -> v.id.toString
        )
      }.unsafeRunSync()
    }.getOrElse {
      BadRequest("Form is invalid")
    }
  }

  def updateForm = authAction { implicit request =>
    val id = request.session.get(deadboltConfig.identifierKey).getOrElse("")
    request.body.asFormUrlEncoded.map(_.map { case (k, vs) => k -> vs.head }).map { formData =>
      userService.updateProfile(UserWithId(
        id.toLong,
        formData("loginField"),
        formData("passwdField"),
        None,
        None,
        None)
      ).convert { v =>
        import deadboltConfig._
        Redirect(routes.FeedController.hottest()).withSession(
          authTokenKey -> "",
          roleKey -> Role.apply("user").name,
          identifierKey -> v.id.toString
        )
      }.unsafeToFuture()
    }.getOrElse {
      Future(BadRequest("Form is invalid"))
    }
  }

  def login = Action { implicit request =>
    Ok(views.html.login("Please sign in"))
  }

  def register = Action { implicit request =>
    Ok(views.html.signup())
  }

  def update = Action {
    //TODO: provide here update html
    Ok(views.html.index("Your new application is ready."))
  }
}
