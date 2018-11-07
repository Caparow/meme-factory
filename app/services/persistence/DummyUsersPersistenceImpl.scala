package services.persistence

import cats.effect.IO
import com.google.inject.{Inject, Singleton}
import models.{User, UserWithId}

import scala.collection.mutable
import scala.util.Random

@Singleton
class DummyUsersPersistenceImpl @Inject() extends UsersPersistence {
  case class UserMarkKey(itemId: Long, userId: Long, itemType: String)
  private val content: mutable.HashMap[Long, User] = mutable.HashMap.empty[Long, User]
  private val userMarks: mutable.HashMap[UserMarkKey, Int] = mutable.HashMap.empty[UserMarkKey, Int]

  override def create(user: User): IO[UserWithId] = synchronized {
    val id = Random.nextInt()
    content.put(id, user)
    import user._
    IO.pure(UserWithId(id, login, pass, name, surname, avatar, avatarType))
  }

  override def delete(id: Long): IO[Unit] = synchronized {
    IO.pure(content.remove(id))
  }

  override def update(user: UserWithId): IO[UserWithId] = synchronized {
    import user._
    content.put(user.id, User(login, pass, name, surname, avatar, avatarType))
    IO.pure(user)
  }

  override def get(id: Long): IO[Option[UserWithId]] = synchronized {
    IO.pure(content.get(id).map { v =>
      import v._
      UserWithId(id, v.login, pass, name, surname, avatar, avatarType)
    })
  }

  override def getUserMark(itemId: Long, userId: Long, itemType: String): IO[Option[Int]] = synchronized {
    IO.pure(userMarks.get(UserMarkKey(itemId, userId, itemType)))
  }

  override def updateUserMark(mark: Int, itemId: Long, userId: Long, itemType: String): IO[Unit] = synchronized {
    IO.pure(userMarks.put(UserMarkKey(itemId, userId, itemType), mark))
  }

  override def setUserMark(mark: Int, itemId: Long, userId: Long, itemType: String): IO[Unit] = synchronized {
    IO.pure(userMarks.put(UserMarkKey(itemId, userId, itemType), mark))
  }

  override def getAvatar(id: Long): IO[Option[(String, String)]] = synchronized {
    IO.pure(
      content.get(id).flatMap(u => u.avatar.flatMap(a => u.avatarType.map(t => (a,t))))
    )
  }

  override def get(login: String, password: String): IO[Option[UserWithId]] = synchronized {
    IO.pure(content.find{ u => u._2.login == login && u._2.pass == password}.map{ case (k,v) =>
      import v._
      UserWithId(k, v.login, pass, name, surname, avatar, avatarType)
    })
  }
}
