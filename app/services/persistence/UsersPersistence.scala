package services.persistence

import cats.effect.IO
import models._

trait UsersPersistence {

  def create(user: User): IO[UserWithId]

  def delete(id: Long): IO[Unit]

  def update(user: UserWithId): IO[UserWithId]

  def get(login: String, password: String): IO[Option[UserWithId]]

  def get(id: Long): IO[Option[UserWithId]]

  def getAvatar(id: Long): IO[Option[(String, String)]]

  def getUserMark(itemId: Long, userId: Long, itemType: String): IO[Option[Int]]

  def updateUserMark(mark: Int, itemId: Long, userId: Long, itemType: String): IO[Unit]

  def setUserMark(mark: Int, itemId: Long, userId: Long, itemType: String): IO[Unit]
}
