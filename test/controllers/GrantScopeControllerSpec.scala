package controllers

import java.util.UUID

import config.AppContext
import models.Environment.PRODUCTION
import models._
import org.joda.time.DateTime
import org.mockito.BDDMockito.given
import org.mockito.Mockito.when
import org.scalatest.BeforeAndAfterAll
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.http.Status
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent}
import play.api.test.{FakeRequest, Helpers}
import services.GrantScopeService
import utils.UnitSpec

import scala.concurrent.Future.{failed, successful}

class GrantScopeControllerSpec extends UnitSpec with MockitoSugar with BeforeAndAfterAll with GuiceOneServerPerSuite {

  val application = EnvironmentApplication(UUID.randomUUID(), "myApp", PRODUCTION, "app description", ApplicationUrls(Seq("/redirectUri")))

  val authorizationCode = AuthorizationCode("aCode", DateTime.now())
  val userId = "userId"
  val completedRequestedAuthority = RequestedAuthority(UUID.randomUUID(), "clientId", Seq("scope1"), "http://redirectUri", PRODUCTION, Some(authorizationCode), Some(userId))

  val requestedAuthorityId = completedRequestedAuthority.id.toString
  val grantAuthority = GrantAuthority(requestedAuthorityId, Seq(Scope("scope1", "View profile", "View name, address and email")), application)


  trait Setup {
    val grantScopeService = mock[GrantScopeService]
    val appContext = mock[AppContext]

    val underTest = new GrantScopeController(Helpers.stubControllerComponents(), grantScopeService, appContext)

    private val csrfAddToken = app.injector.instanceOf[play.filters.csrf.CSRFAddToken]

    def execute[T <: play.api.mvc.AnyContent](action: Action[AnyContent], request: FakeRequest[T]) = await(csrfAddToken(action)(request))

    val request = FakeRequest()
    when(appContext.loginUrl).thenReturn("http://loginpage")
  }

  "showGrantScope" should {
    "display the grantScope when the user is logged in" in new Setup {
      val authenticatedRequest = request.withSession("userId" -> "john.doe")

      given(grantScopeService.fetchGrantAuthority(requestedAuthorityId)).willReturn(successful(grantAuthority))

      val result = execute(underTest.showGrantScope(requestedAuthorityId, Some("aState")), authenticatedRequest)

      status(result) shouldBe Status.OK
      bodyOf(result) should include ("The <strong>myApp</strong> software application is requesting to do the following")
    }

    "redirect to the login page when the user is not logged in" in new Setup {
      val unauthenticatedRequest = request

      given(grantScopeService.fetchGrantAuthority(requestedAuthorityId)).willReturn(successful(grantAuthority))

      val result = execute(underTest.showGrantScope(requestedAuthorityId, Some("aState")),unauthenticatedRequest)

      status(result) shouldBe Status.SEE_OTHER
      result.header.headers.get("Location") shouldBe Some(s"http://loginpage?continue=%2Fgrantscope%3FreqAuthId%3D$requestedAuthorityId%26state%3DaState")
    }

    "fail with time out when the requested authority does not exist or has expired" in new Setup {
      val authenticatedRequest = request.withSession("userId" -> "john.doe")

      given(grantScopeService.fetchGrantAuthority(requestedAuthorityId)).willReturn(failed(RequestedAuthorityNotFound()))

      val result = execute(underTest.showGrantScope(requestedAuthorityId, Some("aState")), authenticatedRequest)

      status(result) shouldBe Status.UNPROCESSABLE_ENTITY
      bodyOf(result) should include ("Session Expired")
    }

  }

  "acceptGrantScope" should {
    "redirect to the redirectUri with the code" in new Setup {
      val loggedInRequest = request
        .withFormUrlEncodedBody("auth_id" -> requestedAuthorityId, "state" -> "aState")
        .withSession("userId" -> userId)

      given(grantScopeService.completeRequestedAuthority(requestedAuthorityId, userId)).willReturn(successful(completedRequestedAuthority))

      val result = await(underTest.acceptGrantScope()(loggedInRequest))

      status(result) shouldBe Status.SEE_OTHER
      result.header.headers.get("Location") shouldBe Some("http://redirectUri?code=aCode&state=aState")
    }

    "redirect to the login page when the user is not logged in" in new Setup {
      val loggedOutRequest = request.withFormUrlEncodedBody("auth_id" -> requestedAuthorityId, "state" -> "aState")

      val result = await(underTest.acceptGrantScope()(loggedOutRequest))

      status(result) shouldBe Status.SEE_OTHER
      result.header.headers.get("Location") shouldBe Some(s"http://loginpage?continue=%2Fgrantscope%3FreqAuthId%3D$requestedAuthorityId%26state%3DaState")
    }

    "fail with BAD_REQUEST when the auth_id is absent" in new Setup {
      val requestWithoutAuthId = request.withFormUrlEncodedBody()

      val result = await(underTest.acceptGrantScope()(requestWithoutAuthId))

      status(result) shouldBe Status.BAD_REQUEST
    }

    "fail with UNSUPPORTED_MEDIA_TYPE when the body is not form-url-encoded" in new Setup {
      val requestWithBadMediaType = request

      val result = await(underTest.acceptGrantScope()(requestWithBadMediaType))

      status(result) shouldBe Status.UNSUPPORTED_MEDIA_TYPE
    }

    "fail with UNPROCESSABLE_ENTITY when the requested authority is invalid or has timed out" in new Setup {
      val loggedInRequest = request
        .withFormUrlEncodedBody("auth_id" -> requestedAuthorityId, "state" -> "aState")
        .withSession("userId" -> userId)

      given(grantScopeService.completeRequestedAuthority(requestedAuthorityId, userId)).willReturn(failed(RequestedAuthorityNotFound()))

      val result = await(underTest.acceptGrantScope()(loggedInRequest))

      status(result) shouldBe Status.UNPROCESSABLE_ENTITY
      bodyOf(result) should include ("Session Expired")
    }
  }
}
