package controllers

import be.objectify.deadbolt.scala.ActionBuilders
import com.google.inject.Inject
import configs.DeadboltConfig
import models.Content
import play.api.libs.ws.WSClient
import play.api.mvc.{AbstractController, ControllerComponents}

import scala.concurrent.ExecutionContext

class ConverterController @Inject()(
                                     actionBuilders: ActionBuilders
                                     , ws: WSClient
                                     , deadboltConfig: DeadboltConfig
                                     , cc: ControllerComponents
                                   )(implicit ec: ExecutionContext) extends AbstractController(cc) {
  def convertForm = Action {
    Ok(views.html.index("Your new application is ready."))
  }

  def convert(imageContent: Content, targetFormat: String) = Action {
    Ok(views.html.index("Your new application is ready."))
  }
}
