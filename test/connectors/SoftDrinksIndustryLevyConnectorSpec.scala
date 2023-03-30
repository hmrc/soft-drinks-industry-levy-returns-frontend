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
import models.{FinancialLineItem, ReturnPeriod, SdilReturn}
import models.retrieved.RetrievedSubscription
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.mockito.MockitoSugar
import play.api.Configuration
import repositories.SDILSessionCache
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpResponse}
import utilitlies.ReturnsHelper
import play.api.libs.json.Json

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class SoftDrinksIndustryLevyConnectorSpec extends SpecBase with MockitoSugar with ScalaFutures {

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

  implicit val hc = HeaderCarrier()

  val utr: String = "1234567891"

  "SoftDrinksIndustryLevyConnector" - {

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
        val res = softDrinksIndustryLevyConnector.checkSmallProducerStatus(sdilNumber, period)

        whenReady(
          res
        ) {
          response =>
            response mustEqual Some(false)
        }

      }

      "return a oldest pending return period successfully" in {

        val returnPeriod = ReturnPeriod(year = 2022, quarter = 3)
        when(mockHttp.GET[List[ReturnPeriod]](any(), any(), any())(any(), any(), any())).thenReturn(Future.successful(List(returnPeriod)))
        val res = softDrinksIndustryLevyConnector.oldestPendingReturnPeriod(utr)

        whenReady(
          res
        ) {
          response =>
            response mustEqual Some(returnPeriod)
        }
      }

      "return balance successfully" in {
        when(mockHttp.GET[BigDecimal](any(), any(), any())(any(), any(), any())).thenReturn(Future.successful(BigDecimal(1000)))
        val res = softDrinksIndustryLevyConnector.balance(sdilNumber, false)

        whenReady(
          res
        ) {
          response =>
            response mustEqual BigDecimal(1000)
        }
      }

    "return balance history successfully" in {

      when(mockHttp.GET[List[FinancialLineItem]](any(), any(), any())(any(), any(), any())).thenReturn(Future.successful(financialItemList))

      val res = softDrinksIndustryLevyConnector.balanceHistory(sdilNumber, false)

      whenReady(
        res
      ) {
        response =>
          response mustEqual financialItemList
      }
    }

    "return returns-pending successfully" in {

      when(mockHttp.GET[List[ReturnPeriod]](any(), any(), any())(any(), any(), any())).thenReturn(Future.successful(returnPeriods))

      val res = softDrinksIndustryLevyConnector.returns_pending(utr)

      whenReady(
        res
      ) {
        response =>
          response mustEqual returnPeriods
      }
    }

//    "return returns-update successfully" in {
//
//      val response = HttpResponse(200,Json.obj(),Map("",Seq()))
//      when(mockHttp.POST[SdilReturn, HttpResponse](any(), any())).thenReturn(Future.successful(response))
//
//      val res = softDrinksIndustryLevyConnector.returns_update(utr, returnPeriod, ReturnsHelper.emptyReturn)
//
//      whenReady(
//        res
//      ) {
//        response =>
//          response mustEqual Some(200)
//      }
//    }

  }

}
