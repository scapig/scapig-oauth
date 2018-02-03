package controllers

import java.net.URLEncoder
import javax.inject.{Inject, Singleton}

import com.mohiva.play.silhouette.api.Silhouette
import config.{AppContext, DefaultEnv}
import models.RequestedAuthorityNotFound
import org.webjars.play.WebJarsUtil
import play.api.mvc._
import services.GrantScopeService

import scala.concurrent.Future.successful
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class GrantScopeController @Inject()(cc: ControllerComponents,
                                     grantScopeService: GrantScopeService,
                                     appContext: AppContext,
                                     silhouette: Silhouette[DefaultEnv])
                                    (implicit webJarsUtil: WebJarsUtil)
  extends AbstractController(cc) with CommonControllers {

  val timedOutTitle = "Session Expired"
  val timedOutHeading = "Your session has ended due to inactivity"
  val timedOutMessage = "Log back in to this service from your accounting software."

  def showGrantScope(reqAuthId: String, state: Option[String]) = silhouette.UserAwareAction.async { implicit request =>
    request.identity match {
      case Some(user) => grantScopeService.fetchGrantAuthority(reqAuthId) map { grantAuthority =>
        Ok(views.html.grantscope(grantAuthority, user.userId, state))
      } recover recovery
      case None => successful(Redirect(fullLoginUrl(reqAuthId, state)))
    }
  }

  def acceptGrantScope() = silhouette.UserAwareAction.async { implicit request =>
    def completeRequestedAuthority(requestedAuthorityId: String, userId: String, state: Option[String]) = {
      grantScopeService.completeRequestedAuthority(requestedAuthorityId, userId) map { requestedAuthority =>
        val params = Map("code" -> requestedAuthority.authorizationCode.map(_.code).toSeq) ++ addState(state)
        Redirect(requestedAuthority.redirectUri, params)
      } recover recovery
    }

    def accept = { form: Map[String, Seq[String]]  =>
      val state = form.get("state").flatMap(_.headOption)

      (form.get("reqAuthId").map(_.head), request.identity.map(_.userId)) match {
        case (Some(reqAuthId), Some(userId)) => completeRequestedAuthority(reqAuthId, userId, state)
        case (Some(reqAuthId), _) => successful(Redirect(fullLoginUrl(reqAuthId, state)))
        case (_, _) => Future.successful(BadRequest(views.html.errorTemplate("", "", "Authority request id not found.")))
      }
    }

    request.body.asFormUrlEncoded.fold(
      successful(UnsupportedMediaType(s"expected: application/x-www-form-urlencoded but received: ${request.contentType}")))(
      accept)
  }

  def cancel(reqAuthId: String, state: Option[String]) = Action.async { implicit request =>
    (for {
      requestedAuthority <- grantScopeService.fetchRequestedAuthority(reqAuthId)
      stateParam = state.map(s => s"&state=$s").getOrElse("")
      cancelUri = s"${requestedAuthority.redirectUri}?error=ACCESS_DENIED&error_description=user+denied+the+authorization$stateParam"
    } yield Found(cancelUri).discardingCookies(DiscardingCookie("PLAY_SESSION"), DiscardingCookie("authenticator"))) recover recovery
  }

  private def recovery(implicit request: Request[_]): PartialFunction[Throwable, Result] = {
    case _: RequestedAuthorityNotFound => UnprocessableEntity(views.html.errorTemplate(timedOutTitle, timedOutHeading, timedOutMessage))
  }

  private def addState(maybeState: Option[String]) = maybeState.map { value => Map("state" -> Seq(value)) }.getOrElse(Map.empty)

  private def fullLoginUrl(reqAuthId: String, state: Option[String]) = {
    val grantScopeUrl = routes.GrantScopeController.showGrantScope(reqAuthId, state).url
    val encodedContinueUrl = URLEncoder.encode(appContext.oauthUrl + grantScopeUrl, "UTF-8")
    s"${appContext.loginUrl}?continue=$encodedContinueUrl"
  }
}
