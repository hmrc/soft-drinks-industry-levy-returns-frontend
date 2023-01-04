package controllers.testSupport

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.{configureFor, reset, resetAllScenarios}
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig
import models.UserAnswers
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach, Suite, TestSuite}
import org.scalatest.concurrent.{IntegrationPatience, PatienceConfiguration}
import org.scalatest.time.{Millis, Seconds, Span}
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.{CookieHeaderEncoding, Session, SessionCookieBaker}
import repositories.SessionRepository
import uk.gov.hmrc.crypto.PlainText
import uk.gov.hmrc.play.bootstrap.frontend.filters.crypto.SessionCookieCrypto
import uk.gov.hmrc.play.health.HealthController

import scala.collection.JavaConverters._
import scala.concurrent.Await
import scala.concurrent.duration.Duration

trait TestConfiguration
  extends GuiceOneServerPerSuite
    with IntegrationPatience
    with PatienceConfiguration
    with BeforeAndAfterEach
    with BeforeAndAfterAll {

  me: Suite with TestSuite ⇒

  val wiremockHost: String = "localhost"
  val wiremockPort: Int = Port.randomAvailable

  val baseUrl = s"http://localhost:$port/soft-drinks-industry-levy-returns-frontend"

  val sessionId = "sessionId-eb3158c2-0aff-4ce8-8d1b-f2208ace52fe"
  val xSessionId: (String, String) = "X-Session-ID" -> sessionId
  val xRequestId: (String, String) = "X-Request-ID" -> sessionId
  val AUTHORIZE_HEADER_VALUE =
    "Bearer BXQ3/Treo4kQCZvVcCqKPhhpBYpRtQQKWTypn1WBfRHWUopu5V/IFWF5phY/fymAP1FMqQR27MmCJxb50Hi5GD6G3VMjMtSLu7TAAIuqDia6jByIpXJpqOgLQuadi7j0XkyDVkl0Zp/zbKtHiNrxpa0nVHm3+GUC4H2h4Ki8OjP9KwIkeIPK/mMlBESjue4V"

  val sessionBaker: SessionCookieBaker = app.injector.instanceOf[SessionCookieBaker]
  val cookieHeaderEncoding: CookieHeaderEncoding = app.injector.instanceOf[CookieHeaderEncoding]
  val sessionCookieCrypto: SessionCookieCrypto = app.injector.instanceOf[SessionCookieCrypto]

  def createSessionCookieAsString(sessionData: Map[String, String]): String = {
    val sessionCookie = sessionBaker.encodeAsCookie(Session(sessionData))
    val encryptedSessionCookieValue =
      sessionCookieCrypto.crypto.encrypt(PlainText(sessionCookie.value)).value
    val encryptedSessionCookie =
      sessionCookie.copy(value = encryptedSessionCookieValue)
    cookieHeaderEncoding.encodeCookieHeader(Seq(encryptedSessionCookie))
  }
  val authData = Map("authToken" -> AUTHORIZE_HEADER_VALUE)
  val sessionAndAuth  = Map("authToken" -> AUTHORIZE_HEADER_VALUE, "sessionId" -> sessionId)

  lazy val mongo: SessionRepository = app.injector.instanceOf[SessionRepository]
  def setAnswers(userAnswers: UserAnswers)(implicit timeout: Duration): Unit  = Await.result(mongo.set(userAnswers), timeout)
  def getAnswers(id: String)(implicit timeout: Duration): Option[UserAnswers] = Await.result(mongo.get(id), timeout)

  val authCookie: String = createSessionCookieAsString(authData).substring(5)
  val authAndSessionCookie: String = createSessionCookieAsString(sessionAndAuth).substring(5)
  abstract override implicit val patienceConfig: PatienceConfig =
    PatienceConfig(
      timeout = Span(4, Seconds),
      interval = Span(50, Millis))

  override implicit lazy val app: Application = new GuiceApplicationBuilder()
    .configure(replaceWithWiremock(Seq(
      "auth",
      "auth.bas-gateway",
      "soft-drinks-industry-levy"
    )))
    .build()
  app.injector.instanceOf[HealthController]


  private def replaceWithWiremock(services: Seq[String]) =
    services.foldLeft(Map.empty[String, Any]) { (configMap, service) =>
      configMap + (
        s"microservice.services.$service.host" -> wiremockHost,
        s"microservice.services.$service.port" -> wiremockPort,
        s"play.filters.csrf.header.bypassHeaders.X-Requested-With" -> "*",
        s"play.filters.csrf.header.bypassHeaders.Csrf-Token" -> "nocheck",
        s"json.encryption.key" -> "fqpLDZ4sumDsekHkeEBlCA==",
        s"json.encryption.previousKeys" -> List.empty
      )
    }

  val wireMockServer = new WireMockServer(wireMockConfig().port(wiremockPort))

  override def beforeAll() = {
    wireMockServer.stop()
    wireMockServer.start()
    configureFor(wiremockHost, wiremockPort)
  }

  override def beforeEach() = {
    resetAllScenarios()
    reset()
  }

  override protected def afterAll(): Unit = {
    wireMockServer.stop()
  }

  override def afterEach(): Unit = {
    println("===== REQUESTS =====")
    wireMockServer.getAllServeEvents.asScala.toList
      .sortBy(_.getRequest.getLoggedDate)
      .map(_.getRequest).map(r => s"${r.getLoggedDate.toInstant.toEpochMilli}\t${r.getMethod}\t${r.getUrl}")
      .foreach(println)
    println("===== END =====")
  }
}
