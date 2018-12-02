package models

import java.time.LocalDateTime
import java.time.format.{DateTimeFormatter, DateTimeParseException}

import CommentItem._
import io.circe._
import io.circe.generic.semiauto._

case class MemeItem(
                     title: String
                     , timestamp: LocalDateTime
                     , content: List[Content]
                     , points: Long
                     , author: Long
                   )

case class MemeItemWithId(
                           id: Long
                           , title: String
                           , timestamp: LocalDateTime
                           , content: List[Content]
                           , points: Long
                           , author: Long
                           , login: String
                         )

case class MemeItemWithoutContent(
                                   id: Long
                                   , title: String
                                   , timestamp: LocalDateTime
                                   , points: Long
                                   , author: Long
                                   , login: String
                                 )

case class MemeItemWithComments(
                                 memeItem: MemeItemWithId
                                 , comments: List[CommentItemWithId]
                               )


case class Content(memeID: Long, contentType: String, content: String, num: Long)

object ContentTypes {
  val HTML = "HTML"
  val TEXT = "TEXT"

  val IMAGE_PNG = ".png"
  val IMAGE_JPG = ".jpg"
  val IMAGE_GIF = ".gif"
  val IMAGE_ICO = ".ico"

  val VIDEO_MP4 = ".mp4"

  val AUDIO_MP3 = ".mp3"

  def isImage(t: String): Boolean = {
    t == IMAGE_JPG || t == IMAGE_PNG || t == IMAGE_GIF || t == IMAGE_ICO
  }

  def isVideo(t: String): Boolean = {
    t == VIDEO_MP4
  }

  def isAudio(t: String): Boolean = {
    t == AUDIO_MP3
  }
}

object MemeItem {

  final def decodeLocalDateTime(formatter: DateTimeFormatter): Decoder[LocalDateTime] =
    Decoder.instance { c =>
      c.as[String] match {
        case Right(s) => try Right(LocalDateTime.parse(s, formatter)) catch {
          case _: DateTimeParseException => Left(DecodingFailure("LocalDateTime", c.history))
        }
        case l@Left(_) => l.asInstanceOf[Decoder.Result[LocalDateTime]]
      }
    }

  final def encodeLocalDateTime(formatter: DateTimeFormatter): Encoder[LocalDateTime] =
    Encoder.instance(time => Json.fromString(time.format(formatter)))


  implicit final val decodeLocalDateTimeDefault: Decoder[LocalDateTime] = decodeLocalDateTime(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
  implicit final val encodeLocalDateTimeDefault: Encoder[LocalDateTime] = encodeLocalDateTime(DateTimeFormatter.ISO_LOCAL_DATE_TIME)

  implicit val contentEncoder: Encoder[Content] = deriveEncoder[Content]
  implicit val contentDecoder: Decoder[Content] = deriveDecoder[Content]

  implicit val memeItemEncoder: Encoder[MemeItem] = deriveEncoder[MemeItem]
  implicit val memeItemDecoder: Decoder[MemeItem] = deriveDecoder[MemeItem]

  implicit val memeItemWithIdEncoder: Encoder[MemeItemWithId] = deriveEncoder[MemeItemWithId]
  implicit val memeItemWithIdDecoder: Decoder[MemeItemWithId] = deriveDecoder[MemeItemWithId]

  implicit val memeItemWithCommentsEncoder: Encoder[MemeItemWithComments] = deriveEncoder[MemeItemWithComments]
  implicit val memeItemWithCommentsDecoder: Decoder[MemeItemWithComments] = deriveDecoder[MemeItemWithComments]
}