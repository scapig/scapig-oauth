package services

import javax.inject.{Inject, Singleton}

import connectors.{ApplicationConnector, DelegatedAuthorityConnector, RequestedAuthorityConnector, ScopeConnector}
import models._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class TokenService @Inject()(requestedAuthorityConnector: RequestedAuthorityConnector,
                             delegatedAuthorityConnector: DelegatedAuthorityConnector,
                             applicationConnector: ApplicationConnector,
                             scopeConnector: ScopeConnector) {

  def createToken(tokenRequest: TokenRequest): Future[TokenResponse] = {
    val future = for {
      _ <- applicationConnector.authenticate(tokenRequest.clientId, tokenRequest.clientSecret)
      requestedAuthority <- requestedAuthorityConnector.fetchByCode(tokenRequest.code)
      _ = if (requestedAuthority.clientId.trim != tokenRequest.clientId) throw OauthUnauthorizedException(OAuthError.invalidClientOrSecret)
      _ = if (requestedAuthority.redirectUri != tokenRequest.redirectUri) throw OauthValidationException(OAuthError.invalidRedirectUri)
      token <- delegatedAuthorityConnector.createToken(DelegatedAuthorityRequest(requestedAuthority))
      _ = requestedAuthorityConnector.delete(requestedAuthority.id.toString)
    } yield token

    future recover {
      case _: ApplicationNotFound => throw OauthUnauthorizedException(OAuthError.invalidClientOrSecret)
      case _: RequestedAuthorityNotFound => throw OauthValidationException(OAuthError.invalidCode)
    }
  }

  def refreshToken(refreshRequest: RefreshRequest): Future[TokenResponse] = {
    val future = for {
      _ <- applicationConnector.authenticate(refreshRequest.clientId, refreshRequest.clientSecret)
      token <- delegatedAuthorityConnector.refreshToken(DelegatedAuthorityRefreshRequest(refreshRequest))
    } yield token

    future recover {
      case _: ApplicationNotFound => throw OauthUnauthorizedException(OAuthError.invalidClientOrSecret)
    }
  }
}
