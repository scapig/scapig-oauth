package models

import java.util.UUID

import models.AuthType.AuthType

case class EnvironmentApplication(id: UUID,
                                  name: String,
                                  environment: AuthType,
                                  description: String,
                                  applicationUrls: ApplicationUrls)

case class ApplicationUrls(redirectUris: Seq[String])

case class AuthenticateRequest(clientId: String, clientSecret: String)
