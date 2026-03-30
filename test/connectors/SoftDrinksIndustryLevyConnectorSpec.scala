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
import org.mockito.ArgumentMatchers.eq as eqTo
import org.mockito.{ArgumentCaptor, Mockito}
import org.mockito.Mockito.{reset, verify, when}
import play.api.http.Status.{BAD_REQUEST, INTERNAL_SERVER_ERROR, OK}
import play.api.libs.json.{JsValue, Json}
import repositories.{SDILSessionCache, SDILSessionKeys}
import uk.gov.hmrc.http.{Authorization, HeaderCarrier, HttpResponse, RequestId, SessionId}
import util.GenericLogger

import java.net.URL
import scala.concurrent.Await
import scala.concurrent.Future
import scala.concurrent.Future.never
import scala.concurrent.duration.*

class SoftDrinksIndustryLevyConnectorSpec extends HttpClientV2Helper {
  val (host, localPort) = ("host", "123")
  val mockSDILSessionCache: SDILSessionCache = mock[SDILSessionCache]
  val softDrinksIndustryLevyConnector =
    new SoftDrinksIndustryLevyConnector(http = mockHttp, frontendAppConfig, mockSDILSessionCache, application.injector.instanceOf[GenericLogger])

  val utr: String = "1234567891"

  private def correlationHeaderCarrier(requestIdValue: String, sessionIdValue: String): HeaderCarrier =
    HeaderCarrier(
      authorization = Some(Authorization("Bearer incoming-token")),
      sessionId = Some(SessionId(sessionIdValue)),
      requestId = Some(RequestId(requestIdValue)),
      deviceID = Some("device-id-1"),
      otherHeaders = Seq("X-Test-Header" -> "should-not-forward")
    )

  private def assertSanitisedCorrelationIds(outboundHc: HeaderCarrier, incomingHc: HeaderCarrier): Unit = {
    outboundHc.requestId mustBe incomingHc.requestId
    outboundHc.sessionId mustBe incomingHc.sessionId
    outboundHc.authorization mustBe None
    outboundHc.deviceID mustBe None
    outboundHc.otherHeaders mustBe Seq.empty
  }

  private def jsonResponse(status: Int, json: JsValue): HttpResponse =
    HttpResponse(status, Json.stringify(json))

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
      when(requestBuilderExecute[HttpResponse]).thenReturn(Future.successful(jsonResponse(200, Json.toJson(aSubscription))))
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
      when(requestBuilderExecute[HttpResponse]).thenReturn(Future.successful(HttpResponse(404, "")))
      when(mockSDILSessionCache.save[OptRetrievedSubscription](any, any, any)(using any())).thenReturn(Future.successful(true))
      val res = softDrinksIndustryLevyConnector.retrieveSubscription(sdilNumber, identifierType)

