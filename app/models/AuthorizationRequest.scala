package models

import play.api.mvc.QueryStringBindable

case class AuthorizationRequest(clientId: String,
                                redirectUri: String,
                                scope: String,
                                state: Option[String] = None)

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
        _ = if(!responseType.equals("code")) Left("response_type must be 'code'") else Right()
        scope <- extractRequired("scope")
        redirectUri <- extractRequired("redirect_uri")
      } yield AuthorizationRequest(clientId, redirectUri, scope, extract("state")))
    }

    override def unbind(key: String, request: AuthorizationRequest): String = {
      def format(key: String, value: String) = s"$key=$value"

      Seq(format("client_id", request.clientId),
        format("scope", request.scope),
        format("response_type", "code"),
        format("redirect_uri", request.redirectUri),
        request.state.map(s => format("state", s))).mkString("&")
    }
  }
}