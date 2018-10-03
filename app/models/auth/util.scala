package models.auth

import be.objectify.deadbolt.scala.models.Role

object Role {
  def apply(str: String): Role = str match {
    case "anon" => AnonRole
    case "admin" => AdminRole
    case "user" => UserRole
  }

  def all : List[Array[String]] = List(AnonRole, AdminRole, UserRole).map(n => Array(n.name))
}
