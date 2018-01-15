package scapig

import java.util.concurrent.TimeUnit

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import org.scalatest._
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import scapig.stubs.{MockApplication, MockDelegatedAuthority, MockRequestedAuthority, MockScope}

import scala.concurrent.duration.Duration

case class MockHost(port: Int) {
  val server = new WireMockServer(WireMockConfiguration.wireMockConfig().port(port))
  val mock = new WireMock("localhost", port)
  val url = s"http://localhost:$port"
}

abstract class BaseFeatureSpec extends FeatureSpec with Matchers
with GivenWhenThen with BeforeAndAfterEach with BeforeAndAfterAll with GuiceOneServerPerSuite {

  override lazy val port = 14680
  val serviceUrl = s"http://localhost:$port"

  override def fakeApplication(): Application = new GuiceApplicationBuilder()
    .configure("services.application.host" -> "localhost")
    .configure("services.application.port" -> "7001")
    .configure("services.delegated-authority.host" -> "localhost")
    .configure("services.delegated-authority.port" -> "7002")
    .configure("services.requested-authority.host" -> "localhost")
    .configure("services.requested-authority.port" -> "7003")
    .configure("services.scope.host" -> "localhost")
    .configure("services.scope.port" -> "7004")
    .configure("loginUrl" -> "http://localhost:15000/login")
    .configure("oauthUrl" -> "http://localhost:14680")
    .configure("silhouette.authenticator.useFingerprinting" -> "false")
    .build()

  val timeout = Duration(5, TimeUnit.SECONDS)
  val mocks = Seq[MockHost](MockApplication, MockDelegatedAuthority, MockRequestedAuthority, MockScope)

  override protected def beforeEach(): Unit = {
    mocks.foreach(m => if (!m.server.isRunning) m.server.start())
  }

  override protected def afterEach(): Unit = {
    mocks.foreach(_.mock.resetMappings())
  }

  override protected def afterAll(): Unit = {
    mocks.foreach(_.server.stop())
  }
}
