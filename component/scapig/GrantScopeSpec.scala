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

  val loggedInCookie = "1-fb8f9f87d17c6c106400984db7ec0073408bcca8-1-mnDqHCGUhETPGMqacoUmGHvgLP9i4O7oj3492jOgXqUI0nJd9LEBvMe7j/OE7hFAf1AvKleeZ/T8ZOse4zhYRWBlbGKmehzcbyIobgGrnhA5wdFtSbyvc71GKhqWQqwKUDhm+PZI+U+e4UixL4m4tuGfn/to2vjYeZVtNWY7FiHGzRMH5ner/Lx6XWMSIqzNDdvI6PO2BJUy2si/aAzbOIHwCe8daoOc33oh2oygOH4sUtvmhqGIc3TNEcFPA1Z042BvJWIAqWpHfzDviab/44v3p16M6OkiBcsllDcTvmou11goB3TVFslXl7iBWYIPxJJD7iOX6XGOXeal0T7CKoJZ1uZy444LrUZHa6sacWTvVzD9pLABLZ3zyWvhctycVTeKIKTVPmdHgwe6G2c2CDKI0DpYK6ygAUlraBEUaO9OsWGcqt+KGw8OirarhCSNoSE/fsPBKSpHqsEgNbSvAz8faIjrsPsNROX+sfsDw+TH74URq7LcMfZpbIWCVDbqRItfsD3PZA0ml2ZneygCy3E3YwJX7hwH4Bf5jjf0z62E+1pYrIM999mxuYgH58xM2qmAuk5LpSetvi+dUeH/bNiDmfkVs6gjUAUtB/CMwBwJDGh3lr4OrTBQYuAIS2i+K5yHZEf0Y7SiDV0xdaTvxI4D4/KjYyuHqQhF0eUqPTDKrw=="

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
      val response = Http(s"$serviceUrl/oauth/cancel?reqAuthId=${requestedAuthority.id}&state=$state")
        .postData("")
        .header("Csrf-Token", "nocheck")
        .asString

      Then("I am redirected to the redirect uri with the ACCESS_DENIED error")
      response.code shouldBe Status.FOUND
      response.header("Location") shouldBe Some(s"http://myapp/redirect?error=ACCESS_DENIED&error_description=user+denied+the+authorization&state=$state")
    }

  }

}
