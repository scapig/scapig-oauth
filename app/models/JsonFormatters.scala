package models

import org.joda.time.DateTime
import play.api.libs.json._

object JsonFormatters {
  val datePattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ"

  implicit val dateRead: Reads[DateTime] = JodaReads.jodaDateReads(datePattern)
  implicit val dateWrite: Writes[DateTime] = JodaWrites.jodaDateWrites(datePattern)
  implicit val dateFormat: Format[DateTime] = Format[DateTime](dateRead, dateWrite)

  implicit val formatAuthType = EnumJson.enumFormat(AuthType)
  implicit val formatDelegatedAuthorityRequest = Json.format[DelegatedAuthorityRequest]
  implicit val formatToken = Json.format[Token]
  implicit val formatDelegatedAuthority = Json.format[DelegatedAuthority]

  implicit val formatCreateRequestedAuthorityRequest = Json.format[CreateRequestedAuthorityRequest]
  implicit val formatAuthorizationCode = Json.format[AuthorizationCode]
  implicit val formatRequestedAuthority = Json.format[RequestedAuthority]
  implicit val formatCompleteRequestedAuthorityRequest = Json.format[CompleteRequestedAuthorityRequest]

  implicit val formatClientSecret = Json.format[ClientSecret]
  implicit val formatEnvironmentToken = Json.format[EnvironmentToken]
  implicit val formatApplicationTokens = Json.format[ApplicationTokens]
  implicit val formatApplicationUrls = Json.format[ApplicationUrls]
  implicit val formatApplication = Json.format[Application]

  implicit val errorResponseWrites = new Writes[ErrorResponse] {
    def writes(e: ErrorResponse): JsValue = Json.obj("code" -> e.errorCode, "message" -> e.message)
  }
}
