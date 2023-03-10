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
import models.ReturnPeriod
import models.retrieved.RetrievedSubscription
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.mockito.MockitoSugar
import play.api.Configuration
import play.api.libs.json.{Format, Json}
import repositories.SDILSessionCache
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}
import play.api.test.Helpers._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, Future}

class SoftDrinksIndustryLevyConnectorSpec extends SpecBase with MockitoSugar with ScalaFutures {


//  def emptyUserAnswers = UserAnswers("XKSDIL000000022", Json.obj())
//
//  val application = applicationBuilder(userAnswers = None).build()
//  protected def applicationBuilder(
//                                    userAnswers: Option[UserAnswers] = None,
//                                    returnPeriod: Option[ReturnPeriod] = None): GuiceApplicationBuilder =
//    new GuiceApplicationBuilder()
//      .overrides(
//        bind[DataRequiredAction].to[DataRequiredActionImpl],
//        bind[IdentifierAction].to[FakeIdentifierAction],
//        bind[DataRetrievalAction].toInstance(new FakeDataRetrievalAction(userAnswers, returnPeriod))
//      )

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

//  val aSubscription = RetrievedSubscription(
//    "0000000022",
//    "XKSDIL000000022",
//    "Super Lemonade Plc",
//    UkAddress(List("63 Clifton Roundabout", "Worcester"), "WR53 7CX"),
//    RetrievedActivity(false, true, false, false, false),
//    LocalDate.of(2018, 4, 19),
//    List(
//      Site(
//        UkAddress(List("33 Rhes Priordy", "East London"), "E73 2RP"),
//        Some("88"),
//        Some("Wild Lemonade Group"),
//        Some(LocalDate.of(2018, 2, 26))),
//      Site(
//        UkAddress(List("117 Jerusalem Court", "St Albans"), "AL10 3UJ"),
//        Some("87"),
//        Some("Highly Addictive Drinks Plc"),
//        Some(LocalDate.of(2019, 8, 19))),
//      Site(
//        UkAddress(List("87B North Liddle Street", "Guildford"), "GU34 7CM"),
//        Some("94"),
//        Some("Monster Bottle Ltd"),
//        Some(LocalDate.of(2017, 9, 23))),
//      Site(
//        UkAddress(List("122 Dinsdale Crescent", "Romford"), "RM95 8FQ"),
//        Some("27"),
//        Some("Super Lemonade Group"),
//        Some(LocalDate.of(2017, 4, 23))),
//      Site(
//        UkAddress(List("105B Godfrey Marchant Grove", "Guildford"), "GU14 8NL"),
//        Some("96"),
//        Some("Star Products Ltd"),
//        Some(LocalDate.of(2017, 2, 11)))
//    ),
//    List(),
//    Contact(Some("Ava Adams"), Some("Chief Infrastructure Agent"), "04495 206189", "Adeline.Greene@gmail.com"),
//    None
//  )

  implicit val hc = HeaderCarrier()

  "SoftDrinksIndustryLevyConnector" - {

//    "return a subscription Successfully" when { TODO

      "when there is a subscription in cache" in {

        val identifierType: String = "sdil"
        val sdilNumber: String = "XKSDIL000000022"
        when(mockSDILSessionCache.fetchEntry[RetrievedSubscription](any(),any())(any())).thenReturn(Future.successful(Some(aSubscription)))
        val res = softDrinksIndustryLevyConnector.retrieveSubscription(sdilNumber, identifierType)

        whenReady(
          res
        ) {
          response =>
            response mustEqual(Some(aSubscription))
        }
      }

      "when there is no subscription in cache" in {
        //TODO

      }

      "return a small producer status successfully" in {
        val sdilNumber: String = "XKSDIL000000022"
        val period = ReturnPeriod(year = 2022, quarter = 3)
        when(mockHttp.GET[Option[Boolean]](any(), any(), any())(any(), any(), any())).thenReturn(Future.successful(Some(false)))
        Await.result(softDrinksIndustryLevyConnector.checkSmallProducerStatus(sdilNumber, period), 4.seconds) mustBe Some(false)
      }

      "return a oldest pending return period successfully" in {
        val utr: String = "1234567891"
        val returnPeriod = ReturnPeriod(year = 2022, quarter = 3)
        when(mockHttp.GET[List[ReturnPeriod]](any(), any(), any())(any(), any(), any())).thenReturn(Future.successful(List(returnPeriod)))
        Await.result(softDrinksIndustryLevyConnector.oldestPendingReturnPeriod(utr), 4.seconds) mustBe Some(returnPeriod)
      }
    }
//  }
}
