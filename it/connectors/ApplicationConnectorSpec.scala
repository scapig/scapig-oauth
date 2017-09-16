package connectors

import java.util.UUID

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig
import models._
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.mvc.Http.Status
import utils.UnitSpec
import models.JsonFormatters._
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach}
import play.api.libs.json.Json.toJson

class ApplicationConnectorSpec extends UnitSpec with BeforeAndAfterAll with BeforeAndAfterEach {
  val port = 7000

  val playApplication = new GuiceApplicationBuilder().build()
  val wireMockServer = new WireMockServer(wireMockConfig().port(port))

  val clientId = "aClientId"
  val clientSecret = "aClientSecret"
  val authenticateRequest = AuthenticateRequest(clientId, clientSecret)
  val application = EnvironmentApplication(UUID.randomUUID(), "appName", AuthType.PRODUCTION, "description", ApplicationUrls(Seq("/redirectUris")))

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

  trait Setup {
    val applicationConnector = playApplication.injector.instanceOf[ApplicationConnector]
  }

  "fetchByClientId" should {
    "return the application" in new Setup {

      stubFor(get(s"/application?clientId=$clientId").willReturn(aResponse()
        .withStatus(Status.OK)
        .withBody(Json.toJson(application).toString())))

      val result = await(applicationConnector.fetchByClientId(clientId))

      result shouldBe Some(application)
    }

    "return None when the clientId does not match any application" in new Setup {

      stubFor(get(s"/application?clientId=$clientId").willReturn(aResponse()
        .withStatus(Status.NOT_FOUND)))

      val result = await(applicationConnector.fetchByClientId(clientId))

      result shouldBe None
    }

    "throw an exception when error" in new Setup {

      stubFor(get(s"/application?clientId=$clientId").willReturn(aResponse()
        .withStatus(Status.INTERNAL_SERVER_ERROR)))

      intercept[RuntimeException]{await(applicationConnector.fetchByClientId(clientId))}
    }
  }

  "authenticate" should {
    "return the application when credentials are valid" in new Setup {

      stubFor(post("/application/authenticate").withRequestBody(equalToJson(toJson(authenticateRequest).toString()))
        .willReturn(aResponse()
        .withStatus(Status.OK)
        .withBody(Json.toJson(application).toString())))

      val result = await(applicationConnector.authenticate(clientId, clientSecret))

      result shouldBe Some(application)
    }

    "return None when the clientId does not match any application" in new Setup {

      stubFor(post("/application/authenticate").withRequestBody(equalToJson(toJson(authenticateRequest).toString()))
        .willReturn(aResponse()
        .withStatus(Status.UNAUTHORIZED)))

      val result = await(applicationConnector.authenticate(clientId, clientSecret))

      result shouldBe None
    }

    "throw an exception when error" in new Setup {

      stubFor(post("/application/authenticate").withRequestBody(equalToJson(toJson(authenticateRequest).toString()))
        .willReturn(aResponse()
        .withStatus(Status.INTERNAL_SERVER_ERROR)))

      intercept[RuntimeException]{await(applicationConnector.authenticate(clientId, clientSecret))}
    }
  }
}