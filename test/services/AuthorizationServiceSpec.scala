package services

import java.util.UUID

import connectors.{ApplicationConnector, RequestedAuthorityConnector, ScopeConnector}
import models.Environment.PRODUCTION
import models.OAuthErrorCode.{INVALID_CLIENT, INVALID_REQUEST, INVALID_SCOPE}
import models.{CreateRequestedAuthorityRequest, _}
import org.mockito.BDDMockito.given
import org.mockito.Matchers.any
import org.scalatest.mockito.MockitoSugar
import utils.UnitSpec

import scala.concurrent.Future
import scala.concurrent.Future.{failed, successful}

class AuthorizationServiceSpec extends UnitSpec with MockitoSugar {

  val clientId = "clientId"
  val scopes = Seq(Scope("scope1", "Scope 1"))
  val scopeKeys = scopes.map(_.key)
  val redirectUri = "https://domain/path"
  val environmentApplication = EnvironmentApplication(UUID.randomUUID(), "app name", PRODUCTION, "app description", Seq(redirectUri))
  val createRequestedAuthorityRequest = CreateRequestedAuthorityRequest(clientId, scopeKeys, redirectUri, PRODUCTION)
  val requestedAuthority = RequestedAuthority(UUID.randomUUID(), clientId, scopeKeys, redirectUri, PRODUCTION)

  trait Setup {
    val requestedAuthorityConnector = mock[RequestedAuthorityConnector]
    val applicationConnector = mock[ApplicationConnector]
    val scopeConnector = mock[ScopeConnector]

    val underTest = new AuthorizationService(requestedAuthorityConnector, applicationConnector, scopeConnector)

    given(applicationConnector.fetchByClientId(clientId)).willReturn(successful(environmentApplication))
    given(scopeConnector.fetchScopes(scopeKeys)).willReturn(successful(scopes))
    given(requestedAuthorityConnector.createRequestedAuthority(createRequestedAuthorityRequest)).willReturn(successful(requestedAuthority))
  }

  "createRequestedAuthority" should {
    "create and return the requested authority" in new Setup {
      val result = await(underTest.createRequestedAuthority(AuthorizationRequest(clientId, redirectUri, "scope1")))

      result shouldBe requestedAuthority
    }

    "succeeds when the request redirect_uri is contained in the base url" in new Setup {
      val requestRedirectUri = s"$redirectUri/redirect"
      given(requestedAuthorityConnector.createRequestedAuthority(any())).willReturn(successful(requestedAuthority))

      val result = await(underTest.createRequestedAuthority(AuthorizationRequest(clientId, requestRedirectUri, "scope1")))

      result shouldBe requestedAuthority
    }

    "fails with OauthValidationException when no application exists for the clientId" in new Setup {
      given(applicationConnector.fetchByClientId(clientId)).willReturn(failed(new ApplicationNotFound))

      val error = intercept[OauthValidationException]{await(underTest.createRequestedAuthority(AuthorizationRequest(clientId, redirectUri, "scope1")))}

      error.oauthError shouldBe OAuthError(INVALID_REQUEST, "client_id is invalid")
    }

    "fails with OauthValidationException when the redirect uri does not match" in new Setup {
      val invalidRedirectUri = "http://invalid-redirect-uri"

      val error = intercept[OauthValidationException]{await(underTest.createRequestedAuthority(AuthorizationRequest(clientId, invalidRedirectUri, "scope1")))}

      error.oauthError shouldBe OAuthError(INVALID_REQUEST, "redirect_uri is invalid")
    }

    "fails with OauthValidationException when there is no valid scope" in new Setup {
      given(scopeConnector.fetchScopes(scopeKeys)).willReturn(successful(Seq.empty))

      val error = intercept[OauthValidationException]{await(underTest.createRequestedAuthority(AuthorizationRequest(clientId, redirectUri, "scope1")))}

      error.oauthError shouldBe OAuthError(INVALID_SCOPE, "scope is invalid")
    }

    "propagate the exception when the scopeConnector failed" in new Setup {
      given(scopeConnector.fetchScopes(scopeKeys)).willReturn(failed(new RuntimeException("test error")))

      intercept[RuntimeException]{await(underTest.createRequestedAuthority(AuthorizationRequest(clientId, redirectUri, "scope1")))}
    }

    "propagate the exception when the applicationConnector failed" in new Setup {
      given(applicationConnector.fetchByClientId(clientId)).willReturn(failed(new RuntimeException("test error")))

      intercept[RuntimeException]{await(underTest.createRequestedAuthority(AuthorizationRequest(clientId, redirectUri, "scope1")))}
    }

    "propagate the exception when the requestedAuthorityConnector failed" in new Setup {
      given(requestedAuthorityConnector.createRequestedAuthority(createRequestedAuthorityRequest)).willReturn(failed(new RuntimeException("test error")))

      intercept[RuntimeException]{await(underTest.createRequestedAuthority(AuthorizationRequest(clientId, redirectUri, "scope1")))}
    }

  }
}
