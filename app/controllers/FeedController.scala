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
import play.api.mvc._
import services.{MemeService, UserService}
import services.persistence.PostsPersistence.FeedOffset

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try
import FeedController._
import play.api.libs.Files.TemporaryFile

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

  private def getUser(implicit request: Request[AnyContent]) = {
    import deadboltConfig._
    for {
      userId <- IO.pure(request.session.get(identifierKey).flatMap(i => Try(i.toLong).toOption))
      uId <- userId match {
        case Some(u) => userService.getUser(u).map(_.toOption)
        case None => IO.pure(None)
      }
    } yield uId
  }

  private def feedList(page: Int, getMemes: FeedOffset => Result[List[MemeItemWithId]], getTotal: => Result[Int], next: Int => Call, errorOnEmpty: String)(implicit request: Request[AnyContent]): Future[play.api.mvc.Result] = {
    (for {
      uId <- getUser
      total <- getTotal
      res <-
        if (total.exists(_ != 0)) {
          getMemes(FeedOffset(DEF_LIMIT * (page - 1), DEF_LIMIT)).convert { memes =>
            Ok(views.html.feed(memes, uId, next: Int => Call, total.getOrElse(1) / DEF_LIMIT, page))
          }
        } else IO.pure(Ok(views.html.error(errorOnEmpty)))
    } yield res).unsafeToFuture()
  }

  def search(target: String, page: Int) = Action.async { implicit request =>
    feedList(
      page,
      (f: FeedOffset) => memeService.getTargetPosts(target, f),
      memeService.countSearchTitles(target),
      (i: Int) => routes.FeedController.search(target, i),
      errorOnEmpty = "There is no such posts."
    )
  }

  def post(id: Long) = Action.async { implicit request =>
    (for {
      uId <- getUser
      res <- memeService.getPostWithComments(id).convert { post =>
        Ok(views.html.post(post, uId))
      }
    } yield res).unsafeToFuture()
  }

  def hottest(page: Int, forDays: Int = 5) = Action.async { implicit request =>
    feedList(
      page,
      (f: FeedOffset) =>memeService.getMostPopular(forDays, f),
      memeService.countMostPopular(forDays),
      routes.FeedController.hottest,
      errorOnEmpty = "Current memes list are empty.\n Wait till someone will create a new meme.\n OR TRY TO CREATE THEM YOURSELF! "
    )
  }

  def latest(page: Int) = Action.async { implicit request =>
    feedList(
      page,
      (f: FeedOffset) => memeService.getLatest(f),
      memeService.countLatest(),
      routes.FeedController.latest,
      errorOnEmpty = "Current memes list are empty.\n Wait till someone will create a new meme.\n OR TRY TO CREATE THEM YOURSELF! "
    )
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

  //todo: fix this bullshit
  private def vote(id: Long, mark: Int, contentType: String)
                  (voteOnItem: (Long, Long) => IO[Either[ServiceException, Long]])
                  (getPoints: Long => IO[Either[ServiceException, Long]])
                  (implicit request: AuthenticatedRequest[AnyContent]) = synchronized {
    val uId: Long = request.session.get(deadboltConfig.identifierKey).flatMap(i => Try(i.toLong).toOption).getOrElse(0)
    userService.getUserMark(id, uId, contentType).flatMap {
      case Right(m) =>
        m match {
          case Some(um) =>
            if (um == 0) {
              userService.updateUserMark(mark, id, uId, contentType).flatMap { _ => voteOnItem(uId, id) }
            } else if (um == mark) {
              getPoints(id)
            } else {
              userService.updateUserMark(0, id, uId, contentType).flatMap { _ => voteOnItem(uId, id) }
            }
          case None => userService.setUserMark(mark, id, uId, contentType).flatMap { _ => voteOnItem(uId, id) }
        }
      case Left(f) => IO.pure(Left(f))
    }.convert { memes =>
      Ok(memes.asJson.spaces2)
    }.unsafeToFuture()
  }

  def upVotePost(id: Long) = authAction { implicit request =>
    vote(id, 1, "meme")(memeService.upVoteMeme)(memeService.getMemePoints)
  }

  def downVotePost(id: Long) = authAction { implicit request =>
    vote(id, -1, "meme")(memeService.downVoteMeme)(memeService.getMemePoints)
  }

  def upVoteComment(id: Long) = authAction { implicit request =>
    vote(id, 1, "comment")(memeService.upVoteComment)(memeService.getCommentPoints)
  }

  def downVoteComment(id: Long) = authAction { implicit request =>
    vote(id, -1, "comment")(memeService.downVoteComment)(memeService.getCommentPoints)
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

  private def getTypeNumber(s: String): (String, Int) = {
    s.split("-").toList match {
      case t :: n :: Nil => (t, n.toInt)
      case _ => ("", 0)
    }
  }

  private def createFilesContent[A](files: Seq[play.api.mvc.MultipartFormData.FilePart[TemporaryFile]]) = {
    files.map { file =>
      val (_, number) = getTypeNumber(file.key)
      val encoded = Base64.getEncoder.encodeToString(Files.readAllBytes(Paths.get(file.ref.file.getAbsolutePath)))
      val filename = Paths.get(file.filename).getFileName.toString
      val cType = filename.substring(filename.lastIndexOf("."))
      Content(0, cType, encoded, number)
    }.toList
  }

  def createPostForm = authAction(parse.multipartFormData) { implicit request =>
    val uId = request.session.get(deadboltConfig.identifierKey).getOrElse("")
    val dataParts = request.body.dataParts

    val res = for {
      title <- dataParts.get("titleField").flatMap(_.headOption)
      data = dataParts - "titleField"

      filesContent = createFilesContent(request.body.files)
      dataContent = data.flatMap { case (k, v) =>
        val (key, number) = getTypeNumber(k)
        v.map(vv => Content(0, key, vv, number))
      }.toList

      meme = MemeItem(
        title,
        ZonedDateTime.now().withZoneSameInstant(ZoneId.of("UTC")).toLocalDateTime,
        filesContent ++ dataContent,
        0,
        uId.toLong
      )
    } yield {
      memeService.createMeme(meme).convert { _ =>
        Redirect(routes.FeedController.hottest(1))
      }.unsafeToFuture()
    }
    res.getOrElse(Future(BadRequest("Form is invalid")))
  }

  def createPost() = authAction { implicit request =>
    (for {
      uId <- getUser
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


object FeedController {
  val DEF_LIMIT = 25
}