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
      post <- getPostStmt(id).query[MemeItemWithoutContent].unique
      _ <- createContentStmt(memeItem.content).update.run
    } yield MemeItemWithId(id, memeItem.title, memeItem.timestamp, memeItem.content, memeItem.points, memeItem.author)

    connector.query(postQuery)
  }

  override def deletePost(id: Long): IO[Unit] = {
    connector.query(deletePostStmt(id).update.run.map(_ => ()))
  }

  override def upVotePost(id: Long): IO[Unit] = {
    val upVote = for {
      meme <- getPostStmt(id).query[MemeItemWithoutContent].unique
      newPoints = meme.points + 1
      updated <- updatePostStmt(meme.copy(points = newPoints)).update.run.map(_ => ())
    } yield updated
    connector.query(upVote)
  }

  override def downVotePost(id: Long): IO[Unit] = {
    val downVote = for {
      meme <- getPostStmt(id).query[MemeItemWithoutContent].unique
      newPoints = meme.points - 1
      updated <- updatePostStmt(meme.copy(points= newPoints)).update.run.map(_ => ())
    } yield updated
    connector.query(downVote)
  }

  override def deleteComment(id: Long): IO[Unit] = {
    connector.query(deleteCommentStmt(id).update.run.map(_ => ()))
  }

  override def upVoteComment(id: Long): IO[Unit] = {
    val upVote = for {
      comment <- getCommentStmt(id).query[CommentItemWithId].unique
      newPoints = comment.points + 1
      updated <- updateCommentStmt(comment.copy(points = newPoints)).update.run.map(_ => ())
    } yield updated
    connector.query(upVote)
  }

  override def downVoteComment(id: Long): IO[Unit] = {
    val downVote = for {
      comment <- getCommentStmt(id).query[CommentItemWithId].unique
      newPoints = comment.points - 1
      updated <- updateCommentStmt(comment.copy(points= newPoints)).update.run.map(_ => ())
    } yield updated
    connector.query(downVote)
  }

  override def getMostPopular(forDays: Int, offset: FeedOffset): IO[List[MemeItemWithId]] = {
    getMemeList(connector.query(getHottestPostsStmt(forDays, offset).query[MemeItemWithoutContent].to[List]))
  }

  private def getMemeList(f: => IO[List[MemeItemWithoutContent]]) = {
    f.flatMap { mm =>
      val ids = mm.map(_.id).toSet
      connector.query(getContentStmt(ids).query[Content].to[List]).map {
        _.groupBy(_.memeID).map { case (k,v) =>
          val m = mm.find(_.id == k).get
          import m._
          MemeItemWithId(id, title, timestamp, v, points, author)
        }.toList
      }
    }
  }

  override def getLatest(offset: FeedOffset): IO[List[MemeItemWithId]] = {
    getMemeList(connector.query(getLatestPostsStmt(offset).query[MemeItemWithoutContent].to[List]))
  }

  override def getPostWithComments(id: Long): IO[MemeItemWithComments] = {
    for {
      post <- getMemeList(connector.query(getPostStmt(id).query[MemeItemWithoutContent].to[List]))
      comments <- connector.query(getCommentsStmt(id).query[CommentItemWithId].to[List])
    } yield {
      MemeItemWithComments(post.head, comments)
    }
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
    import commentItem._
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

  def updatePostStmt(memeItem: MemeItemWithoutContent): Fragment = {
    import memeItem._
    sql"""
         |update memes set
         |title = $title,
         |points = $points,
         |where id = ${memeItem.id};
       """.stripMargin
  }

  def createContentStmt(contentList: List[Content]): Fragment = {
    def instantiate (content: Content): String = {
      s"(${content.memeID}, ${content.contentType}, ${content.content}, ${content.num})"
    }

    val v = contentList.map(instantiate).mkString(",\n")
    sql"""
         |insert into content(meme_id,content_type,content,number) values
         |$v
         |;
       """.stripMargin
  }

  def getContentStmt(ids: Set[Long]): Fragment = {
    val s = ids.toString().replace("Set", "")
    sql"""
         |select meme_id,content_type,content,num
         |from content
         |where id in $s
         |;
       """.stripMargin
  }
}
