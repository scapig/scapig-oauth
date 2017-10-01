package config

import javax.inject.Singleton

import models.{OAuthError, OAuthErrorCode}
import play.api.http.{DefaultHttpErrorHandler, HttpErrorHandler}
import play.api.libs.json.Json
import play.api.mvc.{RequestHeader, Result, Results}
import models.JsonFormatters._

import scala.concurrent.Future

@Singleton
class ErrorHandler extends DefaultHttpErrorHandler {
  override def onBadRequest(request: RequestHeader, message: String): Future[Result] = {
    Future.successful(Results.BadRequest(Json.toJson(OAuthError(OAuthErrorCode.INVALID_REQUEST, message)).toString()))
  }
}