package models

import cats.effect.IO

trait Result {
  type Result[F] = IO[Either[ServiceException, F]]
}
