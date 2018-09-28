package models

import io.circe._
import io.circe.generic.semiauto._

case class User(
                 login: String
                 , pass: String
                 , name: Option[String]
                 , surname: Option[String]
                 , avatar: Option[String]
               )

case class UserWithId(
                       id: Long
                       , user: User
                     )


object User {
  implicit val userEncoder: Encoder[User] = deriveEncoder[User]
  implicit val userDecoder: Decoder[User] = deriveDecoder[User]

  implicit val userWithIdEncoder: Encoder[UserWithId] = deriveEncoder[UserWithId]
  implicit val userWithIdDecoder: Decoder[UserWithId] = deriveDecoder[UserWithId]
}

