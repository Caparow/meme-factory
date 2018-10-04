package configs

import com.google.inject.{Inject, Singleton}
import com.zaxxer.hikari.HikariConfig

@Singleton
case class PostgresCfg @Inject()(jdbcDriver : String, url : String, user : String, password : String) {
  lazy val hikariConfig: HikariConfig = {
    val config = new HikariConfig()
    config.setJdbcUrl(url)
    config.setUsername(user)
    config.setPassword(password)
    config.setDriverClassName(jdbcDriver)
    config.setMaximumPoolSize(15)
    config
  }
}