package services.persistence

import cats.effect.IO
import com.google.inject.{Inject, Singleton}
import doobie.implicits._
import doobie.util.fragment.Fragment
import models.{User, UserWithId}
import services.persistence.PostgresUsersPersistenceImpl._

@Singleton
class PostgresUsersPersistenceImpl @Inject()(connector: PostgresConnector) extends UsersPersistence {

  override def create(user: User): IO[UserWithId] = {
    val userQ = for {
      _ <- createUserStmt(user).update.run
      id <- sql"select lastval()".query[Long].unique
      comment <- getUserStmt(id).query[UserWithId].unique
    } yield comment

    connector.query(userQ)
  }

  override def delete(id: Long): IO[Unit] = {
    connector.query(deleteUserStmt(id).update.run.map(_ => ()))
  }

  override def get(login: String, password: String): IO[Option[UserWithId]] = {
    connector.query(getUserByLogAndPassStmt(login, password).query[UserWithId].option)
  }

  override def get(id: Long): IO[Option[UserWithId]] = {
    connector.query(getUserStmt(id).query[UserWithId].option)
  }

  override def getAvatar(id: Long): IO[Option[(String, String)]] = {
    connector.query(getUserAvatarStmt(id).query[(Option[String], Option[String])].option.map(
      _.flatMap { case (a, t) => a.flatMap(aa => t.map(tt => (aa, tt))) }
    ))
  }

  override def update(user: UserWithId): IO[UserWithId] = {
    val userQ = for {
      _ <- updateUserStmt(user).update.run
      comment <- getUserStmt(user.id).query[UserWithId].unique
    } yield comment

    connector.query(userQ)
  }
}


object PostgresUsersPersistenceImpl {

  def getUserAvatarStmt(id: Long): Fragment = {
    sql"""select avatar, avatar_type from users where id = $id;"""
  }

  def getUserStmt(id: Long): Fragment = {
    sql"""select id, login,password,user_name,surname,avatar,avatar_type from users where id = $id;"""
  }

  def getUserByLogAndPassStmt(login: String, pass: String): Fragment = {
    sql"""select id, login,password,user_name,surname,avatar,avatar_type from users where login = $login and password = $pass;"""
  }

  def deleteUserStmt(id: Long): Fragment = {
    sql"""delete from users where id = $id;"""
  }

  def createUserStmt(user: User): Fragment = {
    import user._
    sql"""
         |insert into users(login,password,user_name,surname,avatar,avatar_type) values
         |($login,$pass,$name,$surname,$avatar,$avatarType);
       """.stripMargin
  }

  def updateUserStmt(user: UserWithId): Fragment = {
    import user._
    sql"""
         |update users set
         |login = $login,
         |password = $pass,
         |user_name = $name,
         |surname = $surname,
         |avatar = $avatar,
         |avatar_type = $avatarType
         |where id = ${user.id};
       """.stripMargin
  }

}

