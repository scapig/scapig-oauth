package services

import javax.inject.{Inject, Singleton}

import connectors.{ApplicationConnector, RequestedAuthorityConnector, ScopeConnector}
import models.{OAuthError, OauthUnauthorizedException, TokenRequest, TokenResponse}

import scala.concurrent.Future

@Singleton
class TokenService @Inject()(requestedAuthorityConnector: RequestedAuthorityConnector,
                             applicationConnector: ApplicationConnector,
                             scopeConnector: ScopeConnector) {

  def createToken(tokenRequest: TokenRequest): Future[TokenResponse] = {
    ???
  }
}
