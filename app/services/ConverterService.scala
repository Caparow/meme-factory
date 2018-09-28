package services

import models._

trait ConverterService extends Result {
  def convertImage(imageContent: Content, targetFormat: String): Result[Content]
}