package controllers

import java.io.File
import java.nio.file.{Files, Paths}
import java.time.{ZoneId, ZonedDateTime}
import java.util.Base64

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.FileIO
import be.objectify.deadbolt.scala.{ActionBuilders, AuthenticatedRequest}
import cats.effect.IO
import com.google.inject.{Inject, Singleton}
import configs.DeadboltConfig
import io.circe.syntax._
import models._
import models.auth.UserRole
import play.api.libs.ws.WSClient
import play.api.mvc.{AbstractController, AnyContent, ControllerComponents}
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

  def search(target: String, feedOffset: FeedOffset = FeedOffset(0, 25)) = Action.async { implicit request =>
    import deadboltConfig._
    (for {
      userId <- IO.pure(request.session.get(identifierKey).flatMap(i => Try(i.toLong).toOption))
      uId <- userId match {
        case Some(u) => userService.getUser(u).map(_.toOption)
        case None => IO.pure(None)
      }
      res <- memeService.getTargetPosts(target, feedOffset).convert { memes =>
        Ok(views.html.feed(memes, uId))
      }
    } yield res).unsafeToFuture()
  }

  def post(id: Long) = Action.async { request =>
    import deadboltConfig._
    (for {
      userId <- IO.pure(request.session.get(identifierKey).flatMap(i => Try(i.toLong).toOption))
      uId <- userId match {
        case Some(u) => userService.getUser(u).map(_.toOption)
        case None => IO.pure(None)
      }
      res <- memeService.getPostWithComments(id).convert { post =>
        Ok(views.html.post(post, uId))
      }
    } yield res).unsafeToFuture()
  }

  def hottest(forDays: Int = 1, feedOffset: FeedOffset = FeedOffset(0, 25)) = Action.async { implicit request =>
    import deadboltConfig._
    (for {
      userId <- IO.pure(request.session.get(identifierKey).flatMap(i => Try(i.toLong).toOption))
      uId <- userId match {
        case Some(u) => userService.getUser(u).map(_.toOption)
        case None => IO.pure(None)
      }
      res <- memeService.getMostPopular(forDays, feedOffset).convert { memes =>
        Ok(views.html.feed(memes, uId))
      }
    } yield res).unsafeToFuture()
  }

  def latest(feedOffset: FeedOffset = FeedOffset(0, 25)) = Action.async { request =>
    import deadboltConfig._
    (for {
      userId <- IO.pure(request.session.get(identifierKey).flatMap(i => Try(i.toLong).toOption))
      uId <- userId match {
        case Some(u) => userService.getUser(u).map(_.toOption)
        case None => IO.pure(None)
      }
      res <- memeService.getLatest(feedOffset).convert { memes =>
        Ok(views.html.feed(memes, uId))
      }
    } yield res).unsafeToFuture()
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

  private def vote(id: Long, mark: Int, contentType: String)
                  (vote: (Long, Long) => IO[Either[ServiceException, Long]])
                  (getPoints: Long => IO[Either[ServiceException, Long]])
                  (implicit request: AuthenticatedRequest[AnyContent]) = {
    val uId: Long = request.session.get(deadboltConfig.identifierKey).flatMap(i => Try(i.toLong).toOption).getOrElse(0)
    userService.getUserMark(id, uId, contentType).flatMap {
      case Right(m) =>
        m match {
          case Some(um) =>
            if (um == 0) {
              userService.updateUserMark(mark, id, uId, contentType).flatMap { _ => vote(uId, id) }
            } else if (um == mark) {
              getPoints(id)
            } else {
              userService.updateUserMark(0, id, uId, contentType).flatMap { _ => vote(uId, id) }
            }
          case None => userService.setUserMark(mark, id, uId, contentType).flatMap { _ => vote(uId, id) }
        }
      case Left(f) => IO.pure(Left(f))
    }.convert { memes =>
      Ok(memes.asJson.spaces2)
    }.unsafeToFuture()
  }

  def upVotePost(id: Long) = authAction { implicit request =>
    vote(id,1,"meme")(memeService.upVoteMeme)(memeService.getMemePoints)
  }

  def downVotePost(id: Long) = authAction { implicit request =>
    vote(id,-1,"meme")(memeService.downVoteMeme)(memeService.getMemePoints)
  }

  def upVoteComment(id: Long) = authAction { implicit request =>
    vote(id,1,"comment")(memeService.upVoteComment)(memeService.getCommentPoints)
  }

  def downVoteComment(id: Long) = authAction { implicit request =>
    vote(id,-1,"comment")(memeService.downVoteComment)(memeService.getCommentPoints)
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
      ).convert { _ =>
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
    (for {
      userId <- IO.pure(request.session.get(identifierKey).flatMap(i => Try(i.toLong).toOption))
      uId <- userId match {
        case Some(u) => userService.getUser(u).map(_.toOption)
        case None => IO.pure(None)
      }
      res = Ok(views.html.create_meme(uId.get))
    } yield res).unsafeToFuture()
  }

  def deleteComment(id: Long) = authAction {
    Future(Ok(views.html.index("Your new application is ready.")))
  }

  def deletePost(id: Long) = authAction {
    Future(Ok(views.html.index("Your new application is ready.")))
  }

}