      whenReady(res) { response =>
        response mustBe None
      }
    }

    "return a small producer status successfully" in {
      val sdilNumber: String = "XKSDIL000000022"
      val period = ReturnPeriod(year = 2022, quarter = 3)
      when(requestBuilderExecute[HttpResponse]).thenReturn(Future.successful(jsonResponse(200, Json.toJson(false))))
      val res = softDrinksIndustryLevyConnector.checkSmallProducerStatus(sdilNumber, period)

      whenReady(res) { response =>
        response mustBe Some(false)
      }

    }

    "return none if no small producer status" in {
      val sdilNumber: String = "XKSDIL000000022"
      val period = ReturnPeriod(year = 2022, quarter = 3)
      when(requestBuilderExecute[HttpResponse]).thenReturn(Future.successful(HttpResponse(404, "")))
      val res = softDrinksIndustryLevyConnector.checkSmallProducerStatus(sdilNumber, period)

      whenReady(res) { response =>
        response mustBe None
      }

    }

    "return the pending returns period successfully" in {

      val returnPeriod = ReturnPeriod(year = 2022, quarter = 3)
      when(requestBuilderExecute[HttpResponse]).thenReturn(Future.successful(jsonResponse(200, Json.toJson(List(returnPeriod)))))
      val res = softDrinksIndustryLevyConnector.getPendingReturnPeriods(utr)

      whenReady(res) { response =>
        response mustBe List(returnPeriod)
      }
    }

    "return balance successfully" in {
      when(requestBuilderExecute[HttpResponse]).thenReturn(Future.successful(jsonResponse(200, Json.toJson(BigDecimal(1000)))))
      val res = softDrinksIndustryLevyConnector.balance(sdilNumber, false)

      whenReady(res) { response =>
        response mustBe BigDecimal(1000)
      }
    }

    "return balance history successfully" in {

      when(requestBuilderExecute[HttpResponse]).thenReturn(Future.successful(jsonResponse(200, Json.toJson(financialItemList))))

      val res = softDrinksIndustryLevyConnector.balanceHistory(sdilNumber, false)

      whenReady(res) { response =>
        response mustBe financialItemList
      }
    }

    "return returns-pending successfully" in {

      when(requestBuilderExecute[HttpResponse]).thenReturn(Future.successful(jsonResponse(200, Json.toJson(returnPeriods))))

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
        response mustBe OK
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

      when(requestBuilderExecute[HttpResponse])
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

    "preserve correlation ids and strip custom headers in outbound GET HeaderCarrier for retrieveSubscription" in {
      val incomingHc = correlationHeaderCarrier("request-id-returns-get-1", "session-id-returns-get-1")
      reset(mockHttp, requestBuilder)

      when(mockHttp.get(any[URL])(using any[HeaderCarrier])).thenReturn(requestBuilder)

      when(mockSDILSessionCache.fetchEntry[OptRetrievedSubscription](any(), any())(using any()))
        .thenReturn(Future.successful(None))
      when(requestBuilderExecute[HttpResponse])
        .thenReturn(Future.successful(jsonResponse(200, Json.toJson(aSubscription))))
      when(mockSDILSessionCache.save[OptRetrievedSubscription](any, any, any)(using any()))
        .thenReturn(Future.successful(true))

      Await.result(
        softDrinksIndustryLevyConnector.retrieveSubscription("XKSDIL000000022", "sdil")(using incomingHc),
        1.seconds
      )

      val hcCaptor: ArgumentCaptor[HeaderCarrier] = ArgumentCaptor.forClass(classOf[HeaderCarrier])
      verify(mockHttp).get(any[URL])(using hcCaptor.capture())

      assertSanitisedCorrelationIds(hcCaptor.getValue, incomingHc)
    }

    "preserve correlation ids and strip custom headers in outbound POST HeaderCarrier for returns_update" in {
      val incomingHc = correlationHeaderCarrier("request-id-returns-post-1", "session-id-returns-post-1")
      val period     = ReturnPeriod(year = 2022, quarter = 3)
      val sdilReturn: SdilReturn = SdilReturn(
        ownBrand = (1L, 1L),
        packLarge = (1L, 1L),
        packSmall = List.empty,
        importLarge = (1L, 1L),
        importSmall = (1L, 1L),
        `export` = (1L, 1L),
        wastage = (1L, 1L),
        submittedOn = None
      )
      reset(mockHttp, requestBuilder)

      when(mockHttp.post(any[URL])(using any[HeaderCarrier])).thenReturn(requestBuilder)
      when(requestBuilder.withBody(any[JsValue])(using any(), any(), any())).thenReturn(requestBuilder)

      when(requestBuilderExecute[HttpResponse])
        .thenReturn(Future.successful(HttpResponse(OK, "")))

      Await.result(
        softDrinksIndustryLevyConnector.returns_update(utr, period, sdilReturn)(using incomingHc),
        1.seconds
      )

      val hcCaptor: ArgumentCaptor[HeaderCarrier] = ArgumentCaptor.forClass(classOf[HeaderCarrier])
      verify(mockHttp).post(any[URL])(using hcCaptor.capture())

      assertSanitisedCorrelationIds(hcCaptor.getValue, incomingHc)
    }
  }
}
