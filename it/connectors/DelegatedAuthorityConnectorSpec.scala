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

  val application = new GuiceApplicationBuilder().build()

  val request = DelegatedAuthorityRequest("clientId", "userId", Set("scope1"), AuthType.PRODUCTION)
  val token = Token(DateTime.now(), request.scopes, "accessToken", "refreshToken")

  val delegatedAuthority = DelegatedAuthority(request.clientId, request.userId, request.authType, token, DateTime.now())

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

  "createAuthority" should {
    "return the authority with the token" in new Setup {

      stubFor(post("/authority").withRequestBody(equalToJson(toJson(request).toString())).willReturn(
        aResponse()
          .withStatus(Status.OK)
          .withBody(toJson(delegatedAuthority).toString())))

      val result = await(delegatedAuthorityConnector.createAuthority(request))

      result shouldBe delegatedAuthority
    }

    "throw an exception when error" in new Setup {

      stubFor(post("/authority").withRequestBody(equalToJson(toJson(request).toString())).willReturn(
        aResponse()
          .withStatus(Status.INTERNAL_SERVER_ERROR)))

      intercept[RuntimeException]{await(delegatedAuthorityConnector.createAuthority(request))}
    }
  }

}
