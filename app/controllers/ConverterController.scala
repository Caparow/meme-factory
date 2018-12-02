package controllers

import java.io.{File, FileInputStream}
import java.nio.file.{Files, Paths}

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import be.objectify.deadbolt.scala.ActionBuilders
import com.google.inject.Inject
import configs.DeadboltConfig
import javax.imageio.ImageIO
import models.auth.UserRole
import play.api.libs.ws.WSClient
import play.api.mvc.{AbstractController, ControllerComponents}

import scala.concurrent.{ExecutionContext, Future}

class ConverterController @Inject()(
                                     actionBuilders: ActionBuilders
                                     , ws: WSClient
                                     , deadboltConfig: DeadboltConfig
                                     , cc: ControllerComponents
                                   )(implicit ec: ExecutionContext) extends AbstractController(cc) {

  private implicit val p = cc.parsers

  implicit val system = ActorSystem("QuickStart")
  implicit val materializer = ActorMaterializer()

  private def authAction = actionBuilders.RestrictAction(UserRole.name).defaultHandler()

  def converter = Action {
    Ok(views.html.converter("hithere"))
  }

  def convertForm = authAction(parse.multipartFormData) { implicit request =>
    val dataParts = request.body.dataParts
    val res = for {
      targetType <- dataParts.get("targetField").flatMap(_.headOption)
      image <- request.body.file("imageField")
    } yield {
      val filename = Paths.get(image.filename).getFileName.toString
      val currentType = filename.substring(filename.lastIndexOf("."))

      val inputFile = File.createTempFile(filename, currentType)
      val outputFile = File.createTempFile(filename, targetType)

      val bytes = Files.readAllBytes(Paths.get(image.ref.getAbsolutePath))
      Files.write(Paths.get(inputFile.getAbsolutePath), bytes)

      val imageIOfile = ImageIO.read(inputFile)

      val res = ImageIO.write(imageIOfile, targetType.replace(".", ""), outputFile)
      if (res){
        Future{Ok.sendFile(outputFile)}
      } else Future(Ok(views.html.error(s"Sorry but we can't convert image from $currentType to $targetType")))
    }
    res.getOrElse(Future(BadRequest("Form is invalid")))
  }

}
