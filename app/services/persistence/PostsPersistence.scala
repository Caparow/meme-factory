package services.persistence

import cats.effect.IO
import models._
import services.persistence.PostsPersistence.FeedOffset

trait PostsPersistence {

  def createPost(feedItem: MemeItem): IO[MemeItemWithId]

  def deletePost(id: Long): IO[Unit]

  def upVotePost(id: Long): IO[Unit]

  def downVotePost(id: Long): IO[Unit]

  def createComment(commentItem: CommentItem): IO[CommentItemWithId]

  def deleteComment(id: Long): IO[Unit]

  def upVoteComment(id: Long): IO[Unit]

  def downVoteComment(id: Long): IO[Unit]

  def getMostPopular(forDays: Int, offset: FeedOffset): IO[List[MemeItemWithId]]

  def getLatest(offset: FeedOffset): IO[List[MemeItemWithId]]

  def getPostWithComments(id: Long): IO[MemeItemWithComments]
}

object PostsPersistence {

  case class FeedOffset(offset: Long, limit: Long)

}