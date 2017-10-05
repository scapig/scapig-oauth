package tapi

import java.util.UUID

import models._
import play.api.http.Status
import play.api.libs.json.Json
import play.mvc.Http.HeaderNames.CONTENT_TYPE
import tapi.stubs.{MockApplication, MockDelegatedAuthority, MockRequestedAuthority}

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
      val createdResponse = Http(s"$serviceUrl/token").postData(requestBody).asString

      Then("I receive a 200 (Ok) with the token")
      createdResponse.code shouldBe Status.OK
      Json.parse(createdResponse.body).as[TokenResponse] shouldBe tokenResponse
    }
  }
}
