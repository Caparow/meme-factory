package services

import models._

trait UserService extends Result {
  def login(login: String, pass: String): Result[UserWithId]

  def register(user: User): Result[UserWithId]

  def updateProfile(userWithId: UserWithId): Result[UserWithId]

  def getAvatar(id: Long): Result[Option[(String, String)]]

  def getUser(id: Long): Result[UserWithId]
}
