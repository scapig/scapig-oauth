package controllers

import java.util.UUID

import config.AppContext
import models._
import org.mockito.BDDMockito.given
import org.mockito.Mockito.when
import org.scalatest.BeforeAndAfterAll
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.http.Status
import play.api.mvc.{Action, AnyContent}
import play.api.test.{FakeRequest, Helpers}
import services.GrantScopeService
import utils.UnitSpec

import scala.concurrent.Future.{failed, successful}

class GrantScopeControllerSpec extends UnitSpec with MockitoSugar with BeforeAndAfterAll with GuiceOneServerPerSuite {

  val application = EnvironmentApplication(UUID.randomUUID(), "myApp", Environment.PRODUCTION, "app description", ApplicationUrls(Seq("/redirectUri")))

  val requestedAuthorityId = UUID.randomUUID().toString
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
}
