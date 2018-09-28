package services.persistence
import models.{CommentItem, CommentItemWithId, PostItem, PostItemWithId}

class PostgresPostsPersistenceImpl(connector: PostgresConnector) extends PostsPersistence {

  override def createComment(commentItem: CommentItem): Result[CommentItemWithId] = ???

  override def createPost(feedItem: PostItem): Result[PostItemWithId] = ???

  override def deletePost(id: Long): Result[Unit] = ???

  override def upVotePost(id: Long): Result[Unit] = ???

  override def downVotePost(id: Long): Result[Unit] = ???

  override def deleteComment(id: Long): Result[Unit] = ???

  override def upVoteComment(id: Long): Result[Unit] = ???

  override def downVoteComment(id: Long): Result[Unit] = ???

  override def getMostPopular(forDays: Int, offset: PostsPersistence.FeedOffset): Result[List[PostItemWithId]] = ???

  override def getPostWithComments(id: Long): Result[PostItemWithId] = ???
}
