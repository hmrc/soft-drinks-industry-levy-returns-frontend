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

package orchestrators

import base.ReturnsTestData._
import base.SpecBase
import errors.NoPendingReturnForGivenPeriod
import models.requests.{DataRequest, IdentifierRequest}
import models.{Amounts, ReturnPeriod}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import play.api.mvc.AnyContent
import play.api.test.FakeRequest
import repositories.{SDILSessionCache, SDILSessionKeys, SessionRepository}
import services.ReturnService

import scala.concurrent.Future

class ReturnsOrchestratorSpec extends SpecBase with MockitoSugar {

  val mockReturnService = mock[ReturnService]
  val mockSdilCache = mock[SDILSessionCache]
  val mockSessionRepository = mock[SessionRepository]
  val year = 2018
  val quarter = 1
  val requestReturnPeriod = ReturnPeriod(year, quarter)
  val identifierRequest: IdentifierRequest[AnyContent] = IdentifierRequest(FakeRequest(), sdilReference, aSubscription)
  val dataRequest: DataRequest[AnyContent] = DataRequest(FakeRequest(), sdilReference, aSubscription, emptyUserAnswers, requestReturnPeriod)


  val orchestrator = new ReturnsOrchestrator(mockReturnService, mockSdilCache, mockSessionRepository)

  "setupNewReturn" - {
    "when the return period is valid" - {
      "should save the return period to cache, setup user answers and return unit" in {
        when(mockReturnService.getPendingReturns(utr)(hc)).thenReturn(Future.successful(returnPeriods))
        when(mockSdilCache.save[ReturnPeriod](sdilReference, SDILSessionKeys.RETURN_PERIOD, requestReturnPeriod)).thenReturn(Future.successful(true))
        when(mockSessionRepository.set(any())).thenReturn(Future.successful(Right(true)))

        val res = orchestrator.setupNewReturn(year, quarter, false)(identifierRequest, hc, ec)

        whenReady(res.value) { result =>
          result mustBe Right((): Unit)
        }
      }
    }

    "when the year and quarter don't match any pending returns" - {

      "should return a NoPendingReturnForGivenPeriod" in {
        when(mockReturnService.getPendingReturns(utr)(hc)).thenReturn(Future.successful(returnPeriods))

        val res = orchestrator.setupNewReturn(2022, quarter, false)(identifierRequest, hc, ec)

        whenReady(res.value) { result =>
          result mustBe Left(NoPendingReturnForGivenPeriod)
        }
      }
    }

    "when there are no pending returns" - {

      "should return a NoPendingReturnForGivenPeriod" in {
        when(mockReturnService.getPendingReturns(utr)(hc)).thenReturn(Future.successful(List.empty))

        val res = orchestrator.setupNewReturn(year, quarter, false)(identifierRequest, hc, ec)

        whenReady(res.value) { result =>
          result mustBe Left(NoPendingReturnForGivenPeriod)
        }
      }
    }
  }

  "calculateAmounts" - {
    "should call the returns service and return the amounts" in {
      when(mockReturnService.calculateAmounts(sdilReference, emptyUserAnswers, requestReturnPeriod)(hc, ec)).thenReturn(Future.successful(amounts))
      val res = orchestrator.calculateAmounts(sdilReference, emptyUserAnswers, requestReturnPeriod)

      whenReady(res) { result =>
        result mustBe amounts
      }
    }
  }

  "completeReturnAndUpdateUserAnswers" - {
    "should calculate amounts, submit return and update user answers to submitted" - {
      "when successful" in {
        when(mockReturnService.calculateAmounts(aSubscription.sdilRef, emptyUserAnswers, requestReturnPeriod)(hc, ec)).thenReturn(Future.successful(amounts))
        when(mockSdilCache.save[Amounts](aSubscription.sdilRef, SDILSessionKeys.AMOUNTS, amounts)).thenReturn(Future.successful(true))
        when(mockReturnService.sendReturn(aSubscription, requestReturnPeriod, emptyUserAnswers, false)(hc, ec)).thenReturn(Future.successful((): Unit))
        when(mockSessionRepository.set(emptyUserAnswers.copy(submitted = true))).thenReturn(Future.successful(Right(true)))
        val res = orchestrator.completeReturnAndUpdateUserAnswers()(dataRequest, hc, ec)

        whenReady(res) { result =>
          result mustBe (): Unit
        }
      }
    }
  }

  "getCalculatedAmountsForReturnSent" - {
    "when the calculated amounts are in the cache" - {
      "should return the amounts" in {
        when(mockSdilCache.fetchEntry[Amounts](sdilReference, SDILSessionKeys.AMOUNTS)).thenReturn(Future.successful(Some(amounts)))

        val res = orchestrator.getCalculatedAmountsForReturnSent(sdilReference, emptyUserAnswers, requestReturnPeriod)

        whenReady(res) { result =>
          result mustBe amounts
        }
      }
    }

    "when the calculated amounts are not in the cache" - {
      "should call the returnsService to calculate amounts and return the amounts" in {
        when(mockSdilCache.fetchEntry[Amounts](sdilReference, SDILSessionKeys.AMOUNTS)).thenReturn(Future.successful(None))
        when(mockReturnService.calculateAmounts(sdilReference, emptyUserAnswers, requestReturnPeriod)(hc, ec)).thenReturn(Future.successful(amounts))
        val res = orchestrator.getCalculatedAmountsForReturnSent(sdilReference, emptyUserAnswers, requestReturnPeriod)

        whenReady(res) { result =>
          result mustBe amounts
        }
      }
    }
  }
  "tempSetupReturnTest" - {
    "should cache a return period and setup user answers for testing purposes when user has not navigated from accounts repo" in {
      when(mockReturnService.getPendingReturns(utr)(hc)).thenReturn(Future.successful(returnPeriods))
      when(mockSdilCache.save[ReturnPeriod](sdilReference, SDILSessionKeys.RETURN_PERIOD, ReturnPeriod(2019,1))).thenReturn(Future.successful(true))
      when(mockSessionRepository.set(any())).thenReturn(Future.successful(Right(true)))
      whenReady(orchestrator.tempSetupReturnTest(identifierRequest, hc, ec).value) { res =>
        res.isRight mustBe true
      }
    }
    s"should return $NoPendingReturnForGivenPeriod when no periods exist for testing purposes when user not navigated from accounts repo" in {
      when(mockReturnService.getPendingReturns(utr)(hc)).thenReturn(Future.successful(List.empty))
      whenReady(orchestrator.tempSetupReturnTest(identifierRequest, hc, ec).value) { res =>
        res mustBe Left(NoPendingReturnForGivenPeriod)
      }
    }
  }

}
