package services.persistence
import models.{User, UserWithId}

class PostgresUsersPersistenceImpl(connector: PostgresConnector) extends UsersPersistence {

  override def create(user: User): Result[UserWithId] = ???

  override def delete(id: Long): Result[Unit] = ???

  override def update(user: UserWithId): Result[UserWithId] = ???
}
