package models.auth

import be.objectify.deadbolt.scala.models.{Permission, Role, Subject}

case class Anon(identifier: String) extends Subject {
  val roles: List[Role] = List(AnonRole)

  val permissions: List[Permission] = List(AnonPermissions)
}

object AnonRole extends Role {
  val name: String = "student"
}

object AnonPermissions extends Permission {
  val value: String = "anon"
}
