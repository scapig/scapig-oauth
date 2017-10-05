package tapi.stubs

import com.github.tomakehurst.wiremock.client.WireMock._
import models.{DelegatedAuthorityRequest, TokenResponse}
import play.api.http.Status.OK
import play.api.libs.json.Json.toJson
import tapi.MockHost
import models.JsonFormatters._

object MockDelegatedAuthority extends MockHost(7002) {

  def willCreateToken(delegatedAuthorityRequest: DelegatedAuthorityRequest, tokenResponse: TokenResponse) = {
    mock.register(post(urlEqualTo(s"/token"))
      .withRequestBody(equalToJson(toJson(delegatedAuthorityRequest).toString()))
      .willReturn(
        aResponse()
          .withStatus(OK)
          .withBody(toJson(tokenResponse).toString())
      )
    )
  }
}
