package controllers

import java.io.{ByteArrayInputStream, File}
import java.nio.file.{Files, Paths}
import java.sql.Blob
import java.time.{ZoneId, ZonedDateTime}
import java.util.Base64

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{FileIO, Source}
import akka.util.ByteString
import be.objectify.deadbolt.scala.{ActionBuilders, AuthenticatedRequest}
import com.google.inject.{Inject, Singleton}
import configs.DeadboltConfig
import models.auth.{Role, UserRole}
import models._
import play.api.libs.ws.WSClient
import play.api.mvc.{AbstractController, ControllerComponents, MultipartFormData}
import services.MemeService
import services.persistence.PostsPersistence.FeedOffset
import io.circe.syntax._
import javax.imageio.ImageIO
import play.api.mvc.MultipartFormData.DataPart

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

@Singleton
class FeedController @Inject()(
                                memeService: MemeService
                                , actionBuilders: ActionBuilders
                                , ws: WSClient
                                , deadboltConfig: DeadboltConfig
                                , cc: ControllerComponents
                              )(implicit ec: ExecutionContext) extends AbstractController(cc) with ResultExt {

  private implicit val p = cc.parsers

  implicit val system = ActorSystem("QuickStart")
  implicit val materializer = ActorMaterializer()

  private def authAction = actionBuilders.RestrictAction(UserRole.name).defaultHandler()

  def hottest(forDays: Int = 1, feedOffset: FeedOffset = FeedOffset(0, 25)) = Action.async {
    import MemeItem._
    memeService.getMostPopular(forDays, feedOffset).convert { memes =>
      Ok(views.html.feed(memes))
    }.unsafeToFuture()
  }

  def latest(feedOffset: FeedOffset = FeedOffset(0, 25)) = Action.async {
    import MemeItem._
    memeService.getLatest(feedOffset).convert { memes =>
      Ok(views.html.index(memes.asJson.spaces2))
    }.unsafeToFuture()
  }

  def image(memeId: Long, num: Long) = Action.async {
    memeService.getContent(memeId, num).convert { content =>
      val bytes = Base64.getDecoder.decode(content.content)
      val tempFile = File.createTempFile("image", content.contentType)
      val path = Paths.get(tempFile.getAbsolutePath)
      Files.write(path, bytes)
      Ok.sendFile(tempFile)
    }.unsafeToFuture()
  }

  def post(id: Long) = authAction {
    import MemeItem._
    memeService.getPostWithComments(id).convert { meme =>
      Ok(views.html.index(meme.asJson.spaces2))
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

  def createCommentForm(commentItem: CommentItem) = authAction {
    Future(Ok(views.html.index("Your new application is ready.")))
  }

  def createComment(commentItem: CommentItem) = authAction {
    Future(Ok(views.html.index("Your new application is ready.")))
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

  def createPost() = authAction {
    Future(Ok(views.html.create_meme()))
  }

  def deleteComment(id: Long) = authAction {
    Future(Ok(views.html.index("Your new application is ready.")))
  }

  def deletePost(id: Long) = authAction {
    Future(Ok(views.html.index("Your new application is ready.")))
  }

}
