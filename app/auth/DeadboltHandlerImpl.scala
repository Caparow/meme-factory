package auth

import be.objectify.deadbolt.scala.models.Subject
import be.objectify.deadbolt.scala.{AuthenticatedRequest, DeadboltHandler, DynamicResourceHandler}
import com.google.inject.{Inject, Singleton}
import configs.DeadboltConfig
import controllers.routes
import models.auth.{Anon, User}
import play.api.mvc.{Request, Result, Results}

import scala.concurrent.Future

@Singleton
class DeadboltHandlerImpl @Inject()(deadboltConfig: DeadboltConfig) extends DeadboltHandler {
  override def beforeAuthCheck[A](request: Request[A]): Future[Option[Result]] = Future.successful(None)

  override def getSubject[A](request: AuthenticatedRequest[A]): Future[Option[Subject]] = Future.successful {
    request.session.get(deadboltConfig.authTokenKey).flatMap { authToken =>
      request.session.get(deadboltConfig.roleKey).map { role =>
        val identifier = request.session(deadboltConfig.identifierKey)
        role match {
          case "anon" => Anon(identifier)
          case "user" => User(identifier, authToken)
          case _ =>
            throw new RuntimeException("Bad role in the session")
        }
      }
    }
  }

  override def onAuthFailure[A](request: AuthenticatedRequest[A]): Future[Result] =
    Future.successful(Results.Redirect(routes.Application.authError("Problems with authorisation occurred")))

  override def getDynamicResourceHandler[A](request: Request[A]): Future[Option[DynamicResourceHandler]] =
    Future.successful(None)
}
