package connectors

import javax.inject.Inject

import config.AppContext
import models._
import play.api.libs.json.Json
import play.api.libs.ws.{WSClient, WSResponse}
import models.JsonFormatters._
import play.api.http.Status

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RequestedAuthorityConnector @Inject()(appContext: AppContext, wsClient: WSClient) {

  val serviceUrl = appContext.serviceUrl("requested-authority")

  def createRequestedAuthority(createRequest: CreateRequestedAuthorityRequest): Future[RequestedAuthority] = {
    wsClient.url(s"$serviceUrl/authority").post(Json.toJson(createRequest)) map {
      case response if response.status == 200 => Json.parse(response.body).as[RequestedAuthority]
      case r: WSResponse => throw new RuntimeException(s"Invalid response from requested-authority ${r.status} ${r.body}")
    }
  }

  def completeRequestedAuthority(id: String, completeRequest: CompleteRequestedAuthorityRequest): Future[RequestedAuthority] = {
    wsClient.url(s"$serviceUrl/authority/$id").post(Json.toJson(completeRequest)) map {
      case response if response.status == 200 => Json.parse(response.body).as[RequestedAuthority]
      case response if response.status == 404 => throw RequestedAuthorityNotFound()
      case r: WSResponse => throw new RuntimeException(s"Invalid response from requested-authority ${r.status} ${r.body}")
    }
  }

  def fetchById(id: String): Future[RequestedAuthority] = {
    wsClient.url(s"$serviceUrl/authority/$id").get() map {
      case response if response.status == 200 => Json.parse(response.body).as[RequestedAuthority]
      case response if response.status == 404 => throw RequestedAuthorityNotFound()
      case r: WSResponse => throw new RuntimeException(s"Invalid response from requested-authority ${r.status} ${r.body}")
    }
  }

  def delete(id: String): Future[Unit] = {
    wsClient.url(s"$serviceUrl/authority/$id").delete() map {
      case response if response.status == Status.NO_CONTENT => ()
      case r: WSResponse => throw new RuntimeException(s"Invalid response from requested-authority ${r.status} ${r.body}")
    }
  }

  def fetchByCode(code: String): Future[RequestedAuthority] = {
    wsClient.url(s"$serviceUrl/authority").withQueryStringParameters("code" -> code).get() map {
      case response if response.status == 200 => Json.parse(response.body).as[RequestedAuthority]
      case response if response.status == 404 =>  throw RequestedAuthorityNotFound()
      case r: WSResponse => throw new RuntimeException(s"Invalid response from requested-authority ${r.status} ${r.body}")
    }
  }

}
