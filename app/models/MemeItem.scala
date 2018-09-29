package models

import io.circe._
import io.circe.generic.semiauto._
import CommentItem._

case class MemeItem(
                     title: String
                     , timestamp: String
                     , content: List[Content]
                     , points: Long
                     , author: Long
                   )

case class MemeItemWithId(
                           id: Long
                           , title: String
                           , timestamp: String
                           , content: List[Content]
                           , points: Long
                           , author: Long
                         )

case class MemeItemWithoutContent(
                           id: Long
                           , title: String
                           , timestamp: String
                           , points: Long
                           , author: Long
                         )

case class MemeItemWithComments(
                                 memeItem: MemeItemWithId
                                 , comments: List[CommentItemWithId]
                               )


case class Content(memeID: Long, contentType: String, content: String, num: Long)

object ContentTypes{
  val HTML = "HTML"
  val TEXT = "TEXT"
  val IMAGE = "IMAGE"
}

object MemeItem {
  implicit val contentEncoder: Encoder[Content] = deriveEncoder[Content]
  implicit val contentDecoder: Decoder[Content] = deriveDecoder[Content]

  implicit val memeItemEncoder: Encoder[MemeItem] = deriveEncoder[MemeItem]
  implicit val memeItemDecoder: Decoder[MemeItem] = deriveDecoder[MemeItem]

  implicit val memeItemWithIdEncoder: Encoder[MemeItemWithId] = deriveEncoder[MemeItemWithId]
  implicit val memeItemWithIdDecoder: Decoder[MemeItemWithId] = deriveDecoder[MemeItemWithId]

  implicit val memeItemWithCommentsEncoder: Encoder[MemeItemWithComments] = deriveEncoder[MemeItemWithComments]
  implicit val memeItemWithCommentsDecoder: Decoder[MemeItemWithComments] = deriveDecoder[MemeItemWithComments]
}