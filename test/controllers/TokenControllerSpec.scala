package controllers

import models.JsonFormatters._
import models.OAuthErrorCode.INVALID_REQUEST
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

class TokenControllerSpec extends UnitSpec with MockitoSugar with BeforeAndAfterAll {

  val tokenRequest = TokenRequest("clientId", "clientSecret", "https://redirect.com/redirect", "code")
  val tokenRequestBody = Map(
    "client_id" -> tokenRequest.clientId,
    "client_secret" -> tokenRequest.clientSecret,
    "code" -> tokenRequest.code,
    "grant_type" -> "authorization_code",
    "redirect_uri" -> tokenRequest.redirectUri
  )
  val tokenResponse = TokenResponse("access_token", "refresh_token", 14400, "scope1")

  val refreshTokenRequest = RefreshRequest("clientId", "clientSecret", "refreshToken")
  val refreshTokenRequestBody = Map(
    "client_id" -> refreshTokenRequest.clientId,
    "client_secret" -> refreshTokenRequest.clientSecret,
    "grant_type" -> "refresh_token",
    "refresh_token" -> refreshTokenRequest.refreshToken
  )
  val refreshedTokenResponse = TokenResponse("new_access_token", "new_refresh_token", 14400, "scope1")

  trait Setup {
    val mockTokenService: TokenService = mock[TokenService]
    val underTest = new TokenController(Helpers.stubControllerComponents(), mockTokenService)

    val request = FakeRequest()
  }

  "createOrRefreshToken for authorization_code" should {

    "succeed with a 200 with the token" in new Setup {
      given(mockTokenService.createToken(tokenRequest)).willReturn(successful(tokenResponse))

      val result: Result = await(underTest.createOrRefreshToken()(request.withFormUrlEncodedBody(tokenRequestBody.toSeq: _*)))

      status(result) shouldBe Status.OK
      jsonBodyOf(result).as[TokenResponse] shouldBe tokenResponse
    }

    for (field <- Seq("client_id", "client_secret", "grant_type", "code", "redirect_uri")) {
      s"return bad request for missing [$field]" in new Setup {
        val result = await(underTest.createOrRefreshToken()(request.withFormUrlEncodedBody(remove(field)(tokenRequestBody).toSeq: _*)))

        status(result) shouldBe Status.BAD_REQUEST
        jsonBodyOf(result) shouldBe Json.toJson(OAuthError(INVALID_REQUEST, s"$field is required"))
      }
    }

    "return a 401 when the token service fails with OauthUnauthorizedException" in new Setup {
      val oauthError = OAuthError.invalidClientOrSecret
      given(mockTokenService.createToken(tokenRequest)).willReturn(failed(OauthUnauthorizedException(oauthError)))

      val result: Result = await(underTest.createOrRefreshToken()(request.withFormUrlEncodedBody(tokenRequestBody.toSeq: _*)))

      status(result) shouldBe Status.UNAUTHORIZED
      jsonBodyOf(result) shouldBe Json.obj("error" -> "invalid_client", "error_description" -> "invalid client id or secret")
    }

    "return a 400 when the token service fails with OauthValidationException" in new Setup {
      val oauthError = OAuthError.invalidCode
      given(mockTokenService.createToken(tokenRequest)).willReturn(failed(OauthValidationException(oauthError)))

      val result: Result = await(underTest.createOrRefreshToken()(request.withFormUrlEncodedBody(tokenRequestBody.toSeq: _*)))

      status(result) shouldBe Status.BAD_REQUEST
      jsonBodyOf(result) shouldBe Json.obj("error" -> "invalid_request", "error_description" -> "code is invalid")
    }

    "propagate the error when the token service fails with an unexpected exception" in new Setup {
      given(mockTokenService.createToken(tokenRequest)).willReturn(failed(new RuntimeException("test error")))

      intercept[RuntimeException] {
        await(underTest.createOrRefreshToken()(request.withFormUrlEncodedBody(tokenRequestBody.toSeq: _*)))
      }
    }

  }

  "createOrRefreshToken for refresh_token" should {
    "succeed with a 200 with the refreshed token" in new Setup {
      given(mockTokenService.refreshToken(refreshTokenRequest)).willReturn(successful(refreshedTokenResponse))

      val result: Result = await(underTest.createOrRefreshToken()(request.withFormUrlEncodedBody(refreshTokenRequestBody.toSeq: _*)))

      status(result) shouldBe Status.OK
      jsonBodyOf(result).as[TokenResponse] shouldBe refreshedTokenResponse
    }

    for (field <- Seq("client_id", "client_secret", "grant_type", "refresh_token")) {
      s"return bad request for missing [$field]" in new Setup {
        val result = await(underTest.createOrRefreshToken()(request.withFormUrlEncodedBody(remove(field)(refreshTokenRequestBody).toSeq: _*)))

        status(result) shouldBe Status.BAD_REQUEST
        jsonBodyOf(result) shouldBe Json.toJson(OAuthError(INVALID_REQUEST, s"$field is required"))
      }
    }
  }

  private def remove(paramName: String) : PartialFunction[Map[String, String], Map[String, String]] = {
    case m: Map[String, String] => m.filterKeys(_ != paramName)
  }

}
