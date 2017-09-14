package connectors

import javax.inject.Inject

import config.AppContext
import models.Application
import play.api.libs.json.Json
import play.api.libs.ws.{WSClient, WSResponse}
import models.JsonFormatters._
import scala.concurrent.ExecutionContext.Implicits.global

import scala.concurrent.Future

class ApplicationConnector @Inject()(appContext: AppContext, wsClient: WSClient) {

  val serviceUrl = appContext.serviceUrl("application")

  def fetchByClientId(clientId: String): Future[Option[Application]] = {
    wsClient.url(s"$serviceUrl/application?clientId=$clientId").get() map {
      case response if response.status == 200 => Json.parse(response.body).asOpt[Application]
      case response if response.status == 404 => None
      case r: WSResponse => throw new RuntimeException(s"Invalid response from application ${r.status} ${r.body}")
    }
  }
}

