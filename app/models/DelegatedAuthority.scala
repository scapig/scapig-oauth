package models

import org.joda.time.DateTime

case class DelegatedAuthorityRequest(clientId: String,
                                     userId: String,
                                     scopes: Seq[String],
                                     environment : Environment.Environment)

object DelegatedAuthorityRequest {
  def apply(requestedAuthority: RequestedAuthority): DelegatedAuthorityRequest = DelegatedAuthorityRequest(requestedAuthority.clientId,
    requestedAuthority.userId.getOrElse(throw new RuntimeException("userId needs to be defined to create delegated authority")),
    requestedAuthority.scopes, requestedAuthority.environment)
}

case class DelegatedAuthorityRefreshRequest(clientId: String,
                                            refreshToken: String)

object DelegatedAuthorityRefreshRequest {
  def apply(refreshRequest: RefreshRequest): DelegatedAuthorityRefreshRequest = DelegatedAuthorityRefreshRequest(refreshRequest.clientId, refreshRequest.refreshToken)
}

object Environment extends Enumeration {
  type Environment = Value
  val PRODUCTION, SANDBOX = Value
}

case class DelegatedAuthority(clientId: String,
                              userId: String,
                              environment: Environment.Environment,
                              token: Token,
                              expiresAt: DateTime)

case class Token(expiresAt: DateTime,
                 scopes: Set[String],
                 accessToken: String,
                 refreshToken: String)

