package connectors

import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import models.{AuthType, AuthorityRequest, DelegatedAuthority, Token}
import org.joda.time.DateTime
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.mvc.Http.Status
import utils.UnitSpec
import models.JsonFormatters._

class DelegatedAuthorityConnectorSpec extends UnitSpec {

  val application = new GuiceApplicationBuilder().build()

  val request = AuthorityRequest("clientId", "userId", Set("scope1"), AuthType.PRODUCTION)
  val token = Token(DateTime.now(), request.scopes, "accessToken", "refreshToken")

  val delegatedAuthority = DelegatedAuthority(request.clientId, request.userId, request.authType, token, DateTime.now())

  trait Setup {
    WireMock.configureFor(7000)

    val delegatedAuthorityConnector = application.injector.instanceOf[DelegatedAuthorityConnector]
  }

  "createAuthority" should {
    "return the authority with the token" in new Setup {

      WireMock.post(Json.toJson(request).toString()).willReturn(aResponse()
        .withStatus(Status.OK)
        .withBody(Json.toJson(delegatedAuthority).toString()))

      val result = await(delegatedAuthorityConnector.createAuthority(request))

      result shouldBe delegatedAuthority
    }

    "throw an exception when error" in new Setup {

      WireMock.post(Json.toJson(request).toString()).willReturn(aResponse()
        .withStatus(Status.INTERNAL_SERVER_ERROR))

      intercept[RuntimeException]{await(delegatedAuthorityConnector.createAuthority(request))}
    }
  }
}
