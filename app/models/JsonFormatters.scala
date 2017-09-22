package models

import org.joda.time.DateTime
import play.api.libs.json._

object JsonFormatters {
  val datePattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ"

  implicit val dateRead: Reads[DateTime] = JodaReads.jodaDateReads(datePattern)
  implicit val dateWrite: Writes[DateTime] = JodaWrites.jodaDateWrites(datePattern)
  implicit val dateFormat: Format[DateTime] = Format[DateTime](dateRead, dateWrite)

  implicit val formatEnvironment = EnumJson.enumFormat(Environment)
  implicit val formatDelegatedAuthorityRequest = Json.format[DelegatedAuthorityRequest]
  implicit val formatToken = Json.format[Token]
  implicit val formatDelegatedAuthority = Json.format[DelegatedAuthority]
  implicit val formatTokenResponse = Json.format[TokenResponse]

  implicit val formatCreateRequestedAuthorityRequest = Json.format[CreateRequestedAuthorityRequest]
  implicit val formatAuthorizationCode = Json.format[AuthorizationCode]
  implicit val formatRequestedAuthority = Json.format[RequestedAuthority]
  implicit val formatCompleteRequestedAuthorityRequest = Json.format[CompleteRequestedAuthorityRequest]

  implicit val formatApplicationUrls = Json.format[ApplicationUrls]
  implicit val formatEnvironmentApplication = Json.format[EnvironmentApplication]
  implicit val formatAuthenticateRequest = Json.format[AuthenticateRequest]

  implicit val formatScope = Json.format[Scope]

  implicit val errorResponseWrites = new Writes[ErrorResponse] {
    def writes(e: ErrorResponse): JsValue = Json.obj("code" -> e.errorCode, "message" -> e.message)
  }

  implicit val write = new Writes[OAuthError] {
    def writes(oauthError: OAuthError): JsValue = {
      val json = Json.obj(
        "error" -> oauthError.error.toString,
        "error_description" -> oauthError.errorDescription
      )
      oauthError.state.fold(json)({ st => json ++ Json.obj("state" -> st) })
    }
  }

}
