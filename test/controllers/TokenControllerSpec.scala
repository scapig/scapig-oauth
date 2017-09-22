package controllers

import models.JsonFormatters._
import models._
import org.mockito.BDDMockito.given
import org.scalatest.BeforeAndAfterAll
import org.scalatest.mockito.MockitoSugar
import play.api.http.Status
import play.api.libs.json.Json
import play.api.mvc.Result
import play.api.test.{FakeRequest, Helpers}
import services.TokenService
import utils.UnitSpec

import scala.concurrent.Future.{failed, successful}

class TokenControllerSpec extends UnitSpec with MockitoSugar with BeforeAndAfterAll  {

  val tokenRequest = TokenRequest("clientId", "clientSecret", "https://redirect.com/redirect", "code")
  val requestBody = Map(
    "client_id" -> tokenRequest.clientId,
    "client_secret" -> tokenRequest.clientSecret,
    "code" -> tokenRequest.code,
    "grant_type" -> "authorization_code",
    "redirect_uri" -> tokenRequest.redirectUri
  )
  val tokenResponse = TokenResponse("access_token", "refresh_token", 14400, "scope1")

  trait Setup {
    val mockTokenService: TokenService = mock[TokenService]
    val underTest = new TokenController(Helpers.stubControllerComponents(), mockTokenService)

    val request = FakeRequest()
  }

  "createToken" should {

    "succeed with a 200 with the token" in new Setup {
      given(mockTokenService.createToken(tokenRequest)).willReturn(successful(tokenResponse))

      val result: Result = await(underTest.createToken()(request.withFormUrlEncodedBody(requestBody.toSeq: _*)))

      status(result) shouldBe Status.OK
      jsonBodyOf(result).as[TokenResponse] shouldBe tokenResponse
    }

    "return a 401 when the token service fails with OauthUnauthorizedException" in new Setup {
      val oauthError = OAuthError.invalidClientOrSecret
      given(mockTokenService.createToken(tokenRequest)).willReturn(failed(OauthUnauthorizedException(oauthError)))

      val result: Result = await(underTest.createToken()(request.withFormUrlEncodedBody(requestBody.toSeq: _*)))

      status(result) shouldBe Status.UNAUTHORIZED
      jsonBodyOf(result) shouldBe Json.obj("error" -> "invalid_client", "error_description" -> "invalid client id or secret")
    }

    "return a 400 when the token service fails with OauthValidationException" in new Setup {
      val oauthError = OAuthError.invalidCode
      given(mockTokenService.createToken(tokenRequest)).willReturn(failed(OauthValidationException(oauthError)))

      val result: Result = await(underTest.createToken()(request.withFormUrlEncodedBody(requestBody.toSeq: _*)))

      status(result) shouldBe Status.BAD_REQUEST
      jsonBodyOf(result) shouldBe Json.obj("error" -> "invalid_request", "error_description" -> "code is invalid")
    }

    "propagate the error when the token service fails with an unexpected exception" in new Setup {
      given(mockTokenService.createToken(tokenRequest)).willReturn(failed(new RuntimeException("test error")))

      intercept[RuntimeException]{await(underTest.createToken()(request.withFormUrlEncodedBody(requestBody.toSeq: _*)))}
   }

  }
}
