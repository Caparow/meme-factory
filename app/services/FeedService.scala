package services

import models._
import services.persistence.PostsPersistence.FeedOffset

trait FeedService extends Result {

  def createPost(user: Long, feedItem: PostItem): Result[PostItemWithId]

  def createComment(user: Long, feedItem: PostItem): Result[CommentItemWithId]

  def deletePost(user: Long, feedItem: PostItem): Result[Unit]

  def upVotePost(user: Long, feedItem: PostItem): Result[Long]

  def downVotePost(user: Long, feedItem: PostItem): Result[Long]

  def upVoteComment(user: Long, feedItem: PostItem): Result[Long]

  def downVoteComment(user: Long, feedItem: PostItem): Result[Long]

  def getLatest(offset: FeedOffset = FeedOffset(0, 25)): Result[List[PostItemWithId]]

  def getMostPopular(forDays: Int = 1, offset: FeedOffset = FeedOffset(0, 25)): Result[List[PostItemWithId]]

  def getPostWithComments(id: Long): Result[PostItemWithId]
}

