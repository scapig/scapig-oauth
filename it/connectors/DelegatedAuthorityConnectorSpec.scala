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
import play.api.libs.json.Json.toJson

class DelegatedAuthorityConnectorSpec extends UnitSpec with BeforeAndAfterAll with BeforeAndAfterEach {
  val port = 7001
  val wireMockServer = new WireMockServer(wireMockConfig().port(port))

  val application = new GuiceApplicationBuilder()
    .configure("services.delegated-authority.port" -> "7001")
    .build()

  val request = DelegatedAuthorityRequest("clientId", "userId", Seq("scope1", "scope2"), Environment.PRODUCTION)
  val tokenResponse = TokenResponse("accessToken", "refreshToken", 14400, "scope1 scope2")

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

}
