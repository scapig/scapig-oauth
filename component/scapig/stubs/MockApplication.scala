package scapig.stubs

import com.github.tomakehurst.wiremock.client.WireMock._
import models.{AuthenticateRequest, EnvironmentApplication}
import play.api.http.Status.OK
import play.api.libs.json.Json.toJson
import scapig.MockHost
import models.JsonFormatters._

object MockApplication extends MockHost(7001) {

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

  def willReturnApplication(clientId: String, application: EnvironmentApplication) = {
    mock.register(get(urlPathEqualTo("/application")).withQueryParam("clientId", equalTo(clientId))
      .willReturn(
        aResponse()
          .withStatus(OK)
          .withBody(toJson(application).toString())
      )
    )
  }

}
