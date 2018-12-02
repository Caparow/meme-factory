package services.persistence

import cats.effect.IO
import models._
import services.persistence.PostsPersistence.FeedOffset

trait PostsPersistence {

  def createPost(feedItem: MemeItem): IO[MemeItemWithId]

  def deletePost(id: Long): IO[Unit]

  def upVotePost(id: Long): IO[Long]

  def downVotePost(id: Long): IO[Long]

  def createComment(commentItem: CommentItem): IO[CommentItemWithId]

  def deleteComment(id: Long): IO[Unit]

  def upVoteComment(id: Long): IO[Long]

  def downVoteComment(id: Long): IO[Long]

  def getPostWithComments(id: Long): IO[MemeItemWithComments]

  def getContent(memeId: Long, num: Long): IO[Option[Content]]

  def getMemePoints(id: Long): IO[Long]

  def getCommentPoints(id: Long): IO[Long]

  def searchTitles(target: String, offset: FeedOffset): IO[List[MemeItemWithId]]

  def getMostPopular(forDays: Int, offset: FeedOffset): IO[List[MemeItemWithId]]

  def getLatest(offset: FeedOffset): IO[List[MemeItemWithId]]

  def countSearchTitles(target: String): IO[Int]

  def countMostPopular(forDays: Int): IO[Int]

  def countLatest(): IO[Int]
}

object PostsPersistence {

  case class FeedOffset(offset: Long, limit: Long)

}