package models

import io.circe._
import io.circe.generic.semiauto._

case class User(
                 login: String
                 , pass: String
                 , name: Option[String]
                 , surname: Option[String]
                 , avatar: Option[String]
                 , avatarType: Option[String]
               )

case class UserWithId(
                       id: Long
                       , login: String
                       , pass: String
                       , name: Option[String]
                       , surname: Option[String]
                       , avatar: Option[String]
                       , avatarType: Option[String]
                     )


object User {
  implicit val userEncoder: Encoder[User] = deriveEncoder[User]
  implicit val userDecoder: Decoder[User] = deriveDecoder[User]

  implicit val userWithIdEncoder: Encoder[UserWithId] = deriveEncoder[UserWithId]
  implicit val userWithIdDecoder: Decoder[UserWithId] = deriveDecoder[UserWithId]
}

