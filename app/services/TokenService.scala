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
    for {
      environmentApplication <- applicationConnector.authenticate(tokenRequest.clientId, tokenRequest.clientSecret)
      requestedAuthority <- requestedAuthorityConnector.fetchByCode(tokenRequest.code)
      delegatedAuthority <- delegatedAuthorityConnector.createToken(DelegatedAuthorityRequest(requestedAuthority))
    } yield delegatedAuthority
  }
}
