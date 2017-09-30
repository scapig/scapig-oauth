package controllers

import javax.inject.{Inject, Singleton}

import models.OAuthErrorCode.SERVER_ERROR
import models.{AuthorizationRequest, OAuthError, OauthValidationException}
import play.api.Logger
import play.api.libs.json.Json.toJson
import play.api.mvc.{AbstractController, Action, ControllerComponents}
import services.{AuthorizationService, TokenService}
import scala.concurrent.ExecutionContext.Implicits.global
import models.JsonFormatters._

@Singleton
class AuthorizationController @Inject()(cc: ControllerComponents, authorizationService: AuthorizationService) extends AbstractController(cc) with CommonControllers {

  def authorize(authorizationRequest: AuthorizationRequest) = Action.async { implicit request =>
    authorizationService.createRequestedAuthority(authorizationRequest)
      .map(reqAuth =>
        Redirect(routes.GrantScopeController.showGrantScope(reqAuth.id.toString, authorizationRequest.state).url))
      .recover {
        case e: OauthValidationException => BadRequest(toJson(e.oauthError))
        case e: Throwable =>
          Logger.error("An unexpected error happened", e)
          InternalServerError(toJson(OAuthError(SERVER_ERROR, "unexpected error occurred", authorizationRequest.state)))
      }
  }

}
