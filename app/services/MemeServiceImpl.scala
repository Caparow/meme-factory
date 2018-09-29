package services
import models.{CommentItemWithId, MemeItem, MemeItemWithId}
import services.persistence.PostsPersistence.FeedOffset

class MemeServiceImpl extends MemeService {
  override def createMeme(user: Long, feedItem: MemeItem): Result[MemeItemWithId] = ???

  override def createComment(user: Long, feedItem: MemeItem): Result[CommentItemWithId] = ???

  override def deleteMeme(user: Long, feedItem: MemeItem): Result[Unit] = ???

  override def upVoteMeme(user: Long, feedItem: MemeItem): Result[Long] = ???

  override def downVoteMeme(user: Long, feedItem: MemeItem): Result[Long] = ???

  override def upVoteComment(user: Long, feedItem: MemeItem): Result[Long] = ???

  override def downVoteComment(user: Long, feedItem: MemeItem): Result[Long] = ???

  override def getLatest(offset: FeedOffset): Result[List[MemeItemWithId]] = ???

  override def getMostPopular(forDays: Int, offset: FeedOffset): Result[List[MemeItemWithId]] = ???

  override def getPostWithComments(id: Long): Result[MemeItemWithId] = ???
}
