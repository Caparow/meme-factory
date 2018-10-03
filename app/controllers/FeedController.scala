package controllers

import be.objectify.deadbolt.scala.{ActionBuilders, AuthenticatedRequest}
import com.google.inject.{Inject, Singleton}
import configs.DeadboltConfig
import play.api.libs.json._
import models.auth.AdminRole
import models.{CommentItem, MemeItem}
import play.api.libs.ws.WSClient
import play.api.libs.ws._
import play.api.mvc.BodyParsers
import play.api.mvc.{AbstractController, ControllerComponents}
import services.persistence.PostsPersistence.FeedOffset

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class FeedController @Inject()(
                                actionBuilders: ActionBuilders
                                , ws: WSClient
                                , deadboltConfig: DeadboltConfig
                                , cc: ControllerComponents
                              )(implicit ec: ExecutionContext) extends AbstractController(cc) {

  private implicit val p = cc.parsers

  private def authAction = actionBuilders.RestrictAction(AdminRole.name).defaultHandler()

  def hottest(feedOffset: FeedOffset = FeedOffset(0, 25)) = authAction {
    Future(Ok(views.html.index("Your new application is ready.")))
  }

  def latest(feedOffset: FeedOffset = FeedOffset(0, 25)) = Action {
    Ok(views.html.index("Your new application is ready."))
  }

  def upVotePost(id: Long) = Action {
    Ok(views.html.index("Your new application is ready."))
  }

  def downVotePost(id: Long) = Action {
    Ok(views.html.index("Your new application is ready."))
  }

  def upVoteComment(id: Long) = Action {
    Ok(views.html.index("Your new application is ready."))
  }

  def downVoteComment(id: Long) = Action {
    Ok(views.html.index("Your new application is ready."))
  }

  def createCommentForm(commentItem: CommentItem) = Action {
    Ok(views.html.index("Your new application is ready."))
  }

  def createComment(commentItem: CommentItem) = Action {
    Ok(views.html.index("Your new application is ready."))
  }

  def createPostForm = Action {
    Ok(views.html.index("Your new application is ready."))
  }

  def createPost(postItem: MemeItem) = Action {
    Ok(views.html.index("Your new application is ready."))
  }

  def deleteComment(id: Long) = Action {
    Ok(views.html.index("Your new application is ready."))
  }

  def deletePost(id: Long) = Action {
    Ok(views.html.index("Your new application is ready."))
  }
}
