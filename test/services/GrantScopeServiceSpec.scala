package services

import java.util.UUID.randomUUID

import connectors.{ApplicationConnector, RequestedAuthorityConnector, ScopeConnector}
import models.Environment.PRODUCTION
import models._
import org.mockito.BDDMockito.given
import org.scalatest.mockito.MockitoSugar
import utils.UnitSpec

import scala.concurrent.Future.{failed, successful}

class GrantScopeServiceSpec extends UnitSpec with MockitoSugar {

  val requestedAuthority = RequestedAuthority(randomUUID(), "clientId", Seq("scope1"), "http://redirecturi", PRODUCTION)
  val requestedAuthorityId = requestedAuthority.id.toString
  val scopes = Seq(Scope("scope1", "View profile", "View names and address"))
  val application = EnvironmentApplication(randomUUID(), "myApp", PRODUCTION, "app description", ApplicationUrls(Seq("http://redirecturi")))

  val userId = "userId"
  val completedRequestedAuthority = requestedAuthority.copy(userId = Some(userId), authorizationCode = Some(AuthorizationCode("aCode")))

  trait Setup {
    val requestedAuthorityConnector = mock[RequestedAuthorityConnector]
    val scopeConnector = mock[ScopeConnector]
    val applicationConnector = mock[ApplicationConnector]

    val underTest = new GrantScopeService(requestedAuthorityConnector, scopeConnector, applicationConnector)
  }

  "fetchGrantAuthority" should {
    "return the grant authority" in new Setup {
      given(requestedAuthorityConnector.fetchById(requestedAuthorityId)).willReturn(successful(requestedAuthority))
      given(scopeConnector.fetchScopes(requestedAuthority.scopes)).willReturn(successful(scopes))
      given(applicationConnector.fetchByClientId(requestedAuthority.clientId)).willReturn(successful(application))

      val result = await(underTest.fetchGrantAuthority(requestedAuthorityId))

      result shouldBe GrantAuthority(requestedAuthorityId, scopes, application)
    }

    "propagate RequestedAuthorityNotFound when the requested authority do not exist" in new Setup {
      given(requestedAuthorityConnector.fetchById(requestedAuthorityId)).willReturn(failed(RequestedAuthorityNotFound()))

      intercept[RequestedAuthorityNotFound]{await(underTest.fetchGrantAuthority(requestedAuthorityId))}
    }
  }

  "fetchRequestedAuthority" should {
    "return the requested authority" in new Setup {
      given(requestedAuthorityConnector.fetchById(requestedAuthorityId)).willReturn(successful(requestedAuthority))

      val result = await(underTest.fetchRequestedAuthority(requestedAuthorityId))

      result shouldBe requestedAuthority
    }

    "propagate RequestedAuthorityNotFound when the requested authority do not exist" in new Setup {
      given(requestedAuthorityConnector.fetchById(requestedAuthorityId)).willReturn(failed(RequestedAuthorityNotFound()))

      intercept[RequestedAuthorityNotFound]{await(underTest.fetchRequestedAuthority(requestedAuthorityId))}
    }
  }

  "completeRequestedAuthority" should {
    "complete the requested authority" in new Setup {
      given(requestedAuthorityConnector.completeRequestedAuthority(requestedAuthorityId, CompleteRequestedAuthorityRequest(userId)))
        .willReturn(successful(completedRequestedAuthority))

      val result = await(underTest.completeRequestedAuthority(requestedAuthorityId, userId))

      result shouldBe completedRequestedAuthority
    }

    "propagate RequestedAuthorityNotFound exception when the requested authority has expired" in new Setup {
      given(requestedAuthorityConnector.completeRequestedAuthority(requestedAuthorityId, CompleteRequestedAuthorityRequest(userId)))
        .willReturn(failed(RequestedAuthorityNotFound()))

      intercept[RequestedAuthorityNotFound]{await(underTest.completeRequestedAuthority(requestedAuthorityId, userId))}
    }

  }
}
