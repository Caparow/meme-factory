package modules

import com.google.inject.AbstractModule
import services.{MemeService, MemeServiceImpl}

class MemeServiceModule extends AbstractModule {
  override def configure(): Unit = {
    bind(classOf[MemeService]).to(classOf[MemeServiceImpl])
  }
}