package services

import models._
import services.persistence.PostsPersistence.FeedOffset

trait MemeService extends Result {

  def createMeme(feedItem: MemeItem): Result[MemeItemWithId]

  def createComment(commentItem: CommentItem): Result[CommentItemWithId]

  def deleteMeme(id: Long): Result[Unit]

  def upVoteMeme(user: Long, id: Long): Result[Long]

  def downVoteMeme(user: Long, id: Long): Result[Long]

  def upVoteComment(user: Long, id: Long): Result[Long]

  def downVoteComment(user: Long, id: Long): Result[Long]

  def getLatest(offset: FeedOffset = FeedOffset(0, 25)): Result[List[MemeItemWithId]]

  def getMostPopular(forDays: Int = 1, offset: FeedOffset = FeedOffset(0, 25)): Result[List[MemeItemWithId]]

  def getPostWithComments(id: Long): Result[MemeItemWithComments]

  def getContent(memeId: Long, num: Long): Result[Content]

  def getMemePoints(id: Long): Result[Long]

  def getCommentPoints(id: Long): Result[Long]

  def getTargetPosts(target: String, feedOffset: FeedOffset): Result[List[MemeItemWithId]]
}

