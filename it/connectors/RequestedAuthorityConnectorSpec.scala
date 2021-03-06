package connectors

import java.util.UUID

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig
import models.{CreateRequestedAuthorityRequest, _}
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach}
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json.toJson
import play.mvc.Http.Status
import utils.UnitSpec
import models.JsonFormatters._

class RequestedAuthorityConnectorSpec extends UnitSpec with BeforeAndAfterAll with BeforeAndAfterEach {
  val port = 7001
  val wireMockServer = new WireMockServer(wireMockConfig().port(port))
  val application = new GuiceApplicationBuilder()
    .configure("services.requested-authority.host" -> "localhost")
    .configure("services.requested-authority.port" -> "7001")
    .build()

  val createRequest = CreateRequestedAuthorityRequest("clientId", Seq("scope1"), "/redirectUri", Environment.PRODUCTION)

  val requestedAuthorityId = UUID.randomUUID()
  val requestedAuthority = RequestedAuthority(requestedAuthorityId, createRequest.clientId, createRequest.scopes, createRequest.redirectUri, createRequest.environment)

  val completeRequest = CompleteRequestedAuthorityRequest(userId = "userId")
  val authorizationCode = "abcde"
  val completedRequestedAuthority = requestedAuthority.copy(userId = Some(completeRequest.userId), authorizationCode = Some(AuthorizationCode(authorizationCode)))

  trait Setup {
    val requestedAuthorityConnector = application.injector.instanceOf[RequestedAuthorityConnector]
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

  "createRequestedAuthority" should {
    "return the requested authority" in new Setup {

      stubFor(post("/authority").withRequestBody(equalToJson(toJson(createRequest).toString())).willReturn(
        aResponse()
          .withStatus(Status.OK)
          .withBody(toJson(requestedAuthority).toString())))

      val result = await(requestedAuthorityConnector.createRequestedAuthority(createRequest))

      result shouldBe requestedAuthority
    }

    "throw an exception when error" in new Setup {

      stubFor(post("/authority").withRequestBody(equalToJson(toJson(createRequest).toString())).willReturn(
        aResponse()
          .withStatus(Status.INTERNAL_SERVER_ERROR)))

      intercept[RuntimeException]{await(requestedAuthorityConnector.createRequestedAuthority(createRequest))}
    }
  }

  "completeRequestedAuthority" should {
    "return the completed requested authority" in new Setup {

      stubFor(post(s"/authority/$requestedAuthorityId").withRequestBody(equalToJson(toJson(completeRequest).toString())).willReturn(
        aResponse()
          .withStatus(Status.OK)
          .withBody(toJson(completedRequestedAuthority).toString())))

      val result = await(requestedAuthorityConnector.completeRequestedAuthority(requestedAuthorityId.toString, completeRequest))

      result shouldBe completedRequestedAuthority
    }

    "throw RequestedAuthorityNotFound when the authority does not exist" in new Setup {

      stubFor(post(s"/authority/$requestedAuthorityId").withRequestBody(equalToJson(toJson(completeRequest).toString())).willReturn(
        aResponse()
          .withStatus(Status.NOT_FOUND)))

      intercept[RequestedAuthorityNotFound]{await(requestedAuthorityConnector.completeRequestedAuthority(requestedAuthorityId.toString, completeRequest))}
    }

    "throw an exception when error" in new Setup {

      stubFor(post(s"/authority/$requestedAuthorityId").withRequestBody(equalToJson(toJson(completeRequest).toString())).willReturn(
        aResponse()
          .withStatus(Status.INTERNAL_SERVER_ERROR)))

      intercept[RuntimeException]{await(requestedAuthorityConnector.completeRequestedAuthority(requestedAuthorityId.toString, completeRequest))}
    }
  }

  "fetchById" should {
    val id = requestedAuthority.id

    "return the requested authority" in new Setup {
      stubFor(get(s"/authority/$id").willReturn(
        aResponse()
          .withStatus(Status.OK)
          .withBody(toJson(requestedAuthority).toString())))

      val result = await(requestedAuthorityConnector.fetchById(id.toString))

      result shouldBe requestedAuthority
    }

    "fail with RequestedAuthorityNotFound when no authority matches" in new Setup {
      stubFor(get(s"/authority/$id").willReturn(
        aResponse()
          .withStatus(Status.NOT_FOUND)))

     intercept[RequestedAuthorityNotFound]{await(requestedAuthorityConnector.fetchById(id.toString))}
    }

    "fail when the request returns an error" in new Setup {
      stubFor(get(s"/authority/$id").willReturn(
        aResponse()
          .withStatus(Status.INTERNAL_SERVER_ERROR)))

      intercept[RuntimeException]{await(requestedAuthorityConnector.fetchById(id.toString))}
    }
  }

  "fetchByCode" should {

    "return the requested authority" in new Setup {
      stubFor(get(urlPathEqualTo(s"/authority")).withQueryParam("code", equalTo(authorizationCode)).willReturn(
        aResponse()
          .withStatus(Status.OK)
          .withBody(toJson(completedRequestedAuthority).toString())))

      val result = await(requestedAuthorityConnector.fetchByCode(authorizationCode))

      result shouldBe completedRequestedAuthority
    }

    "fail with RequestedAuthorityNotFound when authority can not be found" in new Setup {
      stubFor(get(urlPathEqualTo(s"/authority")).withQueryParam("code", equalTo(authorizationCode)).willReturn(
        aResponse()
          .withStatus(Status.NOT_FOUND)))

      intercept[RequestedAuthorityNotFound]{await(requestedAuthorityConnector.fetchByCode(authorizationCode))}
    }

    "fail when the request returns an error" in new Setup {
      stubFor(get(urlPathEqualTo(s"/authority")).withQueryParam("code", equalTo(authorizationCode)).willReturn(
        aResponse()
          .withStatus(Status.INTERNAL_SERVER_ERROR)))

      intercept[RuntimeException]{await(requestedAuthorityConnector.fetchByCode(authorizationCode))}
    }

  }

  "delete" should {

    "delete the requested authority" in new Setup {
      stubFor(delete(urlPathEqualTo(s"/authority/$requestedAuthorityId")).willReturn(
        aResponse()
          .withStatus(Status.NO_CONTENT)))

      val result: Unit = await(requestedAuthorityConnector.delete(requestedAuthorityId.toString))

      result shouldBe ()
    }

    "fail when tapi-requested-authority fails" in new Setup {
      stubFor(delete(urlPathEqualTo(s"/authority/$requestedAuthorityId")).willReturn(
        aResponse()
          .withStatus(Status.INTERNAL_SERVER_ERROR)))

      intercept[RuntimeException] {
        await(requestedAuthorityConnector.delete(requestedAuthorityId.toString))
      }
    }
  }
}
