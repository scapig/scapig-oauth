package models

import java.util.UUID

import org.joda.time.DateTime

case class CreateRequestedAuthorityRequest(clientId: String,
                                           scopes: Seq[String],
                                           redirectUri: String,
                                           environment: Environment.Environment)


case class CompleteRequestedAuthorityRequest(userId: String)

case class RequestedAuthority(id: UUID,
                              clientId: String,
                              scopes: Seq[String],
                              redirectUri: String,
                              environment: Environment.Environment,
                              code: Option[AuthorizationCode] = None,
                              userId: Option[String] = None)

case class AuthorizationCode(code: String,
                             createdAt: DateTime = DateTime.now())
