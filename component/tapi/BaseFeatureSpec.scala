package tapi

import java.util.concurrent.TimeUnit

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import org.scalatest._
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import tapi.stubs.{MockApplication, MockDelegatedAuthority, MockRequestedAuthority, MockScope}

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
