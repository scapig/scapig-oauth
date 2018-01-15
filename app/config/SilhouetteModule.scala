package config

import javax.inject.Inject

import com.google.inject.{AbstractModule, Provides, TypeLiteral}
import com.mohiva.play.silhouette.api._
import com.mohiva.play.silhouette.api.actions.{SecuredErrorHandler, UnsecuredErrorHandler}
import com.mohiva.play.silhouette.api.crypto._
import com.mohiva.play.silhouette.api.services._
import com.mohiva.play.silhouette.api.util._
import com.mohiva.play.silhouette.crypto.{JcaCrypter, JcaCrypterSettings, JcaSigner, JcaSignerSettings}
import com.mohiva.play.silhouette.impl.authenticators._
import com.mohiva.play.silhouette.impl.util._
import controllers.routes
import models.User
import net.ceedubs.ficus.Ficus._
import net.ceedubs.ficus.readers.ArbitraryTypeReader._
import play.api.Configuration
import play.api.mvc.{CookieHeaderEncoding, Request, RequestHeader, Results}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.Future.successful

class SilhouetteModule extends AbstractModule {

  def configure() {
    bind(new TypeLiteral[Silhouette[DefaultEnv]]{}).to(new TypeLiteral[SilhouetteProvider[DefaultEnv]]{})
    bind(classOf[IDGenerator]).toInstance(new SecureRandomIDGenerator())
    bind(classOf[FingerprintGenerator]).toInstance(new DefaultFingerprintGenerator(false))
    bind(classOf[EventBus]).toInstance(EventBus())
    bind(classOf[Clock]).toInstance(Clock())

    bind(classOf[UnsecuredErrorHandler]).to(classOf[CustomUnsecuredErrorHandler])
    bind(classOf[SecuredErrorHandler]).to(classOf[CustomSecuredErrorHandler])
  }

  @Provides
  def provideEnvironment(
    userService: UserServiceImpl,
    authenticatorService: AuthenticatorService[CookieAuthenticator],
    eventBus: EventBus): Environment[DefaultEnv] = {

    Environment[DefaultEnv](
      userService,
      authenticatorService,
      Seq(),
      eventBus
    )
  }

  @Provides
  def provideAuthenticatorSigner(configuration: Configuration): Signer = {
    val config = configuration.underlying.as[JcaSignerSettings]("silhouette.authenticator.signer")
    new JcaSigner(config)
  }

  @Provides
  def provideAuthenticatorCrypter(configuration: Configuration): Crypter = {
    val config = configuration.underlying.as[JcaCrypterSettings]("silhouette.authenticator.crypter")
    new JcaCrypter(config)
  }

  @Provides
  def provideAuthenticatorService(
    signer: Signer,
    crypter: Crypter,
    cookieHeaderEncoding: CookieHeaderEncoding,
    fingerprintGenerator: FingerprintGenerator,
    idGenerator: IDGenerator,
    configuration: Configuration,
    clock: Clock): AuthenticatorService[CookieAuthenticator] = {
    val config = configuration.underlying.as[CookieAuthenticatorSettings]("silhouette.authenticator")
    val authenticatorEncoder = new CrypterAuthenticatorEncoder(crypter)

    new CookieAuthenticatorService(config, None, signer, cookieHeaderEncoding, authenticatorEncoder, fingerprintGenerator, idGenerator, clock)
  }

}


trait DefaultEnv extends Env {
  type I = User
  type A = CookieAuthenticator
}

class CustomUnsecuredErrorHandler extends UnsecuredErrorHandler {
  override def onNotAuthorized(implicit request: RequestHeader) = {
    successful(Results.NotFound("Page Not Found"))
  }
}

class CustomSecuredErrorHandler @Inject()() extends SecuredErrorHandler {
  override def onNotAuthenticated(implicit request: RequestHeader) = {
    Future.successful(Results.Unauthorized("User logged out"))
  }

  override def onNotAuthorized(implicit request: RequestHeader) = {
    successful(Results.NotFound("Page Not Found"))
  }
}

class UserServiceImpl @Inject()() extends IdentityService[User] {
  override def retrieve(loginInfo: LoginInfo): Future[Option[User]] = {
    successful(Some(User(loginInfo.providerKey)))
  }
}
