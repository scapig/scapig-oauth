package scapig

import java.util.UUID

import models.Environment.PRODUCTION
import models._
import play.api.http.Status
import scapig.stubs.{MockApplication, MockRequestedAuthority, MockScope}

import scalaj.http.Http

class GrantScopeSpec extends BaseFeatureSpec {

  val clientId = "clientId"
  val scope = "aScope"
  val state = "aState"
  val userId = "user1"
  val redirectUri = "http://myApp/redirect"
  val authorizationCode = "aCode"
  val scopes = Seq(Scope(scope, "view profile"))
  val application = EnvironmentApplication(UUID.randomUUID(), "appName", PRODUCTION, "description", Seq("http://myApp"))
  val requestedAuthority = RequestedAuthority(UUID.randomUUID(), clientId, Seq(scope), redirectUri, application.environment)
  val completedRequestedAuthority = requestedAuthority.copy(userId = Some(userId), authorizationCode = Some(AuthorizationCode(authorizationCode)))

  //Cookie created from scapig-oauth-login with large expiry
  val loggedInCookie = "1-bd453e36d042af87157f64188f0179b9fe9786e5-1-4zpqqX/XceFZr9yfYOgDbCitH1RIrCLXGWgkgQjkCtCXPwb/KLm+4/G1gfjrJaTnh7H0Z1NC/NmcLkRQNtR6xWchdnuwsuRefA87cR89TvqMQNAhk7mOVtPHTVmQTMpXoeWGjUvANSNUWEE3M8SoAyG+5sFaOT7moGuqeOZBnEKi5HE0xeepFIrZybfaPKUhP1kIoZtf95hhd4pD09O+KVoFu0WJ61pctvCedfsN2lw6JDQ25Twvr6nK/l2rEguU7ylwxnktPwyDun4ISkR9YJa3I1TJECne2Lh4S7ujQ6OiD3RZJlQIFCqvzycJi6vzmk4yoNt34R92qmNPSXsyouLGjMlk8ZMpvXUx0vuT1NXSDb8n3xGsu7605AGrWlE1ianOSnEYYNBnVR+4mcu2Duau/1ZqpQmnEl4GVm0OCPNHEQghdnDtAxLnOmYl8zdYqSYrLTwpFAnZplEC4DoBUmIVhASeuDPDM8wiLTEGscnhUgUBd62YAMRkgBeZGlqQcXUaMLA3+yA4unXrH3wcyYQNqECRi8I8qeKXbOMw2/l7JfleY4eEy+iumlP9JgJnkpHyK49A+htHUkLABM+OvFaAOnoQ4LtSbPSFCtkjVxXkq8b/IAE8dVAMerVuGh953wvenIt5Hc9HUZbQRlYVwnY0FkFhQAh9TRIxk/v2Qzvdyu4ZjIJm"

  feature("show grantscope") {

    scenario("logged in user") {

      Given("an application")
      MockApplication.willReturnApplication(clientId, application)

      And("a scope")
      MockScope.willReturnScopes(scopes)

      And("a requested authority")
      MockRequestedAuthority.willReturnRequestedAuthorityForId(requestedAuthority)

      And("I am logged in")
      val cookie = loggedInCookie

      When("I request the grant scope")
      val response = Http(s"$serviceUrl/oauth/grantscope?reqAuthId=${requestedAuthority.id}&state=$state")
        .cookie("authenticator", cookie)
        .asString

      Then("I receive a 200 (Ok) with the grant page")
      response.code shouldBe Status.OK
      response.body should include ("Grant Scope")
    }

    scenario("logged out user") {

      Given("an application")
      MockApplication.willReturnApplication(clientId, application)

      And("a scope")
      MockScope.willReturnScopes(scopes)

      And("a requested authority")
      MockRequestedAuthority.willReturnRequestedAuthorityForId(requestedAuthority)

      And("I am logged out")

      When("I request the grant scope")
      val response = Http(s"$serviceUrl/oauth/grantscope?reqAuthId=${requestedAuthority.id}&state=$state").asString

      Then("I am redirected to the login page")
      response.code shouldBe Status.SEE_OTHER
      response.header("Location") shouldBe Some(s"http://localhost:15000/login?continue=http%3A%2F%2Flocalhost%3A14680%2Foauth%2Fgrantscope%3FreqAuthId%3D${requestedAuthority.id}%26state%3D$state")
    }

  }

  feature("accept grantscope") {

    scenario("logged in user") {
      Given("a requested authority")
      MockRequestedAuthority.willReturnRequestedAuthorityForId(requestedAuthority)

      And("updating requested authority will succeed")
      MockRequestedAuthority.willUpdateRequestedAuthority(CompleteRequestedAuthorityRequest(userId), completedRequestedAuthority)

      And("I am logged in")
      val cookie = loggedInCookie

      When("I accept to grant authority")
      val response = Http(s"$serviceUrl/oauth/grantscope")
        .postForm(Seq(("reqAuthId", requestedAuthority.id.toString), ("state", state)))
        .cookie("authenticator", cookie)
        .header("Csrf-Token", "nocheck")
        .asString

      Then("I am redirected to the redirect uri with the code")
      response.code shouldBe Status.SEE_OTHER
      response.header("Location") shouldBe Some(s"http://myapp/redirect?code=$authorizationCode&state=$state")

    }

    scenario("logged out user") {
      Given("a requested authority")
      MockRequestedAuthority.willReturnRequestedAuthorityForId(requestedAuthority)

      And("updating requested authority will succeed")
      MockRequestedAuthority.willUpdateRequestedAuthority(CompleteRequestedAuthorityRequest(userId), completedRequestedAuthority)

      And("I am logged out")

      When("I accept to grant authority")
      val response = Http(s"$serviceUrl/oauth/grantscope")
        .postForm(Seq(("reqAuthId", requestedAuthority.id.toString), ("state", state)))
        .header("Csrf-Token", "nocheck")
        .asString

      Then("I am redirected to the login page")
      response.code shouldBe Status.SEE_OTHER
      response.header("Location") shouldBe Some(s"http://localhost:15000/login?continue=http%3A%2F%2Flocalhost%3A14680%2Foauth%2Fgrantscope%3FreqAuthId%3D${requestedAuthority.id}%26state%3D$state")

    }

  }

  feature("cancel grantscope") {
    scenario("logged in user") {
      Given("a requested authority")
      MockRequestedAuthority.willReturnRequestedAuthorityForId(requestedAuthority)

      When("I cancel to grant authority")
      val response = Http(s"$serviceUrl/oauth/cancel?reqAuthId=${requestedAuthority.id}&state=$state").asString

      Then("I am redirected to the redirect uri with the ACCESS_DENIED error")
      response.code shouldBe Status.FOUND
      response.header("Location") shouldBe Some(s"http://myapp/redirect?error=ACCESS_DENIED&error_description=user+denied+the+authorization&state=$state")
    }

  }

}
