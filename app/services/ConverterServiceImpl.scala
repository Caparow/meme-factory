package services
import models.Content

class ConverterServiceImpl extends ConverterService {
  override def convertImage(imageContent: Content, targetFormat: String): Result[Content] = ???
}
