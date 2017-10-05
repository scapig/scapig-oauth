package config

import javax.inject.Singleton

import models._
import play.api.http.{DefaultHttpErrorHandler, HttpErrorHandler}
import play.api.libs.json.Json
import play.api.mvc.{RequestHeader, Result, Results}
import models.JsonFormatters._
import play.api.Logger

import scala.concurrent.Future

@Singleton
class ErrorHandler extends DefaultHttpErrorHandler {
  override def onBadRequest(request: RequestHeader, message: String): Future[Result] = {
    Future.successful(Results.BadRequest(Json.obj("error" -> "invalid_request", "error_description" -> message)))
  }

  override def onNotFound(request: RequestHeader, message: String): Future[Result] = {
    Future.successful(ErrorNotFound().toHttpResponse)
  }

  override def onServerError(request: RequestHeader, exception: Throwable) = {
    Logger.error("An unexpected error occurred", exception)
    Future.successful(ErrorInternalServerError(exception.getMessage).toHttpResponse)
  }

}