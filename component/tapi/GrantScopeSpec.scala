package tapi

import java.util.UUID

import models.Environment.PRODUCTION
import models._
import play.api.http.Status
import play.api.mvc.Session
import tapi.stubs.{MockApplication, MockRequestedAuthority, MockScope}

import scalaj.http.Http

class GrantScopeSpec extends BaseFeatureSpec {

  val clientId = "clientId"
  val scope = "aScope"
  val state = "aState"
  val userId = "aUser"
  val redirectUri = "http://myApp/redirect"
  val scopes = Seq(Scope(scope, "view profile", "view first name, last name and address"))
  val application = EnvironmentApplication(UUID.randomUUID(), "appName", PRODUCTION, "description", ApplicationUrls(Seq("http://myApp")))
  val requestedAuthority = RequestedAuthority(UUID.randomUUID(), clientId, Seq(scope), redirectUri, application.environment)
  val completedRequestedAuthority = requestedAuthority.copy(userId = Some(userId), code = Some(AuthorizationCode("aCode")))

  feature("show grantscope") {

    scenario("logged in user") {

      Given("an application")
      MockApplication.willReturnApplication(clientId, application)

      And("a scope")
      MockScope.willReturnScopes(scopes)

      And("a requested authority")
      MockRequestedAuthority.willReturnRequestedAuthorityForId(requestedAuthority)

      And("I am logged in")
      val cookie = Session.encodeAsCookie(Session(Map("userId" -> userId)))

      When("I request the grant scope")
      val response = Http(s"$serviceUrl/grantscope?reqAuthId=${requestedAuthority.id}&state=$state")
        .cookie("PLAY_SESSION", cookie.value)
        .asString

      Then("I receive a 200 (Ok) with the grant page")
      response.code shouldBe Status.OK
      response.body should include ("Authority to interact on your behalf")
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
      val response = Http(s"$serviceUrl/grantscope?reqAuthId=${requestedAuthority.id}&state=$state").asString

      Then("I am redirected to the login page")
      response.code shouldBe Status.SEE_OTHER
      response.header("Location") shouldBe Some(s"http://localhost:8000/login?continue=%2Fgrantscope%3FreqAuthId%3D${requestedAuthority.id}%26state%3D$state")
    }

  }

  feature("accept grantscope") {

    scenario("logged in user") {
      Given("a requested authority")
      MockRequestedAuthority.willReturnRequestedAuthorityForId(requestedAuthority)

      And("updating requested authority will succeed")
      MockRequestedAuthority.willUpdateRequestedAuthority(CompleteRequestedAuthorityRequest(userId), completedRequestedAuthority)

      And("I am logged in")
      val cookie = Session.encodeAsCookie(Session(Map("userId" -> userId)))

      When("I accept to grant authority")
      val response = Http(s"$serviceUrl/grantscope")
        .postForm(Seq(("reqAuthId", requestedAuthority.id.toString), ("state", state)))
        .cookie("PLAY_SESSION", cookie.value)
        .asString

      Then("I am redirected to the redirect uri with the code")
      response.code shouldBe Status.SEE_OTHER
      response.header("Location") shouldBe Some(s"")

    }

    scenario("logged out user") {
      Given("a requested authority")
      MockRequestedAuthority.willReturnRequestedAuthorityForId(requestedAuthority)

      And("updating requested authority will succeed")
      MockRequestedAuthority.willUpdateRequestedAuthority(CompleteRequestedAuthorityRequest(userId), completedRequestedAuthority)

      And("I am logged out")

      When("I accept to grant authority")
      val response = Http(s"$serviceUrl/grantscope")
        .postForm(Seq(("reqAuthId", requestedAuthority.id.toString), ("state", state)))
        .asString

      Then("I am redirected to the login page")
      response.code shouldBe Status.SEE_OTHER
      response.header("Location") shouldBe Some(s"http://localhost:8000/login?continue=%2Fgrantscope%3FreqAuthId%3D${requestedAuthority.id}%26state%3D$state")

    }

  }
}
