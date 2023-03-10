/*
 * Copyright 2023 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package connectors

import base.SpecBase
import com.typesafe.config.ConfigFactory
import controllers.actions.{AuthenticatedIdentifierAction, DataRequiredAction, DataRequiredActionImpl, DataRetrievalAction, DataRetrievalActionImpl, FakeDataRetrievalAction, FakeIdentifierAction, IdentifierAction}
import models.{ReturnPeriod, UserAnswers, Warehouse}
import models.backend.{Contact, Site, UkAddress}
import models.retrieved.{RetrievedActivity, RetrievedSubscription}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import play.api.{Application, Configuration, Environment, Mode}
import play.api.http.Status.OK
import play.api.inject._
import play.api.inject.guice.GuiceApplicationBuilder
import repositories.{CascadeUpsert, SDILSessionCache, SDILSessionCacheRepository, SessionRepository}
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}
import play.api.libs.json.{Format, Json}
import play.api.test.FakeRequest
import play.api.test.Helpers.{GET, route, running}

import java.time.{Clock, LocalDate, ZoneOffset}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.{Duration, DurationInt}
import scala.concurrent.{Await, Future}

class SoftDrinksIndustryLevyConnectorSpec extends PlaySpec with MockitoSugar with ScalaFutures {


  def emptyUserAnswers = UserAnswers("XKSDIL000000022", Json.obj())

  val application = applicationBuilder(userAnswers = None).build()
  protected def applicationBuilder(
                                    userAnswers: Option[UserAnswers] = None,
                                    returnPeriod: Option[ReturnPeriod] = None): GuiceApplicationBuilder =
    new GuiceApplicationBuilder()
      .overrides(
        bind[DataRequiredAction].to[DataRequiredActionImpl],
        bind[IdentifierAction].to[FakeIdentifierAction],
        bind[DataRetrievalAction].toInstance(new FakeDataRetrievalAction(userAnswers, returnPeriod))
      )

  val (host, localPort) = ("host", "123")

  val localConfig = Configuration(
    ConfigFactory.parseString(s"""
                                 | microservice.services.soft-drinks-industry-levy {
                                 |    host     = "$host"
                                 |    port     = $localPort
                                 |  }
                                 |""".stripMargin)
  )

  val mockHttp = mock[HttpClient]
  val mockSDILSessionCache = mock[SDILSessionCache]
  val softDrinksIndustryLevyConnector = new SoftDrinksIndustryLevyConnector(http =mockHttp, localConfig, mockSDILSessionCache)

  val aSubscription = RetrievedSubscription(
    "0000000022",
    "XKSDIL000000022",
    "Super Lemonade Plc",
    UkAddress(List("63 Clifton Roundabout", "Worcester"), "WR53 7CX"),
    RetrievedActivity(false, true, false, false, false),
    LocalDate.of(2018, 4, 19),
    List(
      Site(
        UkAddress(List("33 Rhes Priordy", "East London"), "E73 2RP"),
        Some("88"),
        Some("Wild Lemonade Group"),
        Some(LocalDate.of(2018, 2, 26))),
      Site(
        UkAddress(List("117 Jerusalem Court", "St Albans"), "AL10 3UJ"),
        Some("87"),
        Some("Highly Addictive Drinks Plc"),
        Some(LocalDate.of(2019, 8, 19))),
      Site(
        UkAddress(List("87B North Liddle Street", "Guildford"), "GU34 7CM"),
        Some("94"),
        Some("Monster Bottle Ltd"),
        Some(LocalDate.of(2017, 9, 23))),
      Site(
        UkAddress(List("122 Dinsdale Crescent", "Romford"), "RM95 8FQ"),
        Some("27"),
        Some("Super Lemonade Group"),
        Some(LocalDate.of(2017, 4, 23))),
      Site(
        UkAddress(List("105B Godfrey Marchant Grove", "Guildford"), "GU14 8NL"),
        Some("96"),
        Some("Star Products Ltd"),
        Some(LocalDate.of(2017, 2, 11)))
    ),
    List(),
    Contact(Some("Ava Adams"), Some("Chief Infrastructure Agent"), "04495 206189", "Adeline.Greene@gmail.com"),
    None
  )

  implicit val hc = HeaderCarrier()

  "SoftDrinksIndustryLevyConnector" must {

    "return a subscription Successfully" in {
      val identifierType: String = "sdil"
      val sdilNumber: String = "XKSDIL000000022"
      implicit val format: Format[RetrievedSubscription] = Json.format[RetrievedSubscription]

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()
      lazy val mongo: SDILSessionCache = application.injector.instanceOf[SDILSessionCache]
      when(mockHttp.GET[Option[RetrievedSubscription]](any(), any(), any())(any(), any(), any())).thenReturn(Future.successful(Some(aSubscription)))
      val fetchEntryRes = Await.result(mongo.fetchEntry(sdilNumber, "SUBSCRIPTION")(format), 4.seconds)
      println(Console.YELLOW + fetchEntryRes + Console.WHITE)
    }
      //      running(application) {
      //
      //        val request = FakeRequest(GET, askSecondaryWarehouseInReturnRoute)
      //
      //        val result = route(application, request).value
      //
      //        val view = application.injector.instanceOf[AskSecondaryWarehouseInReturnView]
      //
      //        status(result) mustEqual OK
      //        contentAsString(result) mustEqual view(form, NormalMode)(request, messages(application)).toString
      //      }


      //      val result = softDrinksIndustryLevyConnector.retrieveSubscription(sdilNumber,identifierType)
      //
      //     result.futureValue must be(OK)

      //     Await.result(softDrinksIndustryLevyConnector.retrieveSubscription(sdilNumber,identifierType), 4.seconds) mustBe  Some(aSubscription)
      //     for{
      //       connectorResponse <- softDrinksIndustryLevyConnector.retrieveSubscription(sdilNumber,identifierType)
      //     } yield {
      //       connectorResponse.get mustEqual(aSubscription)
      //     }



    "return a small producer status successfully" in {
      val sdilNumber: String = "XKSDIL000000022"
      val period = ReturnPeriod(year = 2022, quarter = 3)
      when(mockHttp.GET[Option[Boolean]](any(),any(),any())(any(),any(),any())).thenReturn(Future.successful(Some(false)))
      Await.result(softDrinksIndustryLevyConnector.checkSmallProducerStatus(sdilNumber, period), 4.seconds) mustBe Some(false)
    }

    "return a oldest pending return period successfully" in {
      val utr: String = "1234567891"
      val returnPeriod = ReturnPeriod(year = 2022, quarter = 3)
      when(mockHttp.GET[List[ReturnPeriod]](any(),any(),any())(any(),any(),any())).thenReturn(Future.successful(List(returnPeriod)))
      Await.result(softDrinksIndustryLevyConnector.oldestPendingReturnPeriod(utr), 4.seconds) mustBe Some(returnPeriod)
    }
  }

}
