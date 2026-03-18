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

import base.LevyCalculationTestHelper.levyCalculation
import base.ReturnsTestData.*
import models.retrieved.{OptRetrievedSubscription, OptSmallProducer, RetrievedSubscription}
import models.{FinancialLineItem, LevyCalculation, ReturnPeriod, ReturnsVariation, SdilReturn}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{verify, when}
import play.api.http.Status.{BAD_REQUEST, INTERNAL_SERVER_ERROR, OK}
import repositories.{SDILSessionCache, SDILSessionKeys}
import uk.gov.hmrc.http.HttpResponse
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.reset

import scala.concurrent.Future
import scala.concurrent.Future.never

class SoftDrinksIndustryLevyConnectorSpec extends HttpClientV2Helper {
  val (host, localPort) = ("host", "123")
  val mockSDILSessionCache: SDILSessionCache = mock[SDILSessionCache]
  val softDrinksIndustryLevyConnector = new SoftDrinksIndustryLevyConnector(http = mockHttp, frontendAppConfig, mockSDILSessionCache)

  val utr: String = "1234567891"

  "SoftDrinksIndustryLevyConnector" - {

    "when there is a subscription in cache" in {

      val identifierType: String = "sdil"
      val sdilNumber:     String = "XKSDIL000000022"
      when(mockSDILSessionCache.fetchEntry[OptRetrievedSubscription](any(), any())(using any()))
        .thenReturn(Future.successful(Some(OptRetrievedSubscription(Some(aSubscription)))))
      val res = softDrinksIndustryLevyConnector.retrieveSubscription(sdilNumber, identifierType)

      whenReady(res) { response =>
        response mustBe Some(aSubscription)
      }
    }

    "when there is no subscription in cache" in {
      val identifierType: String = "sdil"
      val sdilNumber:     String = "XKSDIL000000022"
      when(mockSDILSessionCache.fetchEntry[OptRetrievedSubscription](any(), any())(using any())).thenReturn(Future.successful(None))
      when(requestBuilderExecute[Option[RetrievedSubscription]]).thenReturn(Future.successful(Some(aSubscription)))
      when(mockSDILSessionCache.save[OptRetrievedSubscription](any, any, any)(using any())).thenReturn(Future.successful(true))
      val res = softDrinksIndustryLevyConnector.retrieveSubscription(sdilNumber, identifierType)

      whenReady(res) { response =>
        response mustBe Some(aSubscription)
      }
    }

    "when there is no subscription in cache and no subscription in the database" in {
      val identifierType: String = "sdil"
      val sdilNumber:     String = "XKSDIL000000022"
      when(mockSDILSessionCache.fetchEntry[OptRetrievedSubscription](any(), any())(using any())).thenReturn(Future.successful(None))
      when(requestBuilderExecute[Option[RetrievedSubscription]]).thenReturn(Future.successful(None))
      when(mockSDILSessionCache.save[OptRetrievedSubscription](any, any, any)(using any())).thenReturn(Future.successful(true))
      val res = softDrinksIndustryLevyConnector.retrieveSubscription(sdilNumber, identifierType)

      whenReady(res) { response =>
        response mustBe None
      }
    }

    "return a small producer status successfully" in {
      val sdilNumber: String = "XKSDIL000000022"
      val period = ReturnPeriod(year = 2022, quarter = 3)
      when(requestBuilderExecute[Option[Boolean]]).thenReturn(Future.successful(Some(false)))
      val res = softDrinksIndustryLevyConnector.checkSmallProducerStatus(sdilNumber, period)

      whenReady(res) { response =>
        response mustBe Some(false)
      }

    }

    "return none if no small producer status" in {
      val sdilNumber: String = "XKSDIL000000022"
      val period = ReturnPeriod(year = 2022, quarter = 3)
      when(requestBuilderExecute[Option[Boolean]]).thenReturn(Future.successful(None))
      val res = softDrinksIndustryLevyConnector.checkSmallProducerStatus(sdilNumber, period)

      whenReady(res) { response =>
        response mustBe None
      }

    }

    "return the pending returns period successfully" in {

      val returnPeriod = ReturnPeriod(year = 2022, quarter = 3)
      when(requestBuilderExecute[List[ReturnPeriod]]).thenReturn(Future.successful(List(returnPeriod)))
      val res = softDrinksIndustryLevyConnector.getPendingReturnPeriods(utr)

      whenReady(res) { response =>
        response mustBe List(returnPeriod)
      }
    }

    "return balance successfully" in {
      when(requestBuilderExecute[BigDecimal]).thenReturn(Future.successful(BigDecimal(1000)))
      val res = softDrinksIndustryLevyConnector.balance(sdilNumber, false)

      whenReady(res) { response =>
        response mustBe BigDecimal(1000)
      }
    }

    "return balance history successfully" in {

      when(requestBuilderExecute[List[FinancialLineItem]]).thenReturn(Future.successful(financialItemList))

      val res = softDrinksIndustryLevyConnector.balanceHistory(sdilNumber, false)

      whenReady(res) { response =>
        response mustBe financialItemList
      }
    }

    "return returns-pending successfully" in {

      when(requestBuilderExecute[List[ReturnPeriod]]).thenReturn(Future.successful(returnPeriods))

      val res = softDrinksIndustryLevyConnector.getPendingReturnPeriods(utr)

      whenReady(res) { response =>
        response mustBe returnPeriods
      }
    }

    "post return succesfully" in {
      val period = ReturnPeriod(year = 2022, quarter = 3)
      val sdilReturn: SdilReturn = SdilReturn(
        ownBrand = (1L, 1L),
        packLarge = (1L, 1L),
        packSmall = List(),
        importLarge = (1L, 1L),
        importSmall = (1L, 1L),
        `export` = (1L, 1L),
        wastage = (1L, 1L),
        submittedOn = None
      )

      when(requestBuilderExecute[HttpResponse])
        .thenReturn(Future.successful(HttpResponse(OK, "")))

      val res = softDrinksIndustryLevyConnector.returns_update(utr, period, sdilReturn)

      whenReady(res) { response =>
        response mustBe Some(OK)
      }
    }

    "return levy calculation when not in cache" in {
      val period              = ReturnPeriod(year = 2024, quarter = 1)
      val expectedCalculation = levyCalculation(BigDecimal("180"), BigDecimal("240"))

      when(mockSDILSessionCache.fetchEntry[LevyCalculation](any(), any())(using any())).thenReturn(Future.successful(None))
      when(requestBuilderExecute[LevyCalculation]).thenReturn(Future.successful(expectedCalculation))
      when(mockSDILSessionCache.save[LevyCalculation](any(), any(), any())(using any())).thenReturn(Future.successful(true))

      val res = softDrinksIndustryLevyConnector.calculateLevy(sdilNumber, 1000L, 1000L, period)

      whenReady(res) { response =>
        response mustBe expectedCalculation
      }
    }

    "return levy calculation from cache" in {
      val period            = ReturnPeriod(year = 2024, quarter = 1)
      val cachedCalculation = levyCalculation(BigDecimal("180"), BigDecimal("240"))

      when(mockSDILSessionCache.fetchEntry[LevyCalculation](any(), any())(using any())).thenReturn(Future.successful(Some(cachedCalculation)))

      val res = softDrinksIndustryLevyConnector.calculateLevy(sdilNumber, 1000L, 1000L, period)

      whenReady(res) { response =>
        response mustBe cachedCalculation
      }
    }

    "when there is a subscription entry in cache but it is cached as None" in {
      val identifierType: String = "sdil"
      val sdilNumber:     String = "XKSDIL000000022"

      when(mockSDILSessionCache.fetchEntry[OptRetrievedSubscription](any(), any())(using any()))
        .thenReturn(Future.successful(Some(OptRetrievedSubscription(None))))

      when(requestBuilderExecute[Option[RetrievedSubscription]])
        .thenThrow(new RuntimeException("HTTP should not be called on cache hit"))

      val res = softDrinksIndustryLevyConnector.retrieveSubscription(sdilNumber, identifierType)

      whenReady(res) { response =>
        response mustBe None
      }
    }

    "when small producer status is in cache (Some(true))" in {
      val sdilRef = "XKSDIL000000022"
      val period  = ReturnPeriod(year = 2022, quarter = 3)

      when(
        mockSDILSessionCache.fetchEntry[OptSmallProducer](
          eqTo(sdilRef),
          eqTo(SDILSessionKeys.smallProducerForPeriod(period))
        )(using any())
      ).thenReturn(Future.successful(Some(OptSmallProducer(Some(true)))))

      val res = softDrinksIndustryLevyConnector.checkSmallProducerStatus(sdilRef, period)

      whenReady(res) { response =>
        response mustBe Some(true)
      }
    }

    "when small producer status is in cache (Some(false))" in {
      val sdilRef = "XKSDIL000000022"
      val period  = ReturnPeriod(year = 2022, quarter = 3)

      when(
        mockSDILSessionCache.fetchEntry[OptSmallProducer](
          eqTo(sdilRef),
          eqTo(SDILSessionKeys.smallProducerForPeriod(period))
        )(using any())
      ).thenReturn(Future.successful(Some(OptSmallProducer(Some(false)))))

      val res = softDrinksIndustryLevyConnector.checkSmallProducerStatus(sdilRef, period)

      whenReady(res) { response =>
        response mustBe Some(false)
      }

    }

    "when small producer status is in cache but cached as None" in {
      val sdilRef = "XKSDIL000000022"
      val period  = ReturnPeriod(year = 2022, quarter = 3)

      when(
        mockSDILSessionCache.fetchEntry[OptSmallProducer](
          eqTo(sdilRef),
          eqTo(SDILSessionKeys.smallProducerForPeriod(period))
        )(using any())
      ).thenReturn(Future.successful(Some(OptSmallProducer(None))))

      val res = softDrinksIndustryLevyConnector.checkSmallProducerStatus(sdilRef, period)

      whenReady(res) { response =>
        response mustBe None
      }

    }

    "return balance from cache (withAssessment = false)" in {
      val sdilRef = "XKSDIL000000022"

      when(
        mockSDILSessionCache.fetchEntry[BigDecimal](
          eqTo(sdilRef),
          eqTo(SDILSessionKeys.balance(false))
        )(using any())
      ).thenReturn(Future.successful(Some(BigDecimal(999))))

      val res = softDrinksIndustryLevyConnector.balance(sdilRef, withAssessment = false)

      whenReady(res) { response =>
        response mustBe BigDecimal(999)
      }
    }

    "return balance from cache (withAssessment = true)" in {
      val sdilRef = "XKSDIL000000022"

      when(
        mockSDILSessionCache.fetchEntry[BigDecimal](
          eqTo(sdilRef),
          eqTo(SDILSessionKeys.balance(true))
        )(using any())
      ).thenReturn(Future.successful(Some(BigDecimal(1234))))

      val res = softDrinksIndustryLevyConnector.balance(sdilRef, withAssessment = true)

      whenReady(res) { response =>
        response mustBe BigDecimal(1234)
      }

    }

    "return balance history from cache (withAssessment = false)" in {
      val sdilRef = "XKSDIL000000022"
      val key     = SDILSessionKeys.balanceHistory(false)

      when(
        mockSDILSessionCache.fetchEntry[List[FinancialLineItem]](
          eqTo(sdilRef),
          eqTo(key)
        )(using any())
      ).thenReturn(Future.successful(Some(financialItemList)))

      val res = softDrinksIndustryLevyConnector.balanceHistory(sdilRef, withAssessment = false)

      whenReady(res) { response =>
        response mustBe financialItemList
      }
    }
  }
}
