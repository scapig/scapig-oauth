package models

import java.util.UUID

import models.Environment.Environment

case class EnvironmentApplication(id: UUID,
                                  name: String,
                                  environment: Environment,
                                  description: String,
                                  redirectUris: Seq[String])

case class AuthenticateRequest(clientId: String, clientSecret: String)
