package services.persistence

import models._

trait UsersPersistence extends Result {

  def create(user: User): Result[UserWithId]

  def delete(id: Long): Result[Unit]

  def update(user: UserWithId): Result[UserWithId]
}
