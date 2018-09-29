package services.persistence

import models._
import services.persistence.PostsPersistence.FeedOffset

trait PostsPersistence extends Result {

  def createPost(feedItem: MemeItem): Result[MemeItemWithId]

  def deletePost(id: Long): Result[Unit]

  def upVotePost(id: Long): Result[Unit]

  def downVotePost(id: Long): Result[Unit]

  def createComment(commentItem: CommentItem): Result[CommentItemWithId]

  def deleteComment(id: Long): Result[Unit]

  def upVoteComment(id: Long): Result[Unit]

  def downVoteComment(id: Long): Result[Unit]

  def getMostPopular(forDays: Int, offset: FeedOffset): Result[List[MemeItemWithId]] = ???

  def getPostWithComments(id: Long): Result[MemeItemWithId] = ???
}

object PostsPersistence {

  case class FeedOffset(offset: Long, limit: Long)

}