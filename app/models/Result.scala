package models

import cats.effect.IO
import controllers.routes
import play.api.mvc.Results

trait Result {
  type Result[F] = IO[Either[ServiceException, F]]
}



trait ResultExt extends Result {
  implicit class ResultExt[F](r: Result[F]) {
    def convert[T <: play.api.mvc.Result](f: F => T): IO[play.api.mvc.Result] = {
      r.map{
        case Left(m) => Results.Redirect(routes.Application.error(m.message))
        case Right(value) => f(value)
      }
    }
  }

  implicit class ResultIOExt[F](r: IO[F]) {
    def succ: Result[F] = r.map(v => Right(v))
  }

  implicit class ResultIOOptExt[F](r: IO[Option[F]]) {
    def toRes(msg: String): Result[F] = r.map{
      case None => Left(ServiceException(1, msg))
      case Some(v) => Right(v)
    }
  }
}