package tapi.stubs

import com.github.tomakehurst.wiremock.client.WireMock._
import models.{CompleteRequestedAuthorityRequest, CreateRequestedAuthorityRequest, RequestedAuthority}
import play.api.libs.json.Json.toJson
import tapi.MockHost
import models.JsonFormatters._
import play.api.http.Status._
import play.api.libs.json.Json

object MockRequestedAuthority extends MockHost(7003) {

  def willReturnRequestedAuthorityForCode(requestedAuthority: RequestedAuthority) = {
    mock.register(get(urlPathEqualTo("/authority")).withQueryParam("code", equalTo(requestedAuthority.authorizationCode.get.code))
      .willReturn(
        aResponse()
          .withStatus(OK)
          .withBody(toJson(requestedAuthority).toString())
      )
    )
  }

  def willReturnRequestedAuthorityForId(requestedAuthority: RequestedAuthority) = {
    mock.register(get(urlPathEqualTo(s"/authority/${requestedAuthority.id}"))
      .willReturn(
        aResponse()
          .withStatus(OK)
          .withBody(toJson(requestedAuthority).toString())
      )
    )
  }

  def willCreateRequestedAuthority(createRequestedAuthorityRequest: CreateRequestedAuthorityRequest, requestedAuthority: RequestedAuthority) = {
    mock.register(post(urlPathEqualTo(s"/authority")).withRequestBody(equalToJson(Json.toJson(createRequestedAuthorityRequest).toString()))
      .willReturn(
        aResponse()
          .withStatus(OK).withBody(toJson(requestedAuthority).toString())))
  }

  def willDeleteRequestedAuthority(requestedAuthority: RequestedAuthority) = {
    mock.register(delete(urlPathEqualTo(s"/authority/${requestedAuthority.id}"))
      .willReturn(
        aResponse()
          .withStatus(NO_CONTENT)))
  }

  def willUpdateRequestedAuthority(completeRequestedAuthorityRequest: CompleteRequestedAuthorityRequest, requestedAuthority: RequestedAuthority) = {
    mock.register(post(urlPathEqualTo(s"/authority/${requestedAuthority.id}"))
      .withRequestBody(equalToJson(Json.toJson(completeRequestedAuthorityRequest).toString()))
      .willReturn(
        aResponse()
          .withStatus(OK).withBody(Json.toJson(requestedAuthority).toString())))
  }

}
