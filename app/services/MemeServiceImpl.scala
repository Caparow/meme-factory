package services

import com.google.inject.{Inject, Singleton}
import models._
import services.persistence.PostsPersistence
import services.persistence.PostsPersistence.FeedOffset

@Singleton
class MemeServiceImpl @Inject()(
                                 postsPersistence: PostsPersistence
                               ) extends MemeService with ResultExt {


  override def getMemePoints(id: Long): Result[Long] = {
    postsPersistence.getMemePoints(id).succ
  }

  override def getCommentPoints(id: Long): Result[Long] = {
    postsPersistence.getCommentPoints(id).succ
  }

  override def getContent(memeId: Long, num: Long): Result[Content] = {
    postsPersistence.getContent(memeId, num).toRes("Image not found.")
  }

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

  override def getTargetPosts(target: String, feedOffset: FeedOffset): Result[List[MemeItemWithId]] = {
    postsPersistence.searchTitles(target, feedOffset).succ
  }

  override def countSearchTitles(target: String): Result[Int] = {
    postsPersistence.countSearchTitles(target).succ
  }

  override def countMostPopular(forDays: Int): Result[Int] = {
    postsPersistence.countMostPopular(forDays).succ
  }

  override def countLatest(): Result[Int] = {
    postsPersistence.countLatest().succ
  }
}
