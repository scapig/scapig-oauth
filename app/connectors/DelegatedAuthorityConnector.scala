package connectors

import javax.inject.Inject

import config.AppContext
import models._
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

  def refreshToken(refreshRequest: DelegatedAuthorityRefreshRequest): Future[TokenResponse] = {
    wsClient.url(s"$serviceUrl/token/refresh").post(Json.toJson(refreshRequest)) map {
      case response if response.status == 200 => Json.parse(response.body).as[TokenResponse]
      case response if response.status == 400 && errorCode(response.body).contains("INVALID_REFRESH_TOKEN") => throw OauthValidationException(OAuthError.invalidRefreshToken)
      case r: WSResponse => throw new RuntimeException(s"Invalid response from delegated-authority ${r.status} ${r.body}")
    }
  }

  private def errorCode(body: String) = (Json.parse(body) \ "code").asOpt[String]
}

