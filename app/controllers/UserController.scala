package controllers

import java.io.File
import java.nio.file.{Files, Paths}
import java.time.{ZoneId, ZonedDateTime}
import java.util.Base64

import be.objectify.deadbolt.scala.ActionBuilders
import com.google.inject.{Inject, Singleton}
import configs.DeadboltConfig
import models.auth.{Role, UserRole}
import models._
import play.api.libs.ws.WSClient
import play.api.mvc.{AbstractController, ControllerComponents}
import services.UserService

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

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
        Redirect(routes.FeedController.hottest(1)).withSession(
          authTokenKey -> "",
          roleKey -> Role.apply("user").name,
          identifierKey -> user.id.toString
        )
      }.unsafeRunSync()
    }.getOrElse {
      BadRequest("Form is invalid")
    }
  }

  def signOut = Action { implicit request =>
    Redirect(routes.FeedController.hottest(1)).withNewSession
  }

  def registerForm = Action.async(parse.multipartFormData) { request =>
    val dataParts = request.body.dataParts
    val res = for {
      loginField <- dataParts.get("loginField").flatMap(_.headOption)
      passwordField <- dataParts.get("passwordField").flatMap(_.headOption)
    } yield {
      val surnameField = dataParts.get("surnameField").flatMap(_.headOption)
      val firstNameField = dataParts.get("firstNameField").flatMap(_.headOption)
      val avatar = request.body.file("avatar").flatMap { image =>
        Try {
          val encoded = Base64.getEncoder.encodeToString(Files.readAllBytes(Paths.get(image.ref.file.getAbsolutePath)))
          val filename = Paths.get(image.filename).getFileName.toString
          val cType = filename.substring(filename.lastIndexOf("."))
          (encoded, cType)
        }.toOption
      }

      val user = User(
        loginField,
        passwordField,
        surnameField,
        firstNameField,
        avatar.map(_._1),
        avatar.map(_._2)
      )

      userService.register(user).convert { v =>
        import deadboltConfig._
        Redirect(routes.FeedController.hottest(1)).withSession(
          authTokenKey -> "",
          roleKey -> Role.apply("user").name,
          identifierKey -> v.id.toString
        )
      }.unsafeToFuture()
    }
    res.getOrElse(Future(BadRequest("Form is invalid")))
  }

  def updateForm = authAction(parse.multipartFormData) { implicit request =>
    val id = request.session.get(deadboltConfig.identifierKey).getOrElse("")

    val dataParts = request.body.dataParts
    val res = for {
      loginField <- dataParts.get("loginField").flatMap(_.headOption)
      passwordField <- dataParts.get("passwordField").flatMap(_.headOption)
    } yield {
      val surnameField = dataParts.get("surnameField").flatMap(_.headOption)
      val firstNameField = dataParts.get("firstNameField").flatMap(_.headOption)
      val avatar = request.body.file("avatar").flatMap { image =>
        Try {
          val encoded = Base64.getEncoder.encodeToString(Files.readAllBytes(Paths.get(image.ref.file.getAbsolutePath)))
          val filename = Paths.get(image.filename).getFileName.toString
          val cType = filename.substring(filename.lastIndexOf("."))
          (encoded, cType)
        }.toOption
      }

      val user = models.UserWithId(
        id.toLong,
        loginField,
        passwordField,
        surnameField,
        firstNameField,
        avatar.map(_._1),
        avatar.map(_._2)
      )

      userService.updateProfile(user).convert { v =>
        import deadboltConfig._
        Redirect(routes.FeedController.hottest(1)).withSession(
          authTokenKey -> "",
          roleKey -> Role.apply("user").name,
          identifierKey -> v.id.toString
        )
      }.unsafeToFuture()
    }
    res.getOrElse(Future(BadRequest("Form is invalid")))
  }

  def avatarResource(id: Long) = authAction { implicit request =>
    userService.getAvatar(id).convert { contentO =>
      contentO.map { content =>
        val bytes = Base64.getDecoder.decode(content._1)
        val tempFile = File.createTempFile("avatar", content._2)
        val path = Paths.get(tempFile.getAbsolutePath)
        Files.write(path, bytes)
        Ok.sendFile(tempFile)
      } getOrElse {
        Assets.Redirect(routes.Assets.versioned("images/defaultAvatar.png"))
      }
    }.unsafeToFuture()
  }

  def user(idS: Long) = Action.async { implicit request =>
    val id = request.session.get(deadboltConfig.identifierKey).flatMap(i => Try(i.toLong).toOption)
    userService.getUser(idS).convert { uS =>
      val u = id.map(i => userService.getUser(i).unsafeRunSync().right.get)
      Ok(views.html.user(uS, u))
    }.unsafeToFuture()
  }

  def login = Action { implicit request =>
    Ok(views.html.login("Please sign in"))
  }

  def register = Action { implicit request =>
    Ok(views.html.signup())
  }

  def update = authAction { implicit request =>
    val id = request.session.get(deadboltConfig.identifierKey).getOrElse("").toLong
    userService.getUser(id).convert { u =>
      Ok(views.html.updateUser(u))
    }.unsafeToFuture()
  }
}
