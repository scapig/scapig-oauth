package models

case class TokenRequest(clientId: String, clientSecret: String, redirectUri: String, code: String)

case class RefreshRequest(clientId: String, clientSecret: String, refreshToken: String)

case class TokenResponse(access_token: String,
                         refresh_token: String,
                         expires_in: Int,
                         scope: String,
                         token_type: String = "bearer")
