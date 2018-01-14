package services

import javax.inject.{Inject, Singleton}

import connectors.{ApplicationConnector, DelegatedAuthorityConnector, RequestedAuthorityConnector, ScopeConnector}
import models._
import models.OAuthErrorCode.{INVALID_REQUEST, INVALID_SCOPE}
import utils.UriUtils.urisMatch
import scala.concurrent.ExecutionContext.Implicits.global

import scala.concurrent.Future

@Singleton
class AuthorizationService @Inject()(requestedAuthorityConnector: RequestedAuthorityConnector,
                                     applicationConnector: ApplicationConnector,
                                     scopeConnector: ScopeConnector) {

  def createRequestedAuthority(authorityRequest: AuthorizationRequest): Future[RequestedAuthority] = {
    val appFuture = applicationConnector.fetchByClientId(authorityRequest.clientId)
    val scopeFuture = scopeConnector.fetchScopes(authorityRequest.scope.split("\\s+"))

    (for {
      app <- appFuture
      scopes <- scopeFuture
      _ = validateRedirectUri(app.redirectUris, authorityRequest.redirectUri, authorityRequest.state)
      _ = if(scopes.isEmpty) throw OauthValidationException(OAuthError(INVALID_SCOPE, "scope is invalid", authorityRequest.state))
      createAuthorityRequest = CreateRequestedAuthorityRequest(authorityRequest.clientId, scopes.map(_.key), authorityRequest.redirectUri,app.environment)
      requestedAuthority <- requestedAuthorityConnector.createRequestedAuthority(createAuthorityRequest)
    } yield requestedAuthority) recover {
      case _: ApplicationNotFound => throw OauthValidationException(OAuthError(INVALID_REQUEST, "client_id is invalid", authorityRequest.state))
    }
  }

  private def validateRedirectUri(appRedirectUris: Seq[String], reqRedirectUri: String, state: Option[String] = None): Unit = {
    if (!appRedirectUris.exists(urisMatch(reqRedirectUri))) throw OauthValidationException(OAuthError(INVALID_REQUEST, "redirect_uri is invalid", state))
  }

}
