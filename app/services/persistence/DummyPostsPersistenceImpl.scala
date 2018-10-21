package services.persistence

import cats.effect.IO
import models._
import services.persistence.PostsPersistence.FeedOffset

import scala.collection.mutable
import scala.util.Random

class DummyPostsPersistenceImpl(
                                 usersPersistence: UsersPersistence
                               ) extends PostsPersistence {
  val memes: mutable.HashMap[Long, MemeItem] = mutable.HashMap.empty[Long, MemeItem]
  val comments: mutable.HashMap[Long, CommentItem] = mutable.HashMap.empty[Long, CommentItem]

  override def createPost(feedItem: MemeItem): IO[MemeItemWithId] = synchronized {
    val id = Random.nextInt()
    memes.put(id, feedItem)
    import feedItem._
    IO.pure(MemeItemWithId(id, title, timestamp, content, points, author,
      usersPersistence.get(author).unsafeRunSync().map(_.login).getOrElse("")))
  }

  override def deletePost(id: Long): IO[Unit] = synchronized {
    IO.pure(memes.remove(id))
  }


  override def upVotePost(id: Long): IO[Long] = synchronized {
    IO.pure {
      val m = memes(id)
      memes.put(id, m.copy(points = m.points + 1))
      m.points + 1
    }
  }


  override def downVotePost(id: Long): IO[Long] = synchronized {
    IO.pure {
      val m = memes(id)
      memes.put(id, m.copy(points = m.points - 1))
      m.points - 1
    }
  }


  override def createComment(commentItem: CommentItem): IO[CommentItemWithId] = synchronized {
    val id = Random.nextInt()
    comments.put(id, commentItem)
    import commentItem._
    IO.pure(CommentItemWithId(id, memeId, comment, timestamp, points, author,
      usersPersistence.get(author).unsafeRunSync().map(_.login).getOrElse("")))
  }


  override def deleteComment(id: Long): IO[Unit] = synchronized {
    IO.pure(comments.remove(id))
  }


  override def upVoteComment(id: Long): IO[Long] = synchronized {
    IO.pure {
      val m = comments(id)
      comments.put(id, m.copy(points = m.points + 1))
      m.points + 1
    }
  }


  override def downVoteComment(id: Long): IO[Long] = synchronized {
    IO.pure {
      val m = comments(id)
      comments.put(id, m.copy(points = m.points - 1))
      m.points - 1
    }
  }


  override def getMostPopular(forDays: Int, offset: FeedOffset): IO[List[MemeItemWithId]] = synchronized {
    IO.pure {
      memes.map { case (k, v) =>
        import v._
        MemeItemWithId(k, title, timestamp, content, points, author, usersPersistence.get(author).unsafeRunSync().map(_.login).getOrElse(""))
      }.toList

    }
  }


  override def getLatest(offset: FeedOffset): IO[List[MemeItemWithId]] = synchronized {
    IO.pure {
      memes.map { case (k, v) =>
        import v._
        MemeItemWithId(k, title, timestamp, content, points, author, usersPersistence.get(author).unsafeRunSync().map(_.login).getOrElse(""))
      }.toList
    }
  }


  override def getPostWithComments(id: Long): IO[MemeItemWithComments] = synchronized {
    IO.pure {
      val c = comments.filter(i => i._2.memeId == id).map { case (k, v) =>
        import v._
        CommentItemWithId(k, memeId, comment, timestamp, points, author, usersPersistence.get(author).unsafeRunSync().map(_.login).getOrElse(""))
      }.toList
      val m = memes(id)
      import m._
      MemeItemWithComments(MemeItemWithId(id, title, timestamp, content, points, author, usersPersistence.get(author).unsafeRunSync().map(_.login).getOrElse("")), c)
    }
  }

  override def getContent(memeId: Long, num: Long): IO[Option[Content]] = synchronized {
    IO.pure {
      val m = memes(memeId)
      import m._
      content.find(_.num == num)
    }
  }
}
