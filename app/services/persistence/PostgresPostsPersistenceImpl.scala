package services.persistence
import models.{CommentItem, CommentItemWithId, MemeItem, MemeItemWithId}

class PostgresPostsPersistenceImpl(connector: PostgresConnector) extends PostsPersistence {

  override def createComment(commentItem: CommentItem): Result[CommentItemWithId] = ???

  override def createPost(feedItem: MemeItem): Result[MemeItemWithId] = ???

  override def deletePost(id: Long): Result[Unit] = ???

  override def upVotePost(id: Long): Result[Unit] = ???

  override def downVotePost(id: Long): Result[Unit] = ???

  override def deleteComment(id: Long): Result[Unit] = ???

  override def upVoteComment(id: Long): Result[Unit] = ???

  override def downVoteComment(id: Long): Result[Unit] = ???

  override def getMostPopular(forDays: Int, offset: PostsPersistence.FeedOffset): Result[List[MemeItemWithId]] = ???

  override def getPostWithComments(id: Long): Result[MemeItemWithId] = ???
}
