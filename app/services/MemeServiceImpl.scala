package services

import com.google.inject.{Inject, Singleton}
import models._
import services.persistence.PostsPersistence
import services.persistence.PostsPersistence.FeedOffset

@Singleton
class MemeServiceImpl @Inject()(
                                 postsPersistence: PostsPersistence
                               ) extends MemeService with ResultExt {
  override def createMeme(feedItem: MemeItem): Result[MemeItemWithId] = {
    postsPersistence.createPost(feedItem).succ
  }

  override def createComment(commentItem: CommentItem): Result[CommentItemWithId] = {
    postsPersistence.createComment(commentItem).succ
  }

  override def deleteMeme(id: Long): Result[Unit] = {
    postsPersistence.deletePost(id).succ
  }

  override def upVoteMeme(user: Long, id: Long): Result[Long] = {
    postsPersistence.upVotePost(id).succ
  }

  override def downVoteMeme(user: Long, id: Long): Result[Long] = {
    postsPersistence.downVotePost(id).succ
  }

  override def upVoteComment(user: Long, id: Long): Result[Long] = {
    postsPersistence.upVoteComment(id).succ
  }

  override def downVoteComment(user: Long, id: Long): Result[Long] = {
    postsPersistence.downVoteComment(id).succ
  }

  override def getLatest(offset: FeedOffset): Result[List[MemeItemWithId]] = {
    postsPersistence.getLatest(offset).succ
  }

  override def getMostPopular(forDays: Int, offset: FeedOffset): Result[List[MemeItemWithId]] = {
    postsPersistence.getMostPopular(forDays, offset).succ
  }

  override def getPostWithComments(id: Long): Result[MemeItemWithComments] = {
    postsPersistence.getPostWithComments(id).succ
  }
}