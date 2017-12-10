package connectors

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig
import models._
import org.joda.time.DateTime
import play.api.inject.guice.GuiceApplicationBuilder
import play.mvc.Http.Status
import utils.UnitSpec
import models.JsonFormatters._
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach}
import play.api.libs.json.Json
import play.api.libs.json.Json.toJson

class DelegatedAuthorityConnectorSpec extends UnitSpec with BeforeAndAfterAll with BeforeAndAfterEach {
  val port = 7001
  val wireMockServer = new WireMockServer(wireMockConfig().port(port))

  val application = new GuiceApplicationBuilder()
    .configure("services.delegated-authority.host" -> "localhost")
    .configure("services.delegated-authority.port" -> "7001")
    .build()

  val request = DelegatedAuthorityRequest("clientId", "userId", Seq("scope1", "scope2"), Environment.PRODUCTION)
  val tokenResponse = TokenResponse("accessToken", "refreshToken", 14400, "scope1 scope2")

  val refreshRequest = DelegatedAuthorityRefreshRequest("clientId", "refreshToken")

  trait Setup {
    val delegatedAuthorityConnector = application.injector.instanceOf[DelegatedAuthorityConnector]
  }

  override def beforeAll {
    configureFor(port)
    wireMockServer.start()
  }

  override def afterAll: Unit = {
    wireMockServer.stop()
  }

  override def beforeEach(): Unit = {
    WireMock.reset()
  }

  "createToken" should {
    "return the token" in new Setup {

      stubFor(post("/token").withRequestBody(equalToJson(toJson(request).toString())).willReturn(
        aResponse()
          .withStatus(Status.OK)
          .withBody(toJson(tokenResponse).toString())))

      val result = await(delegatedAuthorityConnector.createToken(request))

      result shouldBe tokenResponse
    }

    "throw an exception when error" in new Setup {

      stubFor(post("/token").withRequestBody(equalToJson(toJson(request).toString())).willReturn(
        aResponse()
          .withStatus(Status.INTERNAL_SERVER_ERROR)))

      intercept[RuntimeException]{await(delegatedAuthorityConnector.createToken(request))}
    }
  }

  "refreshToken" should {
    "return the refreshed token" in new Setup {

      stubFor(post("/token/refresh").withRequestBody(equalToJson(toJson(refreshRequest).toString())).willReturn(
        aResponse()
          .withStatus(Status.OK)
          .withBody(toJson(tokenResponse).toString())))

      val result = await(delegatedAuthorityConnector.refreshToken(refreshRequest))

      result shouldBe tokenResponse
    }

    "throw an exception when error" in new Setup {

      stubFor(post("/token/refresh").withRequestBody(equalToJson(toJson(refreshRequest).toString())).willReturn(
        aResponse()
          .withStatus(Status.INTERNAL_SERVER_ERROR)))

      intercept[RuntimeException] {
        await(delegatedAuthorityConnector.refreshToken(refreshRequest))
      }
    }

    "throw an OauthValidationException when the third-party-delegated-authority return 400 with INVALID_REFRESH_TOKEN" in new Setup {

      stubFor(post("/token/refresh").withRequestBody(equalToJson(toJson(refreshRequest).toString())).willReturn(
        aResponse()
          .withBody(Json.obj("code" -> "INVALID_REFRESH_TOKEN", "message" -> "refresh token is invalid").toString())
          .withStatus(Status.BAD_REQUEST)
        ))

      val exception = intercept[OauthValidationException] {
        await(delegatedAuthorityConnector.refreshToken(refreshRequest))
      }
      exception.oauthError shouldBe OAuthError.invalidRefreshToken
    }
  }
}
