package connectors

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.{aResponse, configureFor, get, stubFor}
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig
import models._
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.mvc.Http.Status
import utils.UnitSpec
import models.JsonFormatters._
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach}

class ApplicationConnectorSpec extends UnitSpec with BeforeAndAfterAll with BeforeAndAfterEach {
  val port = 7000

  val playApplication = new GuiceApplicationBuilder().build()
  val wireMockServer = new WireMockServer(wireMockConfig().port(port))

  val application = Application("appName", ApplicationUrls(Seq("/redirectUris")),
    ApplicationTokens(
      production = EnvironmentToken("prodClientId", "prodServerToken", Seq(ClientSecret("prodClientSecret"))),
      sandbox = EnvironmentToken("sandboxClientId", "sandboxServerToken", Seq(ClientSecret("sandboxClientSecret")))
    ))
  val clientId = application.tokens.production.clientId

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
}
