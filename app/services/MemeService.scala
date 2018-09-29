package services

import models._
import services.persistence.PostsPersistence.FeedOffset

trait MemeService extends Result {

  def createMeme(user: Long, feedItem: MemeItem): Result[MemeItemWithId]

  def createComment(user: Long, feedItem: MemeItem): Result[CommentItemWithId]

  def deleteMeme(user: Long, feedItem: MemeItem): Result[Unit]

  def upVoteMeme(user: Long, feedItem: MemeItem): Result[Long]

  def downVoteMeme(user: Long, feedItem: MemeItem): Result[Long]

  def upVoteComment(user: Long, feedItem: MemeItem): Result[Long]

  def downVoteComment(user: Long, feedItem: MemeItem): Result[Long]

  def getLatest(offset: FeedOffset = FeedOffset(0, 25)): Result[List[MemeItemWithId]]

  def getMostPopular(forDays: Int = 1, offset: FeedOffset = FeedOffset(0, 25)): Result[List[MemeItemWithId]]

  def getPostWithComments(id: Long): Result[MemeItemWithId]
}

