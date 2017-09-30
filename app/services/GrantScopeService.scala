package services

import javax.inject.{Inject, Singleton}

import models.GrantAuthority

import scala.concurrent.Future

@Singleton
class GrantScopeService @Inject()() {

  def fetchGrantAuthority(requestedAuthorityId: String): Future[GrantAuthority] = {
    ???
  }

}
