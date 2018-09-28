package models

import io.circe._
import io.circe.generic.semiauto._
import CommentItem._

case class PostItem(
                     title: String
                     , timestamp: String
                     , content: List[Content]
                     , points: Long
                     , author: Long
                   )

case class PostItemWithId(
                           id: Long
                           , postItem: PostItem
                         )

case class PostItemWithComments(
                                 postItem: PostItemWithId
                                 , comments: List[CommentItemWithId]
                               )


case class Content(Value: String, contentType: String)

object ContentTypes{
  val HTML = "HTML"
  val TEXT = "TEXT"
  val IMAGE = "IMAGE"
}

object PostItem {
  implicit val contentEncoder: Encoder[Content] = deriveEncoder[Content]
  implicit val contentDecoder: Decoder[Content] = deriveDecoder[Content]

  implicit val feedItemEncoder: Encoder[PostItem] = deriveEncoder[PostItem]
  implicit val feedItemDecoder: Decoder[PostItem] = deriveDecoder[PostItem]

  implicit val feedItemWithIdEncoder: Encoder[PostItemWithId] = deriveEncoder[PostItemWithId]
  implicit val feedItemWithIdDecoder: Decoder[PostItemWithId] = deriveDecoder[PostItemWithId]

  implicit val feedItemWithCommentsEncoder: Encoder[PostItemWithComments] = deriveEncoder[PostItemWithComments]
  implicit val feedItemWithCommentsDecoder: Decoder[PostItemWithComments] = deriveDecoder[PostItemWithComments]
}