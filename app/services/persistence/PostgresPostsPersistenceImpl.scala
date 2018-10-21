package services.persistence

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

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
      id <- fr"select lastval()".query[Long].unique
      comment <- getCommentStmt(id).query[CommentItemWithId].unique
    } yield comment

    connector.query(commentQuery)
  }

  override def createPost(memeItem: MemeItem): IO[MemeItemWithId] = {
    val postQuery = for {
      _ <- createPostStmt(memeItem).update.run
      id <- fr"select lastval()".query[Long].unique
      _ <- getPostStmt(id).query[MemeItemWithoutContent].unique
      newContent = memeItem.content.map(_.copy(memeID = id))
      _ <- {
        if (newContent.nonEmpty)
          createContentStmt(newContent).update.run
        else fr"select 1".query[Long].unique
      }
    } yield MemeItemWithId(id, memeItem.title, memeItem.timestamp, newContent, memeItem.points, memeItem.author, "")

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
          .map(c => MemeItemWithId(meme.id, meme.title, meme.timestamp, c, meme.points, meme.author, meme.login))
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

  override def getContent(memeId: Long, num: Long): IO[Option[Content]] = {
    connector.query(getContentStmt(memeId, num).query[Content].option)
  }
}

object PostgresPostsPersistenceImpl {

  implicit val commentWithIdToComposite: Composite[CommentItemWithId] =
    Composite[(Long, Long, String, String, Long, Long, String)].imap(
      (t: (Long, Long, String, String, Long, Long, String)) =>
        CommentItemWithId(t._1, t._2, t._3, LocalDateTime.parse(t._4.replace(" ", "T"), DateTimeFormatter.ISO_DATE_TIME), t._5, t._6, t._7))(
      (p: CommentItemWithId) => {
        import p._
        (id, memeId, comment, timestamp.format(DateTimeFormatter.ISO_DATE_TIME), points, author, login)
      }
    )

  implicit val commentToComposite: Composite[CommentItem] =
    Composite[(Long, String, String, Long, Long)].imap(
      (t: (Long, String, String, Long, Long)) =>
        CommentItem(t._1, t._2, LocalDateTime.parse(t._3.replace(" ", "T"), DateTimeFormatter.ISO_DATE_TIME), t._4, t._5))(
      (p: CommentItem) => {
        import p._
        (memeId, comment, timestamp.format(DateTimeFormatter.ISO_DATE_TIME), points, author)
      }
    )

  implicit val memeToComposite: Composite[MemeItemWithoutContent] =
    Composite[(Long, String, String, Long, Long, String)].imap(
      (t: (Long, String, String, Long, Long, String)) =>
        MemeItemWithoutContent(t._1, t._2, LocalDateTime.parse(t._3.replace(" ", "T"), DateTimeFormatter.ISO_DATE_TIME), t._4, t._5, t._6))(
      (p: MemeItemWithoutContent) => {
        import p._
        (id, title, timestamp.format(DateTimeFormatter.ISO_DATE_TIME), points, author, login)
      }
    )

  def getContentStmt(memeId: Long, num: Long): Fragment = {
    fr"""select meme_id,content_type,content,num from content where meme_id = $memeId and num = $num;"""
  }

  def getCommentStmt(id: Long): Fragment = {
    fr"""select comments.id, meme_id, comment, added_at, points, author, users.login
        |from comments
        |join users on comments.author = users.id
        |where comments.id = $id;""".stripMargin
  }

  def deleteCommentStmt(id: Long): Fragment = {
    fr"""delete from comments where id = $id;"""
  }

  def getCommentsStmt(id: Long): Fragment = {
    fr"""select comments.id, meme_id, comment, added_at, points, author, users.login
        |from comments
        |join users on comments.author = users.id
        |where meme_id = $id;""".stripMargin
  }

  def createCommentStmt(commentItem: CommentItem): Fragment = {
    import commentItem._
    Fragment.const(
      s"""
         |insert into comments(meme_id, comment, added_at, points, author) values
         |($memeId, '$comment', '${timestamp.format(DateTimeFormatter.ISO_DATE_TIME)}', $points, $author);
       """.stripMargin)
  }

  def updateCommentStmt(commentItem: CommentItemWithId): Fragment = {
    import commentItem._
    Fragment.const(
      s"""
         |update comments set
         |points = $points
         |where id = ${commentItem.id};
       """.stripMargin)
  }

  def createPostStmt(memeItem: MemeItem): Fragment = {
    import memeItem._
    Fragment.const(
      s"""
         |insert into memes(title, added_at, points, author) values
         |('$title', '${timestamp.format(DateTimeFormatter.ISO_DATE_TIME)}', $points, $author);
       """.stripMargin)
  }

  def getPostStmt(id: Long): Fragment = {
    fr"""select memes.id, title, added_at, points, author, users.login
        |from memes
        |join users on memes.author = users.id
        |where memes.id = $id;""".stripMargin
  }

  def getHottestPostsStmt(forDays: Int, offset: FeedOffset): Fragment = {
    Fragment.const(
      s"""select memes.id, title, added_at, points, author, users.login
         |from memes
         |join users on memes.author = users.id
         |where now() - added_at < '$forDays days'::interval
         |order by points
         |limit ${offset.limit} offset ${offset.offset};""".stripMargin)
  }

  def getLatestPostsStmt(offset: FeedOffset): Fragment = {
    fr"""select memes.id, title, added_at, points, author, users.login
        |from memes
        |join users on memes.author = users.id
        |order by added_at
        |limit ${offset.limit} offset ${offset.offset};""".stripMargin
  }

  def deletePostStmt(id: Long): Fragment = {
    fr"""delete from memes where id = $id;"""
  }

  def updatePostStmt(memeItem: MemeItemWithoutContent): Fragment = {
    import memeItem._
    Fragment.const(
      s"""
         |update memes set
         |title = '$title',
         |points = $points
         |where id = ${memeItem.id};
       """.stripMargin)
  }

  def createContentStmt(contentList: List[Content]): Fragment = {
    def instantiate(content: Content): String = {
      s"(${content.memeID}, '${content.contentType}', '${content.content}', ${content.num})"
    }

    val v = contentList.map(instantiate).mkString(",") ++ ";"
    fr"""insert into content(meme_id,content_type,content,num) values""" ++
      Fragment.const(v)
  }

  def getContentStmt(id: Long): Fragment = {
    fr"""
        |select meme_id,content_type,content,num
        |from content
        |where meme_id = $id
        |;
       """.stripMargin
  }
}
