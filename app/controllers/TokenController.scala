package controllers

import javax.inject.{Inject, Singleton}

import models.OAuthErrorCode.{INVALID_REQUEST, OAuthErrorCode, SERVER_ERROR}
import models.{OauthUnauthorizedException, OauthValidationException, RefreshRequest, TokenRequest}
import play.api.data.{Form, Forms}
import play.api.data.Forms.{nonEmptyText, tuple}
import play.api.libs.json.Json.JsValueWrapper
import play.api.libs.json.{JsObject, Json}
import play.api.mvc._
import services.TokenService
import models.JsonFormatters._
import play.api.Logger

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.Future.successful

@Singleton
class TokenController  @Inject()(cc: ControllerComponents, tokenService: TokenService) extends AbstractController(cc) with CommonControllers {

  private val grantTypeForm = Form("grant_type" -> nonEmptyText)
  private val createAuthorizationCodeTokenForm = Form(
    tuple(
      "client_id" -> nonEmptyText,
      "client_secret" -> nonEmptyText,
      "redirect_uri" -> nonEmptyText,
      "code" -> nonEmptyText
    )
  )
  private val refreshForm = Form(
    tuple(
      "client_id" -> nonEmptyText,
      "client_secret" -> nonEmptyText,
      "refresh_token" -> nonEmptyText
    )
  )

  def createOrRefreshToken =  Action.async { implicit request =>
    grantTypeForm.bindFromRequest.fold(requiredFieldsError, {
      case "authorization_code" => createOauthToken()
      case "refresh_token" => refreshToken()
    }) recover tokenRecovery
  }

  private def createOauthToken()(implicit request: Request[AnyContent]): Future[Result] = {
    createAuthorizationCodeTokenForm.bindFromRequest.fold(requiredFieldsError, { case (clientId, clientSecret, redirectUri, code) =>
      tokenService.createToken(TokenRequest(clientId, clientSecret, redirectUri, code)) map {
        token => Ok(Json.toJson(token))
      }
    })
  }

  private def refreshToken()(implicit request: Request[AnyContent]): Future[Result] = {
    refreshForm.bindFromRequest.fold(requiredFieldsError, { case (clientId, clientSecret, refreshToken) =>
      tokenService.refreshToken(RefreshRequest(clientId, clientSecret, refreshToken)) map {
        token => Ok(Json.toJson(token))
      }
    })
  }

  private def requiredFieldsError(errorForm: Form[_]): Future[Result] = {
    successful(BadRequest(Json.toJson(error(INVALID_REQUEST,  s"${errorForm.errors.head.key} is required"))))
  }

  private def error(errorCode: OAuthErrorCode, message: JsValueWrapper): JsObject = {
    Json.obj(
      "error" -> errorCode.toString,
      "error_description" -> message
    )
  }

  private def tokenRecovery: PartialFunction[Throwable, Result] = {
    case e: OauthUnauthorizedException => Unauthorized(Json.toJson(e.oauthError))
    case e: OauthValidationException => BadRequest(Json.toJson(e.oauthError))
  }
}
