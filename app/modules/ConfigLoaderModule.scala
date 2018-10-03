package modules

import com.google.inject.AbstractModule
import configs._
import pureconfig._

class ConfigLoaderModule extends AbstractModule {
  override def configure(): Unit = {
    val postgresCfg = loadConfig[PostgresCfg]("postgres").right.get
    bind(classOf[PostgresCfg]).toInstance(postgresCfg)

    val deadboltConfig = loadConfig[DeadboltConfig]("deadbolt").right.get
    bind(classOf[DeadboltConfig]).toInstance(deadboltConfig)
  }
}