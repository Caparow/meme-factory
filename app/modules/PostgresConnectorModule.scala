package modules

import com.google.inject.AbstractModule
import services.persistence.{PostgresConnector, PostgresConnectorImpl}

class PostgresConnectorModule extends AbstractModule {
  override def configure(): Unit = {
    bind(classOf[PostgresConnector]).to(classOf[PostgresConnectorImpl])
  }
}