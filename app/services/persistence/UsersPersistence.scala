package services.persistence

import cats.effect.IO
import models._

trait UsersPersistence {

  def create(user: User): IO[UserWithId]

  def delete(id: Long): IO[Unit]

  def update(user: UserWithId): IO[UserWithId]

  def get(login: String, password: String): IO[Option[UserWithId]]

  def get(id: Long): IO[Option[UserWithId]]
}
