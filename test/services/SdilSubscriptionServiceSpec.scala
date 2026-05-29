/*
 * Copyright 2026 HM Revenue & Customs
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

package services

import base.ReturnsTestData.aSubscription
import base.SpecBase
import connectors.SoftDrinksIndustryLevyConnector
import org.mockito.Mockito.{reset, when}
import org.scalatestplus.mockito.MockitoSugar

import java.time.LocalDate
import scala.concurrent.Future

class SdilSubscriptionServiceSpec extends SpecBase with MockitoSugar {

  val mockConnector: SoftDrinksIndustryLevyConnector = mock[SoftDrinksIndustryLevyConnector]
  val service       = new SdilSubscriptionService(mockConnector)
  val firstSdilRef  = "XKSDIL000000021"
  val secondSdilRef = "XKSDIL000000022"

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockConnector)
  }

  "resolveActiveSdilRef" - {
    "return the first active SDIL ref" in {
      val active  = aSubscription.copy(deregDate = None)
      val expired = aSubscription.copy(deregDate = Some(LocalDate.now.minusDays(1)))

      when(mockConnector.retrieveSubscriptionNoCache(firstSdilRef, "sdil")).thenReturn(Future.successful(Some(active)))
      when(mockConnector.retrieveSubscriptionNoCache(secondSdilRef, "sdil")).thenReturn(Future.successful(Some(expired)))

      val result = service.resolveActiveSdilRef(Seq(firstSdilRef, secondSdilRef))

      result.futureValue mustBe Some(firstSdilRef)
    }

    "return the next active SDIL ref when the first is inactive" in {
      val expired = aSubscription.copy(deregDate = Some(LocalDate.now.minusDays(1)))
      val active  = aSubscription.copy(deregDate = None)

      when(mockConnector.retrieveSubscriptionNoCache(firstSdilRef, "sdil")).thenReturn(Future.successful(Some(expired)))
      when(mockConnector.retrieveSubscriptionNoCache(secondSdilRef, "sdil")).thenReturn(Future.successful(Some(active)))

      val result = service.resolveActiveSdilRef(Seq(firstSdilRef, secondSdilRef))

      result.futureValue mustBe Some(secondSdilRef)
    }

    "return None when all SDIL refs are inactive" in {
      val expired = aSubscription.copy(deregDate = Some(LocalDate.now.minusDays(1)))

      when(mockConnector.retrieveSubscriptionNoCache(firstSdilRef, "sdil")).thenReturn(Future.successful(Some(expired)))
      when(mockConnector.retrieveSubscriptionNoCache(secondSdilRef, "sdil")).thenReturn(Future.successful(Some(expired)))

      val result = service.resolveActiveSdilRef(Seq(firstSdilRef, secondSdilRef))

      result.futureValue mustBe None
    }

    "return the ref when the only SDIL enrolment is inactive" in {
      val expired = aSubscription.copy(deregDate = Some(LocalDate.now.minusDays(1)))

      when(mockConnector.retrieveSubscriptionNoCache(firstSdilRef, "sdil")).thenReturn(Future.successful(Some(expired)))

      val result = service.resolveActiveSdilRef(Seq(firstSdilRef))

      result.futureValue mustBe Some(firstSdilRef)
    }

    "return None when it is the only SDIL enrolment but no subscription is found" in {
      when(mockConnector.retrieveSubscriptionNoCache(firstSdilRef, "sdil")).thenReturn(Future.successful(None))

      val result = service.resolveActiveSdilRef(Seq(firstSdilRef))

      result.futureValue mustBe None
    }

    "return None when no SDIL refs are provided" in {
      val result = service.resolveActiveSdilRef(Seq.empty)

      result.futureValue mustBe None
    }

    "return an active ref when another lookup fails" in {
      val active = aSubscription.copy(deregDate = None)

      when(mockConnector.retrieveSubscriptionNoCache(firstSdilRef, "sdil"))
        .thenReturn(Future.failed(new RuntimeException("subscription lookup failed")))
      when(mockConnector.retrieveSubscriptionNoCache(secondSdilRef, "sdil")).thenReturn(Future.successful(Some(active)))

      val result = service.resolveActiveSdilRef(Seq(firstSdilRef, secondSdilRef))

      result.futureValue mustBe Some(secondSdilRef)
    }

    "fail when a lookup fails and no active ref is found" in {
      when(mockConnector.retrieveSubscriptionNoCache(firstSdilRef, "sdil"))
        .thenReturn(Future.failed(new RuntimeException("subscription lookup failed")))

      val result = service.resolveActiveSdilRef(Seq(firstSdilRef))

      result.failed.futureValue mustBe a[IllegalStateException]
    }
  }

  "isActive" - {
    "return true when deregistrationDate is None" in {
      service.isActive(aSubscription.copy(deregDate = None)) mustBe true
    }

    "return false when deregistrationDate is in the past" in {
      service.isActive(aSubscription.copy(deregDate = Some(LocalDate.now.minusDays(1)))) mustBe false
    }

    "return true when deregistrationDate is in the future" in {
      service.isActive(aSubscription.copy(deregDate = Some(LocalDate.now.plusDays(5)))) mustBe true
    }
  }
}
