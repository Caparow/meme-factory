package models.auth

import be.objectify.deadbolt.scala.models.{Permission, Role, Subject}

case class Admin(identifier: String, authToken: String) extends Subject {
  val roles: List[Role] = List(AdminRole)

  val permissions: List[Permission] = List(AdminPermission)
}

object AdminRole extends Role {
  val name: String = "admin"
}

object AdminPermission extends Permission {
  val value: String = "admin"
}
