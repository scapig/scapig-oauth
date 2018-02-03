package controllers

import java.util.UUID

import com.google.inject.{AbstractModule, TypeLiteral}
import com.mohiva.play.silhouette.api.{Environment, LoginInfo}
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider
import com.mohiva.play.silhouette.test.{FakeEnvironment, _}
import config.{AppContext, DefaultEnv}
import models.Environment.PRODUCTION
import models._
import org.joda.time.DateTime
import org.mockito.BDDMockito.given
import org.mockito.Mockito.when
import org.scalatest.BeforeAndAfterAll
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.http.Status
import play.api.inject
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.Result
import play.api.test.CSRFTokenHelper.addCSRFToken
import play.api.test.{CSRFTokenHelper, FakeRequest}
import services.GrantScopeService
import utils.UnitSpec

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future.{failed, successful}

class GrantScopeControllerSpec extends UnitSpec with MockitoSugar with BeforeAndAfterAll with GuiceOneServerPerSuite {

  val application = EnvironmentApplication(UUID.randomUUID(), "myApp", PRODUCTION, "app description", Seq("/redirectUri"))

  val authorizationCode = AuthorizationCode("aCode", DateTime.now())
  val userId = "userId"
  val requestedAuthority = RequestedAuthority(UUID.randomUUID(), "clientId", Seq("scope1"), "http://redirectUri", PRODUCTION, Some(authorizationCode), Some(userId))
  val completedRequestedAuthority = requestedAuthority.copy(authorizationCode = Some(authorizationCode), userId = Some(userId))

  val requestedAuthorityId = completedRequestedAuthority.id.toString
  val grantAuthority = GrantAuthority(requestedAuthorityId, Seq(Scope("scope1", "View profile")), application)

  val user = User(userId)
  val loginInfo = LoginInfo(CredentialsProvider.ID, userId)
  implicit val env: FakeEnvironment[DefaultEnv] = FakeEnvironment[DefaultEnv](Seq(loginInfo -> user))

  class FakeModule extends AbstractModule {
    def configure(): Unit = {
      bind(new TypeLiteral[Environment[DefaultEnv]]{}).toInstance(env)
    }
  }

  trait Setup {
    val grantScopeService = mock[GrantScopeService]
    val appContext = mock[AppContext]

    val playApplication = new GuiceApplicationBuilder()
      .overrides(new FakeModule())
      .bindings(inject.bind[GrantScopeService].to(grantScopeService))
      .bindings(inject.bind[AppContext].to(appContext))
      .build()

    val underTest = playApplication.injector.instanceOf[GrantScopeController]

    when(appContext.loginUrl).thenReturn("http://loginpage")
    when(appContext.oauthUrl).thenReturn("http://oauthpage")

    val authenticatedRequest = FakeRequest().withAuthenticator[DefaultEnv](loginInfo)
    val unauthenticatedRequest = FakeRequest()
  }

  "showGrantScope" should {
    "display the grantScope when the user is logged in" in new Setup {
      given(grantScopeService.fetchGrantAuthority(requestedAuthorityId)).willReturn(successful(grantAuthority))

      val result = await(underTest.showGrantScope(requestedAuthorityId, Some("aState"))(addCSRFToken(authenticatedRequest)))

      status(result) shouldBe Status.OK
      bodyOf(result) should include("The <strong>myApp</strong> software application is requesting to do the following")
    }

    "redirect to the login page when the user is not logged in" in new Setup {

      given(grantScopeService.fetchGrantAuthority(requestedAuthorityId)).willReturn(successful(grantAuthority))

      val result = await(underTest.showGrantScope(requestedAuthorityId, Some("aState"))(unauthenticatedRequest))

      status(result) shouldBe Status.SEE_OTHER
      result.header.headers.get("Location") shouldBe Some(s"http://loginpage?continue=http%3A%2F%2Foauthpage%2Foauth%2Fgrantscope%3FreqAuthId%3D$requestedAuthorityId%26state%3DaState")
    }

    "fail with time out when the requested authority does not exist or has expired" in new Setup {
      given(grantScopeService.fetchGrantAuthority(requestedAuthorityId)).willReturn(failed(RequestedAuthorityNotFound()))

      val result = await(underTest.showGrantScope(requestedAuthorityId, Some("aState"))(authenticatedRequest))

      status(result) shouldBe Status.UNPROCESSABLE_ENTITY
      bodyOf(result) should include("Session Expired")
    }

  }

