package services

import com.google.inject.{Inject, Singleton}
import models.{ResultExt, User, UserWithId}
import services.persistence.UsersPersistence

@Singleton
class UserServiceImpl @Inject()(
                                 usersPersistence: UsersPersistence
                               ) extends UserService with ResultExt {
  override def getAvatar(id: Long): Result[Option[(String, String)]] = {
    usersPersistence.getAvatar(id).succ
  }

  override def login(login: String, pass: String): Result[UserWithId] = {
    usersPersistence.get(login, pass).toRes("User not found.")
  }

  override def getUser(id: Long): Result[UserWithId] = {
    usersPersistence.get(id).toRes("User not found.")
  }

  override def register(user: User): Result[UserWithId] = {
    usersPersistence.create(user).succ
  }

  override def updateProfile(userWithId: UserWithId): Result[UserWithId] = {
    usersPersistence.update(userWithId).succ
  }
}
