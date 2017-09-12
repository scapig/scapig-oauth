package controllers

import javax.inject.{Inject, Singleton}

import play.api.mvc.{AbstractController, ControllerComponents}
import services.TokenService

@Singleton
class TokenController  @Inject()(cc: ControllerComponents, tokenService: TokenService) extends AbstractController(cc) with CommonControllers {

}
