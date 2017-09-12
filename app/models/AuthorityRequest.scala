package models

import java.util.UUID

import org.joda.time.DateTime

case class AuthorityRequest(clientId: String,
                            userId: String,
                            scopes: Set[String],
                            authType: AuthType.AuthType)

object AuthType extends Enumeration {
  type AuthType = Value
  val PRODUCTION, SANDBOX = Value
}

case class DelegatedAuthority(clientId: String,
                              userId: String,
                              authType: AuthType.AuthType,
                              token: Token,
                              expiresAt: DateTime)

case class Token(expiresAt: DateTime,
                 scopes: Set[String],
                 accessToken: String,
                 refreshToken: String)

