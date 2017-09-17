package models

import java.util.UUID

import models.Environment.Environment

case class EnvironmentApplication(id: UUID,
                                  name: String,
                                  environment: Environment,
                                  description: String,
                                  applicationUrls: ApplicationUrls)

case class ApplicationUrls(redirectUris: Seq[String])

case class AuthenticateRequest(clientId: String, clientSecret: String)
