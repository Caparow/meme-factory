package services.persistence

import cats.data.NonEmptyList
import cats.effect.IO
import com.google.inject.{Inject, Singleton}
import doobie._
import doobie.implicits._
import models._
import services.persistence.PostgresPostsPersistenceImpl._
import services.persistence.PostsPersistence.FeedOffset

@Singleton
class PostgresPostsPersistenceImpl @Inject()(connector: PostgresConnector) extends PostsPersistence {

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
//      _ <- createContentStmt(memeItem.content).update.run
    } yield MemeItemWithId(id, memeItem.title, memeItem.timestamp, memeItem.content, memeItem.points, memeItem.author)

    connector.query(postQuery)
  }

  override def deletePost(id: Long): IO[Unit] = {
    connector.query(deletePostStmt(id).update.run.map(_ => ()))
  }

  override def upVotePost(id: Long): IO[Long] = {
    val upVote = for {
      meme <- getPostStmt(id).query[MemeItemWithoutContent].unique
      newPoints = meme.points + 1
      _ <- updatePostStmt(meme.copy(points = newPoints)).update.run.map(_ => ())
    } yield newPoints
    connector.query(upVote)
  }

  override def downVotePost(id: Long): IO[Long] = {
    val downVote = for {
      meme <- getPostStmt(id).query[MemeItemWithoutContent].unique
      newPoints = meme.points - 1
      _ <- updatePostStmt(meme.copy(points = newPoints)).update.run.map(_ => ())
    } yield newPoints
    connector.query(downVote)
  }

  override def deleteComment(id: Long): IO[Unit] = {
    connector.query(deleteCommentStmt(id).update.run.map(_ => ()))
  }

  override def upVoteComment(id: Long): IO[Long] = {
    val upVote = for {
      comment <- getCommentStmt(id).query[CommentItemWithId].unique
      newPoints = comment.points + 1
      _ <- updateCommentStmt(comment.copy(points = newPoints)).update.run.map(_ => ())
    } yield newPoints
    connector.query(upVote)
  }

  override def downVoteComment(id: Long): IO[Long] = {
    val downVote = for {
      comment <- getCommentStmt(id).query[CommentItemWithId].unique
      newPoints = comment.points - 1
      _ <- updateCommentStmt(comment.copy(points = newPoints)).update.run.map(_ => ())
    } yield newPoints
    connector.query(downVote)
  }

  override def getMostPopular(forDays: Int, offset: FeedOffset): IO[List[MemeItemWithId]] = {
    getMemeList(connector.query(getHottestPostsStmt(forDays, offset).query[MemeItemWithoutContent].to[List]))
  }

  private def getMemeList(f: => IO[List[MemeItemWithoutContent]]) = {
    f.flatMap { mm =>
      mm.map { meme =>
        connector.query(getContentStmt(meme.id).query[Content].to[List])
          .map(c => MemeItemWithId(meme.id, meme.title, meme.timestamp, c, meme.points, meme.author))
      }.foldLeft(IO.pure(List.empty[MemeItemWithId])) {
        case (acc, v) => acc.flatMap(ii => v.map(List(_) ++ ii))
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
         |($title, $timestamp, $points, $author);
       """.stripMargin
  }

  def getPostStmt(id: Long): Fragment = {
    sql"""select id, title, added_at, points, author from memes where id = $id;"""
  }

  def getHottestPostsStmt(forDays: Int, offset: FeedOffset): Fragment = {
//    todo: add timestamp
//    where now() - added_at < '1 day'::interval
    sql"""select id, title, added_at, points, author from memes
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
    def instantiate(content: Content): String = {
      s"(${content.memeID}, ${content.contentType}, ${content.content}, ${content.num})"
    }

    val v = contentList.map(instantiate).mkString(",\n")
    sql"""
         |insert into content(meme_id,content_type,content,number) values
         |$v
         |;
       """.stripMargin
  }

  def getContentStmt(id: Long): Fragment = {
    sql"""
         |select meme_id,content_type,content,num
         |from content
         |where meme_id = $id
         |;
       """.stripMargin
  }
}
