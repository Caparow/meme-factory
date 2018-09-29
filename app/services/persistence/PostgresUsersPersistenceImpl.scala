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

  override def update(user: UserWithId): IO[UserWithId] = {
    val userQ = for {
      _ <- updateUserStmt(user).update.run
      comment <- getUserStmt(user.id).query[UserWithId].unique
    } yield comment

    connector.query(userQ)
  }
}


object PostgresUsersPersistenceImpl {
  def getUserStmt(id: Long): Fragment = {
    sql"""select id, login,password,user_name,surname,avatar from users where id = $id;"""
  }

  def getUserByLogAndPassStmt(login: String, pass: String): Fragment = {
    sql"""select id, login,password,user_name,surname,avatar from users where login = $login and password = $pass;"""
  }

  def deleteUserStmt(id: Long): Fragment = {
    sql"""delete from users where id = $id;"""
  }

  def createUserStmt(user: User): Fragment = {
    import user._
    sql"""
         |insert into users(login,password,user_name,surname,avatar) values
         |($login,$pass,$name,$surname,$avatar);
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
         |avatar = $avatar
         |where id = ${user.id};
       """.stripMargin
  }

}

