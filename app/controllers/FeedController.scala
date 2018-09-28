package controllers

import com.google.inject.Inject
import models.{CommentItem, PostItem}
import play.api.mvc.{AbstractController, ControllerComponents}
import services.persistence.PostsPersistence.FeedOffset

class FeedController @Inject()(cc: ControllerComponents) extends AbstractController(cc) {

  def hottest(feedOffset: FeedOffset = FeedOffset(0, 25)) = Action {
    Ok(views.html.index("Your new application is ready."))
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

  def createPost(postItem: PostItem) = Action {
    Ok(views.html.index("Your new application is ready."))
  }

  def deleteComment(id: Long) = Action {
    Ok(views.html.index("Your new application is ready."))
  }

  def deletePost(id: Long) = Action {
    Ok(views.html.index("Your new application is ready."))
  }
}
