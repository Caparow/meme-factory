package services

import com.google.inject.{Inject, Singleton}
import models.{ServiceException, User, UserWithId}
import services.persistence.UsersPersistence

@Singleton
class UserServiceImpl @Inject()(
                                 usersPersistence: UsersPersistence
                               ) extends UserService {
  override def login(login: String, pass: String): Result[UserWithId] = {
    usersPersistence.get(login, pass).map{
      case Some(v) => Right(v)
      case None => Left(ServiceException(1, "User not found."))
    }
  }

  override def register(user: User): Result[UserWithId] = {
    usersPersistence.create(user).map(v => Right(v))
  }

  override def updateProfile(userWithId: UserWithId): Result[UserWithId] = {
    usersPersistence.update(userWithId).map(v => Right(v))
  }
}
