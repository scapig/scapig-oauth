package models

case class Application(name: String,
                       applicationUrls: ApplicationUrls,
                       tokens: ApplicationTokens)

case class ApplicationUrls(redirectUris: Seq[String])

case class ApplicationTokens(production: EnvironmentToken,
                             sandbox: EnvironmentToken)

case class EnvironmentToken(clientId: String,
                            serverToken: String,
                            clientSecrets: Seq[ClientSecret])

case class ClientSecret(secret: String)
