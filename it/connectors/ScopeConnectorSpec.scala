package connectors

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig
import models._
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach}
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.mvc.Http.Status
import utils.UnitSpec
import models.JsonFormatters._

class ScopeConnectorSpec extends UnitSpec with BeforeAndAfterAll with BeforeAndAfterEach {
  val port = 7003

  val playApplication = new GuiceApplicationBuilder().build()
  val wireMockServer = new WireMockServer(wireMockConfig().port(port))

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
    val scopeConnector = playApplication.injector.instanceOf[ScopeConnector]
  }

  "fetchScopes" should {
    "return the scopes" in new Setup {
      val scope1 = Scope("scope1", "scope1 name", "scope1 desc")
      val scope2 = Scope("scope2", "scope2 name", "scope2 desc")

      stubFor(get(urlPathEqualTo("/scope")).withQueryParam("keys", equalTo("scope1 scope2")).willReturn(aResponse()
        .withStatus(Status.OK)
        .withBody(Json.toJson(Seq(scope1, scope2)).toString())))

      val result = await(scopeConnector.fetchScopes(Seq("scope1", "scope2")))

      result shouldBe Seq(scope1, scope2)
    }

    "throw an exception when error" in new Setup {

      stubFor(get(urlPathEqualTo("/scope")).withQueryParam("keys", equalTo("scope1")).willReturn(aResponse()
        .withStatus(Status.INTERNAL_SERVER_ERROR)))

      intercept[RuntimeException]{await(scopeConnector.fetchScopes(Seq("scope1")))}
    }
  }
}
