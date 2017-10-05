package services

import javax.inject.{Inject, Singleton}

import connectors.{ApplicationConnector, RequestedAuthorityConnector, ScopeConnector}
import models.{CompleteRequestedAuthorityRequest, _}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class GrantScopeService @Inject()(requestedAuthorityConnector: RequestedAuthorityConnector,
                                  scopeConnector: ScopeConnector,
                                  applicationConnector: ApplicationConnector) {

  def fetchGrantAuthority(requestedAuthorityId: String): Future[GrantAuthority] = {
    for {
      requestedAuthority <- requestedAuthorityConnector.fetchById(requestedAuthorityId)
      scopes <- scopeConnector.fetchScopes(requestedAuthority.scopes)
      app <- applicationConnector.fetchByClientId(requestedAuthority.clientId)
    } yield GrantAuthority(requestedAuthorityId, scopes, app)

  }

  def fetchRequestedAuthority(requestedAuthorityId: String): Future[RequestedAuthority] = {
    requestedAuthorityConnector.fetchById(requestedAuthorityId)
  }

  def completeRequestedAuthority(requestedAuthorityId: String, userId: String): Future[RequestedAuthority] = {
    requestedAuthorityConnector.completeRequestedAuthority(requestedAuthorityId, CompleteRequestedAuthorityRequest(userId))
  }
}
