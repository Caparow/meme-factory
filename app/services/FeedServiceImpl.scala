package services
import models.{CommentItemWithId, PostItem, PostItemWithId}
import services.persistence.PostsPersistence.FeedOffset

class FeedServiceImpl extends FeedService {
  override def createPost(user: Long, feedItem: PostItem): Result[PostItemWithId] = ???

  override def createComment(user: Long, feedItem: PostItem): Result[CommentItemWithId] = ???

  override def deletePost(user: Long, feedItem: PostItem): Result[Unit] = ???

  override def upVotePost(user: Long, feedItem: PostItem): Result[Long] = ???

  override def downVotePost(user: Long, feedItem: PostItem): Result[Long] = ???

  override def upVoteComment(user: Long, feedItem: PostItem): Result[Long] = ???

  override def downVoteComment(user: Long, feedItem: PostItem): Result[Long] = ???

  override def getLatest(offset: FeedOffset): Result[List[PostItemWithId]] = ???

  override def getMostPopular(forDays: Int, offset: FeedOffset): Result[List[PostItemWithId]] = ???

  override def getPostWithComments(id: Long): Result[PostItemWithId] = ???
}
