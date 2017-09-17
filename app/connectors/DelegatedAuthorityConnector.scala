package connectors

import javax.inject.Inject

import config.AppContext
import models.{DelegatedAuthority, DelegatedAuthorityRequest, TokenResponse}
import play.api.libs.json.Json
import play.api.libs.ws.{WSClient, WSResponse}
import models.JsonFormatters._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class DelegatedAuthorityConnector @Inject()(appContext: AppContext, wsClient: WSClient) {

  val serviceUrl = appContext.serviceUrl("delegated-authority")

  def createToken(delegatedAuthorityRequest: DelegatedAuthorityRequest): Future[TokenResponse] = {
    wsClient.url(s"$serviceUrl/token").post(Json.toJson(delegatedAuthorityRequest)) map {
      case response if response.status == 200 => Json.parse(response.body).as[TokenResponse]
      case r: WSResponse => throw new RuntimeException(s"Invalid response from delegated-authority ${r.status} ${r.body}")
    }
  }
}

