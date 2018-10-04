package models

import java.time.LocalDateTime

import io.circe._
import io.circe.generic.semiauto._


case class CommentItem(
                        memeId: Long
                        , comment: String
                        , timestamp: LocalDateTime
                        , points: Long
                        , author: Long
                      )

case class CommentItemWithId(
                              id: Long
                              , memeId: Long
                              , comment: String
                              , timestamp: LocalDateTime
                              , points: Long
                              , author: Long
                            )

object CommentItem {
  import MemeItem._

  implicit val commentItemEncoder: Encoder[CommentItem] = deriveEncoder[CommentItem]
  implicit val commentItemDecoder: Decoder[CommentItem] = deriveDecoder[CommentItem]

  implicit val commentItemWithIDEncoder: Encoder[CommentItemWithId] = deriveEncoder[CommentItemWithId]
  implicit val commentItemWithIDDecoder: Decoder[CommentItemWithId] = deriveDecoder[CommentItemWithId]
}