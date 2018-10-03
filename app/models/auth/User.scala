package models.auth

import be.objectify.deadbolt.scala.models.{Permission, Role, Subject}

case class User(identifier: String, authToken: String) extends Subject {
  val roles: List[Role] = List(UserRole)

  val permissions: List[Permission] = List(UserPermission)
}

object UserRole extends Role {
  val name: String = "user"
}

object UserPermission extends Permission {
  val value: String = "user"
}
