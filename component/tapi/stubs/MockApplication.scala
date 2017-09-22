package tapi.stubs

import com.github.tomakehurst.wiremock.client.WireMock._
import models.{AuthenticateRequest, EnvironmentApplication}
import play.api.http.Status.OK
import play.api.libs.json.Json.toJson
import tapi.MockHost
import models.JsonFormatters._

object MockApplication extends MockHost(7000) {

  def willAuthenticateSucceed(clientId: String, clientSecret: String, result: EnvironmentApplication) = {
    mock.register(post(urlEqualTo(s"/application/authenticate"))
      .withRequestBody(equalToJson(toJson(AuthenticateRequest(clientId, clientSecret)).toString()))
      .willReturn(
        aResponse()
          .withStatus(OK)
          .withBody(toJson(result).toString())
      )
    )
  }
}
