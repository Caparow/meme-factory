package auth

import be.objectify.deadbolt.scala.{DeadboltHandler, HandlerKey}
import be.objectify.deadbolt.scala.cache.HandlerCache
import com.google.inject.{Inject, Singleton}

@Singleton
class HandlerCacheImpl @Inject()(deadboltHandlerImpl: DeadboltHandlerImpl) extends HandlerCache {

  val defaultHandler: DeadboltHandler = deadboltHandlerImpl

  val handlers: Map[Any, DeadboltHandler] = Map(HandlerKeys.defaultHandler -> defaultHandler)

  override def apply(v1: HandlerKey): DeadboltHandler = handlers(v1)

  override def apply(): DeadboltHandler = defaultHandler

}

object HandlerKeys {
  val defaultHandler: HandlerKey = new HandlerKey {
    val id: String = "default"
  }
}
