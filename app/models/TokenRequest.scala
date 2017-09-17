package models

import org.joda.time.{DateTime, Days, Seconds}

case class TokenRequest(clientId: String, clientSecret: String, redirectUri: String, code: String)

case class TokenResponse(access_token: String,
                         refresh_token: String,
                         expires_in: Int,
                         scope: String,
                         token_type: String = "bearer")
