package modules

import com.google.inject.AbstractModule
import services.persistence.{DummyUsersPersistenceImpl, PostgresUsersPersistenceImpl, UsersPersistence}

class UserPersistenceModule extends AbstractModule {
  override def configure(): Unit = {
    bind(classOf[UsersPersistence]).to(classOf[DummyUsersPersistenceImpl])
  }
}