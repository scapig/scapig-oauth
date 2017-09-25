package models

import models.OAuthErrorCode.UNSUPPORTED_RESPONSE_TYPE
import play.api.mvc.QueryStringBindable

case class AuthorizationRequest(clientId: String,
                                redirectUri: String,
                                scope: String,
                                state: Option[String] = None,
                                responseType: String = "code") {

  if (!responseType.contains("code")) {
    OauthValidationException(OAuthError(UNSUPPORTED_RESPONSE_TYPE, "response_type must be 'code'", state))
  }
}

object AuthorizationRequest {

  implicit def authorizationRequestBinder = new QueryStringBindable[AuthorizationRequest] {

    override def bind(key: String, params: Map[String, Seq[String]]): Option[Either[String, AuthorizationRequest]] = {
      def extract(param: String): Option[String] = {
        params.get(param).flatMap(_.headOption.map(_.trim))
      }
      def extractRequired(param: String): Either[String, String] = {
        extract(param).toRight(s"$param is required")
      }

      Some(for {
        clientId <- extractRequired("client_id")
        responseType <- extractRequired("response_type")
        scope <- extractRequired("scope")
        redirectUri <- extractRequired("redirect_uri")
      } yield AuthorizationRequest(clientId, redirectUri, scope, extract("state"), responseType))
    }

    override def unbind(key: String, request: AuthorizationRequest): String = {
      def format(key: String, value: String) = s"$key=$value"

      Seq(format("client_id", request.clientId),
        format("scope", request.scope),
        format("response_type", request.responseType),
        format("redirect_uri", request.redirectUri),
        request.state.map(s => format("state", s))).mkString("&")
    }
  }
}