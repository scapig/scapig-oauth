package tapi.stubs

import com.github.tomakehurst.wiremock.client.WireMock._
import models.JsonFormatters._
import models.Scope
import play.api.http.Status.OK
import play.api.libs.json.Json.toJson
import tapi.MockHost

object MockScope extends MockHost(7003) {

  def willReturnScopes(scopes: Seq[Scope]) = {
    mock.register(get(urlPathEqualTo("/scope")).withQueryParam("keys", equalTo(scopes.map(_.key).mkString(" ")))
      .willReturn(
        aResponse()
          .withStatus(OK)
          .withBody(toJson(scopes).toString())
      )
    )
  }

}
