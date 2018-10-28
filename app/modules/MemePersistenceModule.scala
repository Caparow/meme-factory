package modules

import com.google.inject.AbstractModule
import services.persistence.{DummyPostsPersistenceImpl, PostsPersistence}

class MemePersistenceModule extends AbstractModule {
  override def configure(): Unit = {
    bind(classOf[PostsPersistence]).to(classOf[DummyPostsPersistenceImpl])
  }
}