package controllers

import com.google.inject.Inject
import models.Content
import play.api.mvc.{AbstractController, ControllerComponents}

class ConverterController @Inject()(cc: ControllerComponents) extends AbstractController(cc) {
  def convertForm = Action {
    Ok(views.html.index("Your new application is ready."))
  }

  def convert(imageContent: Content, targetFormat: String) = Action {
    Ok(views.html.index("Your new application is ready."))
  }
}
