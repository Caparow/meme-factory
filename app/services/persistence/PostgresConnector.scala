package services.persistence

import com.zaxxer.hikari.{HikariConfig, HikariDataSource}
import doobie.ConnectionIO
import doobie.hikari.HikariTransactor
import doobie.hikari.implicits._
import doobie.implicits._

final class PostgresConnector(cfg: PostgresCfg) {

  private lazy val transactor: HikariTransactor[cats.effect.IO] = {
    val ds = new HikariDataSource(cfg.hikariConfig)
    HikariTransactor(ds)
  }

  def query[T](query: ConnectionIO[T]): cats.effect.IO[T] = {
    query.transact(transactor)
  }

  private def closeConnectionPool(): cats.effect.IO[Unit] = {
    transactor.shutdown
  }

  def close(): Unit = {
    closeConnectionPool().unsafeRunSync()
  }
}

case class PostgresCfg(jdbcDriver : String, url : String, user : String, password : String) {
  lazy val hikariConfig: HikariConfig = {
    val config = new HikariConfig()
    config.setJdbcUrl(url)
    config.setUsername(user)
    config.setPassword(password)
    config.setDriverClassName(jdbcDriver)
    config
  }
}