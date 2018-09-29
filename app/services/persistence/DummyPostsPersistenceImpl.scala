package services.persistence

import cats.effect.IO
import models._
import services.persistence.PostsPersistence.FeedOffset

import scala.collection.mutable
import scala.util.Random

class DummyPostsPersistenceImpl extends PostsPersistence {
  val memes: mutable.HashMap[Long, MemeItem] = mutable.HashMap.empty[Long, MemeItem]
  val comments: mutable.HashMap[Long, CommentItem] = mutable.HashMap.empty[Long, CommentItem]

  override def createPost(feedItem: MemeItem): IO[MemeItemWithId] = synchronized {
    val id = Random.nextInt()
    memes.put(id, feedItem)
    IO.pure(MemeItemWithId(id, feedItem))
  }

  override def deletePost(id: Long): IO[Unit] = synchronized {
    IO.pure(memes.remove(id))
  }


  override def upVotePost(id: Long): IO[Unit] = synchronized {
    IO.pure {
      val m = memes(id)
      memes.put(id, m.copy(points = m.points + 1))
      ()
    }
  }


  override def downVotePost(id: Long): IO[Unit] = synchronized {
    IO.pure {
      val m = memes(id)
      memes.put(id, m.copy(points = m.points - 1))
      ()
    }
  }


  override def createComment(commentItem: CommentItem): IO[CommentItemWithId] = synchronized {
    val id = Random.nextInt()
    comments.put(id, commentItem)
    IO.pure(CommentItemWithId(id, commentItem))
  }


  override def deleteComment(id: Long): IO[Unit] = synchronized {
    IO.pure(comments.remove(id))
  }


  override def upVoteComment(id: Long): IO[Unit] = synchronized {
    IO.pure {
      val m = comments(id)
      comments.put(id, m.copy(points = m.points + 1))
      ()
    }
  }


  override def downVoteComment(id: Long): IO[Unit] = synchronized {
    IO.pure {
      val m = comments(id)
      comments.put(id, m.copy(points = m.points - 1))
      ()
    }
  }


  override def getMostPopular(forDays: Int, offset: FeedOffset): IO[List[MemeItemWithId]] = synchronized {
    IO.pure{
      memes.map{case (k, v) => MemeItemWithId(k,v)}.toList

    }
  }


  override def getLatest(offset: FeedOffset): IO[List[MemeItemWithId]] = synchronized {
    IO.pure{
      memes.map{case (k, v) => MemeItemWithId(k,v)}.toList
    }
  }


  override def getPostWithComments(id: Long): IO[MemeItemWithComments] = synchronized {
    IO.pure{
      val c = comments.filter(i => i._2.memeId == id).map{case (k,v) => CommentItemWithId(k,v)}.toList
      val m = memes(id)
      MemeItemWithComments(MemeItemWithId(id,m), c)
    }
  }

}
