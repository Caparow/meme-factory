package controllers

import java.io.File
import java.nio.file.{Files, Paths}
import java.time.{ZoneId, ZonedDateTime}
import java.util.Base64

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.FileIO
import be.objectify.deadbolt.scala.ActionBuilders
import com.google.inject.{Inject, Singleton}
import configs.DeadboltConfig
import io.circe.syntax._
import models._
import models.auth.UserRole
import play.api.libs.ws.WSClient
import play.api.mvc.{AbstractController, ControllerComponents}
import services.{MemeService, UserService}
import services.persistence.PostsPersistence.FeedOffset

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

@Singleton
class FeedController @Inject()(
                                memeService: MemeService
                                , userService: UserService
                                , actionBuilders: ActionBuilders
                                , ws: WSClient
                                , deadboltConfig: DeadboltConfig
                                , cc: ControllerComponents
                              )(implicit ec: ExecutionContext) extends AbstractController(cc) with ResultExt {

  private implicit val p = cc.parsers

  implicit val system = ActorSystem("QuickStart")
  implicit val materializer = ActorMaterializer()

  private def authAction = actionBuilders.RestrictAction(UserRole.name).defaultHandler()

  def post(id: Long) = Action.async { request =>
    import deadboltConfig._
    val uId: Option[UserWithId] = request.session.get(identifierKey).flatMap(i => Try(i.toLong).toOption)
      .map(id => userService.getUser(id).unsafeRunSync().right.get)
    memeService.getPostWithComments(id).convert { post =>
      Ok(views.html.post(post, uId))
    }.unsafeToFuture()
  }

  def hottest(forDays: Int = 1, feedOffset: FeedOffset = FeedOffset(0, 25)) = Action.async { implicit request =>
    import deadboltConfig._
    val uId: Option[UserWithId] = request.session.get(identifierKey).flatMap(i => Try(i.toLong).toOption)
      .map(id => userService.getUser(id).unsafeRunSync().right.get)
    memeService.getMostPopular(forDays, feedOffset).convert { memes =>
      Ok(views.html.feed(memes, uId))
    }.unsafeToFuture()
  }

  def latest(feedOffset: FeedOffset = FeedOffset(0, 25)) = Action.async { request =>
    import deadboltConfig._
    val uId: Option[UserWithId] = request.session.get(identifierKey).flatMap(i => Try(i.toLong).toOption)
      .map(id => userService.getUser(id).unsafeRunSync().right.get)
    memeService.getLatest(feedOffset).convert { memes =>
      Ok(views.html.feed(memes, uId))
    }.unsafeToFuture()
  }

  def resource(memeId: Long, num: Long) = Action.async {
    memeService.getContent(memeId, num).convert { content =>
      val bytes = Base64.getDecoder.decode(content.content)
      val tempFile = File.createTempFile("resource", content.contentType)
      val path = Paths.get(tempFile.getAbsolutePath)
      Files.write(path, bytes)
      content.contentType match {
        case tt if ContentTypes.isImage(tt) => Ok.sendFile(tempFile)
        case tt if ContentTypes.isVideo(tt) =>
          val io = FileIO.fromPath(path)
          Ok.chunked(io)
        case tt if ContentTypes.isAudio(tt) =>
          val io = FileIO.fromPath(path)
          Ok.chunked(io)
        case _ => Ok(content.content)
      }
    }.unsafeToFuture()
  }

  def upVotePost(id: Long) = authAction { implicit request =>
    val uId: Long = request.session.get(deadboltConfig.identifierKey).flatMap(i => Try(i.toLong).toOption).getOrElse(0)
    memeService.upVoteMeme(uId, id).convert { memes =>
      Ok(views.html.index(memes.asJson.spaces2))
    }.unsafeToFuture()
  }

  def downVotePost(id: Long) = authAction { implicit request =>
    val uId: Long = request.session.get(deadboltConfig.identifierKey).flatMap(i => Try(i.toLong).toOption).getOrElse(0)
    memeService.downVoteMeme(uId, id).convert { memes =>
      Ok(views.html.index(memes.asJson.spaces2))
    }.unsafeToFuture()
  }

  def upVoteComment(id: Long) = authAction { implicit request =>
    val uId: Long = request.session.get(deadboltConfig.identifierKey).flatMap(i => Try(i.toLong).toOption).getOrElse(0)
    memeService.upVoteComment(uId, id).convert { memes =>
      Ok(views.html.index(memes.asJson.spaces2))
    }.unsafeToFuture()
  }

  def downVoteComment(id: Long) = authAction { implicit request =>
    val uId: Long = request.session.get(deadboltConfig.identifierKey).flatMap(i => Try(i.toLong).toOption).getOrElse(0)
    memeService.downVoteComment(uId, id).convert { memes =>
      Ok(views.html.index(memes.asJson.spaces2))
    }.unsafeToFuture()
  }

  def newCommentForm(id: Long) = authAction { implicit request =>
    import deadboltConfig._
    val uId = request.session.get(identifierKey).getOrElse("")
    request.body.asFormUrlEncoded.map(_.map { case (k, vs) => k -> vs.head }).map { formData =>
      memeService.createComment(
        CommentItem(
          id,
          formData("textField"),
          ZonedDateTime.now().withZoneSameInstant(ZoneId.of("UTC")).toLocalDateTime,
          0,
          uId.toLong
        )
      ).convert { v =>
        Redirect(routes.FeedController.post(id))
      }.unsafeToFuture()
    }.get
  }

  def createPostForm = authAction(parse.multipartFormData) { implicit request =>
    val uId = request.session.get(deadboltConfig.identifierKey).getOrElse("")
    val dataParts = request.body.dataParts
    val res = for {
      title <- dataParts.get("titleField").flatMap(_.headOption)
      image <- request.body.file("imageField")
    } yield {
      val encoded = Base64.getEncoder.encodeToString(Files.readAllBytes(Paths.get(image.ref.file.getAbsolutePath)))
      val filename = Paths.get(image.filename).getFileName.toString
      val cType = filename.substring(filename.lastIndexOf("."))
      val meme = MemeItem(
        title,
        ZonedDateTime.now().withZoneSameInstant(ZoneId.of("UTC")).toLocalDateTime,
        List(Content(0, cType, encoded, 1), Content(0, ContentTypes.HTML, "some", 2)),
        0,
        uId.toLong
      )
      memeService.createMeme(meme).convert { _ =>
        Redirect(routes.FeedController.hottest())
      }.unsafeToFuture()
    }
    res.getOrElse(Future(BadRequest("Form is invalid")))
  }

  def createPost() = authAction { request =>
    import deadboltConfig._
    val u: Option[UserWithId] = request.session.get(identifierKey).flatMap(i => Try(i.toLong).toOption)
      .map(id => userService.getUser(id).unsafeRunSync().right.get)
    Future(Ok(views.html.create_meme(u.get)))
  }

  def deleteComment(id: Long) = authAction {
    Future(Ok(views.html.index("Your new application is ready.")))
  }

  def deletePost(id: Long) = authAction {
    Future(Ok(views.html.index("Your new application is ready.")))
  }

}
