package tapi.stubs

import com.github.tomakehurst.wiremock.client.WireMock._
import models.RequestedAuthority
import play.api.libs.json.Json.toJson
import tapi.MockHost
import models.JsonFormatters._
import play.api.http.Status._

object MockRequestedAuthority extends MockHost(7002) {

  def willReturnRequestedAuthority(requestedAuthority: RequestedAuthority) = {
    mock.register(get(urlPathEqualTo("/authority")).withQueryParam("code", equalTo(requestedAuthority.code.get.code))
      .willReturn(
        aResponse()
          .withStatus(OK)
          .withBody(toJson(requestedAuthority).toString())
      )
    )
  }

  def willDeleteRequestedAuthority(requestedAuthority: RequestedAuthority) = {
    mock.register(delete(urlPathEqualTo(s"/authority/${requestedAuthority.id}"))
      .willReturn(
        aResponse()
          .withStatus(NO_CONTENT)))
  }

}
