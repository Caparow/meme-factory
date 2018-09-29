package services.persistence

import cats.effect.IO
import doobie._
import doobie.implicits._
import models._
import services.persistence.PostgresPostsPersistenceImpl._
import services.persistence.PostsPersistence.FeedOffset

class PostgresPostsPersistenceImpl(connector: PostgresConnector) extends PostsPersistence {

  override def createComment(commentItem: CommentItem): IO[CommentItemWithId] = {
    val commentQuery = for {
      _ <- createCommentStmt(commentItem).update.run
      id <- sql"select lastval()".query[Long].unique
      comment <- getCommentStmt(id).query[CommentItemWithId].unique
    } yield comment

    connector.query(commentQuery)
  }

  override def createPost(memeItem: MemeItem): IO[MemeItemWithId] = {
    val postQuery = for {
      _ <- createPostStmt(memeItem).update.run
      id <- sql"select lastval()".query[Long].unique
      post <- getPostStmt(id).query[MemeItemWithId].unique
      _ <- createContentStmt(post).update.run
    } yield post

    connector.query(postQuery)
  }

  override def deletePost(id: Long): IO[Unit] = {
    connector.query(deletePostStmt(id).update.run.map(_ => Unit))
  }

  override def upVotePost(id: Long): IO[Unit] = {
    val upVote = for {
      meme <- getPostStmt(id).query[MemeItemWithId].unique
      newPoints = meme.memeItem.points + 1
      updated <- updatePostStmt(meme.copy(memeItem = meme.memeItem.copy(points = newPoints)))
    } yield updated
    connector.query(upVote)
  }

  override def downVotePost(id: Long): IO[Unit] = {
    val downVote = for {
      meme <- getPostStmt(id).query[MemeItemWithId].unique
      newPoints = meme.memeItem.points - 1
      updated <- updatePostStmt(meme.copy(memeItem = meme.memeItem.copy(points = newPoints)))
    } yield updated
    connector.query(downVote)
  }

  override def deleteComment(id: Long): IO[Unit] = {
    connector.query(deleteCommentStmt(id).update.run.map(_ => Unit))
  }

  override def upVoteComment(id: Long): IO[Unit] = {
    val upVote = for {
      comment <- getCommentStmt(id).query[CommentItemWithId].unique
      newPoints = comment.commentItem.points + 1
      updated <- updateCommentStmt(comment.copy(commentItem = comment.commentItem.copy(points = newPoints)))
    } yield updated
    connector.query(upVote)
  }

  override def downVoteComment(id: Long): IO[Unit] = {
    val downVote = for {
      comment <- getCommentStmt(id).query[CommentItemWithId].unique
      newPoints = comment.commentItem.points - 1
      updated <- updateCommentStmt(comment.copy(commentItem = comment.commentItem.copy(points = newPoints)))
    } yield updated
    connector.query(downVote)
  }

  override def getMostPopular(forDays: Int, offset: FeedOffset): IO[List[MemeItemWithId]] = {
    connector.query(getHottestPostsStmt(forDays, offset).query[MemeItemWithId].to[List])
  }

  override def getLatest(offset: FeedOffset): IO[List[MemeItemWithId]] = {
    connector.query(getLatestPostsStmt(offset).query[MemeItemWithId].to[List])
  }

  override def getPostWithComments(id: Long): IO[MemeItemWithComments] = {
    for {
      post <- getPostStmt(id).query[MemeItemWithId]
      comments <- getCommentsStmt(id).query[List[CommentItemWithId]]
    } yield MemeItemWithComments(post, comments)
  }
}

object PostgresPostsPersistenceImpl {
  def getCommentStmt(id: Long): Fragment = {
    sql"""select id, meme_id, comment, points, added_at, author from comments where id = $id;"""
  }

  def deleteCommentStmt(id: Long): Fragment = {
    sql"""delete from comments where id = $id;"""
  }

  def getCommentsStmt(id: Long): Fragment = {
    sql"""select id, meme_id, comment, points, added_at, author from comments where meme_d = $id;"""
  }

  def createCommentStmt(commentItem: CommentItem): Fragment = {
    import commentItem._
    sql"""
         |insert into comments(meme_id, comment, points, added_at, author) values
         |($memeId, $comment, $points, $timestamp, $author)
         |returning id;
       """.stripMargin
  }

  def updateCommentStmt(commentItem: CommentItemWithId): Fragment = {
    import commentItem.commentItem._
    sql"""
         |update comments set
         |points = $points,
         |where id = ${commentItem.id};
       """.stripMargin
  }

  def createPostStmt(memeItem: MemeItem): Fragment = {
    import memeItem._
    sql"""
         |insert into memes(title, added_at, points, author) values
         |($title, $timestamp, $points, $author)
         |returning id;
       """.stripMargin
  }

  def getPostStmt(id: Long): Fragment = {
    sql"""select id, title, added_at, points, author from memes where id = $id;"""
  }

  def getHottestPostsStmt(forDays: Int, offset: PostsPersistence.FeedOffset): Fragment = {
    sql"""select id, title, added_at, points, author from memes
         |where now() - added_at < '1 day'::interval
         |order by points
         |limit ${offset.limit} offset ${offset.offset};""".stripMargin
  }

  def getLatestPostsStmt(offset: FeedOffset): Fragment = {
    sql"""select id, title, added_at, points, author from memes
         |order by added_at
         |limit ${offset.limit} offset ${offset.offset};""".stripMargin
  }

  def deletePostStmt(id: Long): Fragment = {
    sql"""delete from memes where id = $id;"""
  }

  def updatePostStmt(memeItem: MemeItemWithId): Fragment = {
    import memeItem.memeItem._
    sql"""
         |update memes set
         |title = $title,
         |points = $points,
         |where id = ${memeItem.id};
       """.stripMargin
  }

  def createContentStmt(memeItem: MemeItemWithId): Fragment = {
    def instantiate (content: Content, num: Int = 1): String = {
      import content._
      s"(${memeItem.id},$contentType,$content,$num)"
    }
    val v = memeItem.memeItem.content.zipWithIndex.map{case (c, i) => instantiate(c, i)}.mkString(",\n")
    sql"""
         |insert into content(meme_id,content_type,content,number) values
         |$v
         |;
       """.stripMargin
  }
}
