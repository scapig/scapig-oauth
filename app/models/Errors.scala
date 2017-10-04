package models

import play.api.http.Status._
import play.api.libs.json.Json
import play.api.mvc.{Result, Results}
import models.JsonFormatters._

sealed abstract class ErrorResponse(
                                     val httpStatusCode: Int,
                                     val errorCode: String,
                                     val message: String) {

  def toHttpResponse: Result = Results.Status(httpStatusCode)(Json.toJson(this))
}

case class ErrorInvalidRequest(errorMessage: String) extends ErrorResponse(BAD_REQUEST, "INVALID_REQUEST", errorMessage)

class ValidationException(message: String) extends RuntimeException(message)

case class OAuthError(error: OAuthErrorCode.Value, errorDescription: String, state: Option[String] = None)
case class OauthUnauthorizedException(oauthError: OAuthError) extends Exception
case class OauthValidationException(oauthError: OAuthError) extends Exception

case class ErrorInternalServerError(errorMessage: String) extends ErrorResponse(INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR", errorMessage)
case class ErrorNotFound() extends ErrorResponse(NOT_FOUND, "NOT_FOUND", "The resource could not be found.")

case class ApplicationNotFound() extends Exception
case class RequestedAuthorityNotFound() extends Exception

object OAuthErrorCode extends Enumeration {

  type OAuthErrorCode = Value
  val UNSUPPORTED_RESPONSE_TYPE = Value("unsupported_response_type")
  val INVALID_REQUEST = Value("invalid_request")
  val UNAUTHORIZED_CLIENT = Value("unauthorized_client")
  val INVALID_CLIENT = Value("invalid_client")
  val INVALID_GRANT = Value("invalid_grant")
  val INVALID_SCOPE = Value("invalid_scope")
  val SERVER_ERROR = Value("server_error")
  val ACCESS_DENIED = Value("access_denied")
}

object OAuthError {
  val invalidCode = OAuthError(OAuthErrorCode.INVALID_REQUEST, "code is invalid")
  val invalidRedirectUri = OAuthError(OAuthErrorCode.INVALID_REQUEST, "redirect_uri is invalid")
  val invalidClientOrSecret = OAuthError(OAuthErrorCode.INVALID_CLIENT, "invalid client id or secret")
  val invalidRefreshToken = OAuthError(OAuthErrorCode.INVALID_REQUEST, "refresh_token is invalid")
  val unauthorizedClient = OAuthError(OAuthErrorCode.UNAUTHORIZED_CLIENT, "client id invalid for the client_credentials grant type")
  val unauthorizedRopcClient = OAuthError(OAuthErrorCode.UNAUTHORIZED_CLIENT, "client id invalid for the password grant type")
  val invalidScopeForPrivilegedOrRopcApplication = OAuthError(OAuthErrorCode.INVALID_SCOPE, "application does not have scopes")
  val invalidUsernameOrPassword = OAuthError(OAuthErrorCode.INVALID_GRANT, "invalid username or password")
}
