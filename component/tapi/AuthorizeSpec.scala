package tapi

import java.util.UUID

import models.Environment.PRODUCTION
import models.JsonFormatters._
import models._
import play.api.http.Status
import play.api.libs.json.Json
import tapi.stubs.{MockApplication, MockDelegatedAuthority, MockRequestedAuthority, MockScope}

import scalaj.http.Http

class AuthorizeSpec extends BaseFeatureSpec {

  val clientId = "aClientId"
  val scope = "aScope"
  val state = "aState"
  val redirectUri = "http://myApp/redirect"
  val scopes = Seq(Scope(scope, "view profile", "view first name, last name and address"))
  val application = EnvironmentApplication(UUID.randomUUID(), "appName", PRODUCTION, "description", ApplicationUrls(Seq("http://myApp")))
  val requestedAuthority = RequestedAuthority(UUID.randomUUID(), clientId, Seq(scope), redirectUri, application.environment)

  feature("authorize") {

    scenario("happy path") {

      Given("an application")
      MockApplication.willReturnApplication(clientId, application)

      And("a scope")
      MockScope.willReturnScopes(scopes)

      And("creation of requested authority succeed")
      MockRequestedAuthority.willCreateRequestedAuthority(CreateRequestedAuthorityRequest(clientId, scopes.map(_.key), redirectUri, PRODUCTION), requestedAuthority)

      When("An authorize request is received")
      val authorizationResponse = Http(s"$serviceUrl/authorize?client_id=$clientId&scope=$scope&response_type=code&redirect_uri=$redirectUri&state=$state").asString

      Then("I receive a 303 (SeeOthers) to the grant page")
      authorizationResponse.code shouldBe Status.SEE_OTHER
      authorizationResponse.headers("Location").head shouldBe s"/grantscope?reqAuthId=${requestedAuthority.id}&state=$state"
    }

    scenario("missing client_id") {
      When("An authorize request is received without client_id")
      val authorizationResponse = Http(s"$serviceUrl/authorize?scope=$scope&response_type=code&redirect_uri=$redirectUri&state=$state").asString

      Then("I receive a 303 (SeeOthers) to the grant page")
      authorizationResponse.code shouldBe Status.BAD_REQUEST
      Json.parse(authorizationResponse.body) shouldBe Json.obj("error" -> "invalid_request", "error_description" -> "client_id is required")
    }

    scenario("missing scope") {
      When("An authorize request is received without scope")
      val authorizationResponse = Http(s"$serviceUrl/authorize?client_id=$clientId&response_type=code&redirect_uri=$redirectUri&state=$state").asString

      Then("I receive a 303 (SeeOthers) to the grant page")
      authorizationResponse.code shouldBe Status.BAD_REQUEST
      Json.parse(authorizationResponse.body) shouldBe Json.obj("error" -> "invalid_request", "error_description" -> "scope is required")
    }

    scenario("missing response_type") {
      When("An authorize request is received without response_type")
      val authorizationResponse = Http(s"$serviceUrl/authorize?client_id=$clientId&scope=$scope&redirect_uri=$redirectUri&state=$state").asString

      Then("I receive a 303 (SeeOthers) to the grant page")
      authorizationResponse.code shouldBe Status.BAD_REQUEST
      Json.parse(authorizationResponse.body) shouldBe Json.obj("error" -> "invalid_request", "error_description" -> "response_type is required")
    }

    scenario("missing redirect_uri") {
      When("An authorize request is received without redirect_uri")
      val authorizationResponse = Http(s"$serviceUrl/authorize?client_id=$clientId&scope=$scope&response_type=code&state=$state").asString

      Then("I receive a 303 (SeeOthers) to the grant page")
      authorizationResponse.code shouldBe Status.BAD_REQUEST
      Json.parse(authorizationResponse.body) shouldBe Json.obj("error" -> "invalid_request", "error_description" -> "redirect_uri is required")
    }

  }
}