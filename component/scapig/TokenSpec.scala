package scapig

import java.util.UUID

import models._
import play.api.http.Status
import play.api.libs.json.Json
import play.mvc.Http.HeaderNames.CONTENT_TYPE
import scapig.stubs.{MockApplication, MockDelegatedAuthority, MockRequestedAuthority}

import scalaj.http.Http
import models.JsonFormatters._

class TokenSpec extends BaseFeatureSpec {

  val clientId = "aClientId"
  val clientSecret = "aClientSecret"
  val application = EnvironmentApplication(UUID.randomUUID(), "appName", Environment.PRODUCTION, "description", ApplicationUrls(Seq("/redirectUris")))
  val requestedAuthority = RequestedAuthority(UUID.randomUUID(), clientId, Seq("scope1"), application.applicationUrls.redirectUris.head, application.environment,
    Some(AuthorizationCode("aCode")), Some("aUserId"))
  val delegatedAuthorityRequest = DelegatedAuthorityRequest(requestedAuthority)
  val tokenResponse = TokenResponse("accessToken", "refreshToken", 14400, "scope1")

  val refreshToken = "refreshToken"
  val refreshTokenResponse = TokenResponse("newAccessToken", "newRefreshToken", 14400, "scope1")
  val delegatedAuthorityRefreshRequest = DelegatedAuthorityRefreshRequest(clientId, refreshToken)

  feature("create token") {
    val requestBody = s"" +
      s"client_id=$clientId" +
      s"&client_secret=$clientSecret" +
      s"&code=${requestedAuthority.authorizationCode.get.code}" +
      s"&grant_type=authorization_code" +
      s"&redirect_uri=${requestedAuthority.redirectUri}"

    scenario("happy path") {

      Given("an application")
      MockApplication.willAuthenticateSucceed(clientId, clientSecret, application)

      And("a requested authority")
      MockRequestedAuthority.willReturnRequestedAuthorityForCode(requestedAuthority)

      And("creation of delegated authority succeed")
      MockDelegatedAuthority.willCreateToken(delegatedAuthorityRequest, tokenResponse)

      And("deletion of requested authority succeed")
      MockRequestedAuthority.willDeleteRequestedAuthority(requestedAuthority)

      When("A token request is received")
      val createdResponse = Http(s"$serviceUrl/oauth/token").postData(requestBody).asString

      Then("I receive a 200 (Ok) with the token")
      createdResponse.code shouldBe Status.OK
      Json.parse(createdResponse.body).as[TokenResponse] shouldBe tokenResponse
    }
  }

  feature("refresh token") {
    val requestBody = s"" +
      s"client_id=$clientId" +
      s"&client_secret=$clientSecret" +
      s"&grant_type=refresh_token" +
      s"&refresh_token=$refreshToken"

    scenario("happy path") {
      Given("an application")
      MockApplication.willAuthenticateSucceed(clientId, clientSecret, application)

      And("refresh of delegated authority succeed")
      MockDelegatedAuthority.willRefreshToken(delegatedAuthorityRefreshRequest, refreshTokenResponse)

      When("A token refresh request is received")
      val createdResponse = Http(s"$serviceUrl/oauth/token").postData(requestBody).asString

      Then("I receive a 200 (Ok) with the token")
      createdResponse.code shouldBe Status.OK
      Json.parse(createdResponse.body).as[TokenResponse] shouldBe refreshTokenResponse
    }
  }
}
