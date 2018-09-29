package services.persistence

import cats.effect.IO
import models.{User, UserWithId}

import scala.collection.mutable
import scala.util.Random

class DummyUsersPersistenceImpl extends UsersPersistence {
  private val content: mutable.HashMap[Long, User] = mutable.HashMap.empty[Long, User]

  override def create(user: User): IO[UserWithId] = synchronized {
    val id = Random.nextInt()
    content.put(id, user)
    import user._
    IO.pure(UserWithId(id, login, pass, name, surname, avatar))
  }

  override def delete(id: Long): IO[Unit] = synchronized {
    IO.pure(content.remove(id))
  }

  override def update(user: UserWithId): IO[UserWithId] = synchronized {
    import user._
    content.put(user.id, User(login, pass, name, surname, avatar))
    IO.pure(user)
  }

  override def get(login: String, password: String): IO[Option[UserWithId]] = synchronized {
    IO.pure(content.find{ u => u._2.login == login && u._2.pass == password}.map{ case (k,v) =>
      import v._
      UserWithId(k, v.login, pass, name, surname, avatar)
    })
  }
}
