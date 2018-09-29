package models

import io.circe._
import io.circe.generic.semiauto._


case class CommentItem(
                        memeId: Long
                        , comment: String
                        , timestamp: String
                        , points: Long
                        , author: Long
                      )

case class CommentItemWithId(
                              id: Long
                              , memeId: Long
                              , comment: String
                              , timestamp: String
                              , points: Long
                              , author: Long
                            )

object CommentItem {
  implicit val commentItemEncoder: Encoder[CommentItem] = deriveEncoder[CommentItem]
  implicit val commentItemDecoder: Decoder[CommentItem] = deriveDecoder[CommentItem]

  implicit val commentItemWithIDEncoder: Encoder[CommentItemWithId] = deriveEncoder[CommentItemWithId]
  implicit val commentItemWithIDDecoder: Decoder[CommentItemWithId] = deriveDecoder[CommentItemWithId]
}