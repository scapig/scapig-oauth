package controllers

import java.util.UUID.randomUUID

import models.Environment.PRODUCTION
import models._
import org.mockito.BDDMockito.given
import org.scalatest.BeforeAndAfterAll
import org.scalatest.mockito.MockitoSugar
import play.api.http.Status
import play.api.libs.json.Json
import play.api.test.{FakeRequest, Helpers}
import services.AuthorizationService
import utils.UnitSpec

import scala.concurrent.Future.{failed, successful}

class AuthorizationControllerSpec extends UnitSpec with MockitoSugar with BeforeAndAfterAll  {

  val authorizationRequest = AuthorizationRequest("clientId", "/redirectUri", "scope1", Some("aState"))
  val requestedAuthority = RequestedAuthority(randomUUID(), authorizationRequest.clientId, Seq(authorizationRequest.scope), authorizationRequest.redirectUri, PRODUCTION)

  trait Setup {
    val authorizationService = mock[AuthorizationService]
    val underTest = new AuthorizationController(Helpers.stubControllerComponents(), authorizationService)

    val request = FakeRequest()
  }

  "authorize" should {
    "create the requested authority and redirect to the grant page" in new Setup {
      given(authorizationService.createRequestedAuthority(authorizationRequest)).willReturn(successful(requestedAuthority))

      val result = await(underTest.authorize(authorizationRequest)(request))

      status(result) shouldBe Status.SEE_OTHER
      result.header.headers.get("Location") shouldBe Some(s"/grantscope?reqAuthId=${requestedAuthority.id}&state=aState")
    }

    "fail with a 400 when authorizationService fails with OauthValidationException" in new Setup {
      val error = OAuthError(OAuthErrorCode.INVALID_CLIENT, "invalid client id or secret", Some("aState"))

      given(authorizationService.createRequestedAuthority(authorizationRequest)).willReturn(failed(OauthValidationException(error)))

      val result = await(underTest.authorize(authorizationRequest)(request))

      status(result) shouldBe Status.BAD_REQUEST
      jsonBodyOf(result) shouldBe Json.obj("error" -> "invalid_client", "error_description" -> "invalid client id or secret", "state" -> "aState")
    }

    "fail with a 500 when authorizationService fails" in new Setup {
      given(authorizationService.createRequestedAuthority(authorizationRequest)).willReturn(failed(new RuntimeException("test error")))

      val result = await(underTest.authorize(authorizationRequest)(request))

      status(result) shouldBe Status.INTERNAL_SERVER_ERROR
      jsonBodyOf(result) shouldBe Json.obj("error" -> "server_error", "error_description" -> "unexpected error occurred", "state" -> "aState")
    }

  }
}
