package controllers

import java.net.URLEncoder
import javax.inject.{Inject, Singleton}

import config.AppContext
import models.RequestedAuthorityNotFound
import play.api.mvc.{AbstractController, ControllerComponents, Request, Result}
import services.GrantScopeService

import scala.concurrent.Future.successful
import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class GrantScopeController @Inject()(cc: ControllerComponents, grantScopeService: GrantScopeService, appContext: AppContext) extends AbstractController(cc) with CommonControllers {

  val timedOutTitle = "Session Expired"
  val timedOutHeading = "Your session has ended due to inactivity"
  val timedOutMessage = "Log back in to this service from your accounting software."

  def showGrantScope(reqAuthId: String, state: Option[String]) = Action.async { implicit request =>
    request.session.get("userId") match {
      case Some(userId) => grantScopeService.fetchGrantAuthority(reqAuthId) map { grantAuthority =>
        Ok(views.html.grantscope(grantAuthority, userId, state))
      } recover recovery
      case None => successful(Redirect(fullLoginUrl(reqAuthId, state)))
    }
  }

  private def recovery(implicit request: Request[_]): PartialFunction[Throwable, Result] = {
    case _: RequestedAuthorityNotFound => UnprocessableEntity(views.html.errorTemplate(timedOutTitle, timedOutHeading, timedOutMessage))
  }

  private def fullLoginUrl(reqAuthId: String, state: Option[String]) = {
    val grantScopeUrl = routes.GrantScopeController.showGrantScope(reqAuthId, state).url
    val encodedContinueUrl = URLEncoder.encode(grantScopeUrl, "UTF-8")
    s"${appContext.loginUrl}?continue=$encodedContinueUrl"
  }
}