  "acceptGrantScope" should {
    "redirect to the redirectUri with the code" in new Setup {
      val loggedInRequest = authenticatedRequest.withFormUrlEncodedBody("reqAuthId" -> requestedAuthorityId, "state" -> "aState")

      given(grantScopeService.completeRequestedAuthority(requestedAuthorityId, userId)).willReturn(successful(completedRequestedAuthority))

      val result = await(underTest.acceptGrantScope()(loggedInRequest))

      status(result) shouldBe Status.SEE_OTHER
      result.header.headers.get("Location") shouldBe Some("http://redirectUri?code=aCode&state=aState")
    }

    "redirect to the login page when the user is not logged in" in new Setup {
      val loggedOutRequest = unauthenticatedRequest.withFormUrlEncodedBody("reqAuthId" -> requestedAuthorityId, "state" -> "aState")

      val result = await(underTest.acceptGrantScope()(loggedOutRequest))

      status(result) shouldBe Status.SEE_OTHER
      result.header.headers.get("Location") shouldBe Some(s"http://loginpage?continue=http%3A%2F%2Foauthpage%2Foauth%2Fgrantscope%3FreqAuthId%3D$requestedAuthorityId%26state%3DaState")
    }

    "fail with BAD_REQUEST when the reqAuthId is absent" in new Setup {
      val requestWithoutAuthId = authenticatedRequest.withFormUrlEncodedBody()

      val result = await(underTest.acceptGrantScope()(requestWithoutAuthId))

      status(result) shouldBe Status.BAD_REQUEST
    }

    "fail with UNSUPPORTED_MEDIA_TYPE when the body is not form-url-encoded" in new Setup {
      val requestWithBadMediaType = authenticatedRequest

      val result = await(underTest.acceptGrantScope()(requestWithBadMediaType))

      status(result) shouldBe Status.UNSUPPORTED_MEDIA_TYPE
    }

    "fail with UNPROCESSABLE_ENTITY when the requested authority is invalid or has timed out" in new Setup {
      val loggedInRequest = authenticatedRequest.withFormUrlEncodedBody("reqAuthId" -> requestedAuthorityId, "state" -> "aState")

      given(grantScopeService.completeRequestedAuthority(requestedAuthorityId, userId)).willReturn(failed(RequestedAuthorityNotFound()))

      val result = await(underTest.acceptGrantScope()(loggedInRequest))

      status(result) shouldBe Status.UNPROCESSABLE_ENTITY
      bodyOf(result) should include("Session Expired")
    }
  }

  "cancel" should {
    "redirect to the redirectUri with an ACCESS_DENIED code and log out" in new Setup {
      val loggedInRequest = authenticatedRequest
        .withFormUrlEncodedBody("reqAuthId" -> requestedAuthorityId, "state" -> "aState")

      given(grantScopeService.fetchRequestedAuthority(requestedAuthorityId)).willReturn(successful(requestedAuthority))

      val result = await(underTest.cancel(requestedAuthorityId, Some("aState"))(loggedInRequest))

      status(result) shouldBe Status.FOUND
      result.header.headers.get("Location") shouldBe Some("http://redirectUri?error=ACCESS_DENIED&error_description=user+denied+the+authorization&state=aState")
      verifyUserLoggedOut(result)
    }

    "fail with UNPROCESSABLE_ENTITY when the requested authority is invalid or has timed out" in new Setup {
      val loggedInRequest = authenticatedRequest
        .withFormUrlEncodedBody("reqAuthId" -> requestedAuthorityId, "state" -> "aState")

      given(grantScopeService.fetchRequestedAuthority(requestedAuthorityId)).willReturn(failed(RequestedAuthorityNotFound()))

      val result = await(underTest.cancel(requestedAuthorityId, Some("aState"))(loggedInRequest))

      status(result) shouldBe Status.UNPROCESSABLE_ENTITY
      bodyOf(result) should include("Session Expired")
    }

  }

  private def verifyUserLoggedOut(result: Result) = {
    result.newCookies.map(_.name) shouldBe Seq("PLAY_SESSION", "authenticator")
    result.newCookies.map(_.maxAge) shouldBe Seq(Some(-86400), Some(-86400))
  }

}