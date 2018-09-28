package services
import models.{User, UserWithId}

class UserServiceImpl extends UserService {
  override def login(login: String, pass: String): Result[UserWithId] = ???

  override def register(user: User): Result[UserWithId] = ???

  override def updateProfile(userWithId: UserWithId): Result[UserWithId] = ???
}
