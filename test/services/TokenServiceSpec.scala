package services

import java.util.UUID

import connectors.{ApplicationConnector, DelegatedAuthorityConnector, RequestedAuthorityConnector, ScopeConnector}
import models._
import org.mockito.BDDMockito.given
import org.mockito.Mockito.verify
import org.scalatest.mockito.MockitoSugar
import utils.UnitSpec

import scala.concurrent.Future.{failed, successful}

class TokenServiceSpec extends UnitSpec with MockitoSugar {

  val tokenRequest = TokenRequest("clientId", "clientSecret", "/redirect", "aCode")
  val tokenResponse = TokenResponse("access_token", "refresh_token", 14400, "scope1")

  val environmentApplication = EnvironmentApplication(UUID.randomUUID(), "appName", Environment.PRODUCTION,
    "appDescription", ApplicationUrls(Seq("/redirectUri")))

  val requestedAuthority = RequestedAuthority(UUID.randomUUID(), tokenRequest.clientId, Seq("scope1"), tokenRequest.redirectUri,
    environmentApplication.environment, Some(AuthorizationCode(tokenRequest.code)), Some("userId"))

  val refreshRequest = RefreshRequest("clientId", "clientSecret", "refreshToken")
  val refreshTokenResponse =  TokenResponse("new_access_token", "new_refresh_token", 14400, "scope1")

  trait Setup {
    val requestedAuthorityConnector: RequestedAuthorityConnector = mock[RequestedAuthorityConnector]
    val delegatedAuthorityConnector: DelegatedAuthorityConnector = mock[DelegatedAuthorityConnector]
    val applicationConnector: ApplicationConnector = mock[ApplicationConnector]
    val scopeConnector: ScopeConnector = mock[ScopeConnector]

    val underTest = new TokenService(requestedAuthorityConnector, delegatedAuthorityConnector, applicationConnector, scopeConnector)

    given(applicationConnector.authenticate(tokenRequest.clientId, tokenRequest.clientSecret))
      .willReturn(successful(environmentApplication))
    given(requestedAuthorityConnector.fetchByCode(tokenRequest.code)).willReturn(successful(requestedAuthority))
    given(delegatedAuthorityConnector.createToken(DelegatedAuthorityRequest(requestedAuthority))).willReturn(successful(tokenResponse))
    given(requestedAuthorityConnector.delete(requestedAuthority.id.toString)).willReturn(successful())
    given(delegatedAuthorityConnector.refreshToken(DelegatedAuthorityRefreshRequest(refreshRequest))).willReturn(successful(refreshTokenResponse))

  }

  "createToken" should {

    "fail with OauthUnauthorizedException when clientId or clientSecret is invalid" in new Setup {
      given(applicationConnector.authenticate(tokenRequest.clientId, tokenRequest.clientSecret))
        .willReturn(failed(new ApplicationNotFound))

      val exception = intercept[OauthUnauthorizedException]{await(underTest.createToken(tokenRequest))}

      exception.oauthError shouldBe OAuthError.invalidClientOrSecret
    }

    "fail with OauthValidationException when code is invalid" in new Setup {
      given(requestedAuthorityConnector.fetchByCode(tokenRequest.code)).willReturn(failed(new RequestedAuthorityNotFound))

      val exception = intercept[OauthValidationException]{await(underTest.createToken(tokenRequest))}

      exception.oauthError shouldBe OAuthError.invalidCode
    }

    "fail with OauthUnauthorizedException when clientId in requested authority is different that the one in the token request" in new Setup {
      val requestedAuthorityWithDifferentClientId = requestedAuthority.copy(clientId = "other")

      given(requestedAuthorityConnector.fetchByCode(tokenRequest.code)).willReturn(successful(requestedAuthorityWithDifferentClientId))

      val exception = intercept[OauthUnauthorizedException]{await(underTest.createToken(tokenRequest))}

      exception.oauthError shouldBe OAuthError.invalidClientOrSecret
    }

    "fail with OauthUnauthorizedException when redirectUri in requested authority is different that the one in the token request" in new Setup {

      val requestedAuthorityWithDifferentRedirectUri = requestedAuthority.copy(redirectUri = "/otherredirecturi")
      given(requestedAuthorityConnector.fetchByCode(tokenRequest.code)).willReturn(successful(requestedAuthorityWithDifferentRedirectUri))

      val exception = intercept[OauthValidationException]{await(underTest.createToken(tokenRequest))}

      exception.oauthError shouldBe OAuthError.invalidRedirectUri
    }

    "create the delegated authority and delete the requested authority" in new Setup {

      val result = await(underTest.createToken(tokenRequest))

      result shouldBe tokenResponse
      verify(requestedAuthorityConnector).delete(requestedAuthority.id.toString)
    }

    "ignore when the requested authority deletion failed" in new Setup {
      given(requestedAuthorityConnector.delete(requestedAuthority.id.toString)).willReturn(failed(new RuntimeException("test error")))

      val result = await(underTest.createToken(tokenRequest))

      result shouldBe tokenResponse
    }

    "fail when the deleted authority creation failed" in new Setup {
      given(delegatedAuthorityConnector.createToken(DelegatedAuthorityRequest(requestedAuthority))).willReturn(failed(new RuntimeException("test error")))

      intercept[RuntimeException]{await(underTest.createToken(tokenRequest))}
    }

  }

  "refreshToken" should {

    "return the refreshedToken" in new Setup {
      val result = await(underTest.refreshToken(refreshRequest))

      result shouldBe refreshTokenResponse
    }

    "fail with OauthUnauthorizedException when clientId or clientSecret is invalid" in new Setup {
      given(applicationConnector.authenticate(refreshRequest.clientId, refreshRequest.clientSecret))
        .willReturn(failed(new ApplicationNotFound))

      val exception = intercept[OauthUnauthorizedException] {
        await(underTest.refreshToken(refreshRequest))
      }

      exception.oauthError shouldBe OAuthError.invalidClientOrSecret
    }

    "propagate OauthValidationException" in new Setup {
      val delegatedAuthorityRefreshRequest = DelegatedAuthorityRefreshRequest(refreshRequest)

      given(delegatedAuthorityConnector.refreshToken(delegatedAuthorityRefreshRequest)).willReturn(failed(new OauthValidationException(OAuthError.unauthorizedClient)))

      val exception = intercept[OauthValidationException] {
        await(underTest.refreshToken(refreshRequest))
      }
    }
  }
}
