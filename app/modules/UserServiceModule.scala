package modules

import com.google.inject.AbstractModule
import services.{UserService, UserServiceImpl}

class UserServiceModule extends AbstractModule {
  override def configure(): Unit = {
    bind(classOf[UserService]).to(classOf[UserServiceImpl])
  }
}