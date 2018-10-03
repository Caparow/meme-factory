package modules

import auth.HandlerCacheImpl
import be.objectify.deadbolt.scala.cache.HandlerCache
import com.google.inject.AbstractModule

class DeadboltModule extends AbstractModule {
  override def configure(): Unit = {
    bind(classOf[HandlerCache]).to(classOf[HandlerCacheImpl])
  }
}