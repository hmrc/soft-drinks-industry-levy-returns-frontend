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

package controllers

import base.SpecBase
import connectors.SoftDrinksIndustryLevyConnector
import models.retrieved.RetrievedActivity
import models.{Amounts, ReturnPeriod, SmallProducer, UserAnswers}
import org.jsoup.Jsoup
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.mockito.MockitoSugar.mock
import play.api.i18n.Messages
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SDILSessionCache

import scala.concurrent.Future

class ReturnsControllerSpec extends SpecBase {

  val zero = BigDecimal(0.00)
  val amounts = Amounts(zero, zero, zero)
  val mockSessionCache = mock[SDILSessionCache]
  when(mockSessionCache.fetchEntry[Amounts](any(), any())(any())) thenReturn Future.successful(Some(amounts))
  val returnPeriodsContainingBaseReturnPeriod = List(ReturnPeriod(2020, 0), ReturnPeriod(2023, 1), returnPeriod)
  val mockSdilConnector = mock[SoftDrinksIndustryLevyConnector]

  when(mockSdilConnector.returns_pending(any())(any())) thenReturn Future.successful(returnPeriods)
  when(mockSdilConnector.returns_update(any(), any(), any())(any())) thenReturn Future.successful(Some(OK))

  "Returns Controller" - {

    "must redirect to return sent when nil returns is false" in {
      val userAnswersData = Json.obj("ownBrands" -> false)
      val userAnswers = UserAnswers(sdilNumber, userAnswersData, List())
      val application = applicationBuilder(Some(userAnswers), Some(returnPeriod)).overrides(
        bind[SDILSessionCache].toInstance(mockSessionCache),
        bind[SoftDrinksIndustryLevyConnector].toInstance(mockSdilConnector)
      ).build()

      running(application) {
        val request = FakeRequest(GET, routes.ReturnsController.onPageLoad(false).url)
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).get mustBe routes.ReturnSentController.onPageLoad().url

      }
    }

      "must redirect to return sent when nil returns is true" in {
        val amounts = Amounts(0, 0, 0)
        when(mockSessionCache.fetchEntry[Amounts](any(), any())(any())) thenReturn Future.successful(Some(amounts))
        val application = applicationBuilder(Some(emptyUserAnswers), Some(returnPeriod)).overrides(
          bind[SDILSessionCache].toInstance(mockSessionCache),
          bind[SoftDrinksIndustryLevyConnector].toInstance(mockSdilConnector)
        ).build()

        running(application) {
          val request = FakeRequest(GET, routes.ReturnsController.onPageLoad(true).url)
          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).get mustBe routes.ReturnSentController.onPageLoad().url
        }
      }


      "must handle errors when submit return fails" in {
        val amounts = Amounts(666, 666, 1332)

        when(mockSessionCache.fetchEntry[Amounts](any(), any())(any())) thenReturn Future.successful(Some(amounts))
        when(mockSdilConnector.returns_pending(any())(any())) thenReturn Future.successful(returnPeriodsContainingBaseReturnPeriod)
        when(mockSdilConnector.returns_update(any(), any(), any())(any())) thenReturn Future.successful(None)

        val application = applicationBuilder(Some(emptyUserAnswers), Some(returnPeriod)).overrides(
          bind[SDILSessionCache].toInstance(mockSessionCache),
          bind[SoftDrinksIndustryLevyConnector].toInstance(mockSdilConnector)
        ).build()

        val result = running(application) {
          val request = FakeRequest(GET, routes.ReturnsController.onPageLoad(false).url)
          route(application, request).value
        }

        intercept[RuntimeException](
          result mustBe an[RuntimeException]
        )
      }

      "must handle errors when no amounts returned from session" in {

        when(mockSessionCache.fetchEntry[Amounts](any(), any())(any())) thenReturn Future.successful(None)
        when(mockSdilConnector.returns_pending(any())(any())) thenReturn Future.successful(returnPeriodsContainingBaseReturnPeriod)
        when(mockSdilConnector.returns_update(any(), any(), any())(any())) thenReturn Future.successful(None)

        val application = applicationBuilder(Some(emptyUserAnswers), Some(returnPeriod)).overrides(
          bind[SDILSessionCache].toInstance(mockSessionCache),
          bind[SoftDrinksIndustryLevyConnector].toInstance(mockSdilConnector)
        ).build()

        val result = running(application) {
          val request = FakeRequest(GET, routes.ReturnsController.onPageLoad(false).url)
          route(application, request).value
        }

        status(result) mustEqual SEE_OTHER

        intercept[RuntimeException](
          result mustBe an[RuntimeException]
        )
      }
    }

}
