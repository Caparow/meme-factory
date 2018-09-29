package modules

import com.google.inject.AbstractModule
import pureconfig._
import services.persistence.{PostgresCfg, PostgresConnector}

class ConfigLoaderModule extends AbstractModule {
  override def configure(): Unit = {
    val postgresCfg = loadConfig[PostgresCfg]("postgres").right.get
    bind(classOf[PostgresCfg]).toInstance(postgresCfg)
  }
}