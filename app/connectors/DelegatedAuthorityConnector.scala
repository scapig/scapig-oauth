package connectors

import javax.inject.Inject

import config.AppContext
import models.{AuthorityRequest, DelegatedAuthority}
import play.api.libs.json.Json
import play.api.libs.ws.{WSClient, WSResponse}
import models.JsonFormatters._
import scala.concurrent.ExecutionContext.Implicits.global

import scala.concurrent.Future

class DelegatedAuthorityConnector @Inject()(appContext: AppContext, wsClient: WSClient) {

  val serviceUrl = appContext.serviceUrl("delegated-authority")

  def createAuthority(authorityRequest: AuthorityRequest): Future[DelegatedAuthority] = {
    wsClient.url(s"$serviceUrl/authority").post(Json.toJson(authorityRequest)) map {
      case response if response.status == 200 => Json.parse(response.body).as[DelegatedAuthority]
      case r: WSResponse => throw new RuntimeException(s"Invalid response from delegated-authority ${r.status} ${r.body}")
    }
  }
}

