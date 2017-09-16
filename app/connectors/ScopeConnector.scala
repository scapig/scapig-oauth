package connectors

import javax.inject.Inject

import config.AppContext
import models.Scope
import play.api.libs.json.Json
import play.api.libs.ws.{WSClient, WSResponse}
import models.JsonFormatters._
import scala.concurrent.ExecutionContext.Implicits.global

import scala.concurrent.Future

class ScopeConnector @Inject()(appContext: AppContext, wsClient: WSClient) {

  val serviceUrl = appContext.serviceUrl("scope")

  def fetchScopes(scopeKeys: Seq[String]): Future[Seq[Scope]] = {
    wsClient.url(s"$serviceUrl/scope?keys=${scopeKeys.mkString(" ")}").get() map {
      case response if response.status == 200 => Json.parse(response.body).as[Seq[Scope]]
      case r: WSResponse => throw new RuntimeException(s"Invalid response from scope ${r.status} ${r.body}")
    }
  }
}
