package services.persistence

import com.google.inject.{Inject, Singleton}
import com.zaxxer.hikari.HikariDataSource
import configs.PostgresCfg
import doobie.ConnectionIO
import doobie.hikari.HikariTransactor
import doobie.hikari.implicits._
import doobie.implicits._

trait PostgresConnector{
  def query[T](query: ConnectionIO[T]): cats.effect.IO[T]
  def close(): Unit
}

@Singleton
final class PostgresConnectorImpl @Inject()(cfg: PostgresCfg) extends PostgresConnector {

  private lazy val transactor: HikariTransactor[cats.effect.IO] = {
    val ds = new HikariDataSource(cfg.hikariConfig)
    HikariTransactor(ds)
  }

  private def closeConnectionPool(): cats.effect.IO[Unit] = {
    transactor.shutdown
  }

  override def query[T](query: ConnectionIO[T]): cats.effect.IO[T] = {
    query.transact(transactor)
  }

  override def close(): Unit = {
    closeConnectionPool().unsafeRunSync()
  }
}