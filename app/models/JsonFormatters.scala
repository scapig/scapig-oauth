package models

import org.joda.time.DateTime
import play.api.libs.json._

object JsonFormatters {
  val datePattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ"

  implicit val dateRead: Reads[DateTime] = JodaReads.jodaDateReads(datePattern)
  implicit val dateWrite: Writes[DateTime] = JodaWrites.jodaDateWrites(datePattern)
  implicit val dateFormat: Format[DateTime] = Format[DateTime](dateRead, dateWrite)

  implicit val formatAuthType = EnumJson.enumFormat(AuthType)
  implicit val formatAuthorityRequest = Json.format[AuthorityRequest]
  implicit val formatToken = Json.format[Token]
  implicit val formatDelegatedAuthority = Json.format[DelegatedAuthority]

  implicit val errorResponseWrites = new Writes[ErrorResponse] {
    def writes(e: ErrorResponse): JsValue = Json.obj("code" -> e.errorCode, "message" -> e.message)
  }
}
