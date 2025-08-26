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

import base.ReturnsTestData._
import base.SpecBase
import config.FrontendAppConfig
import errors.NoPendingReturnForGivenPeriod
import models.{ Amounts, NormalMode }
import orchestrators.ReturnsOrchestrator
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar.mock
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._

class ReturnsControllerSpec extends SpecBase {

  val zero = BigDecimal(0.00)
  val amounts = Amounts(zero, zero, zero)
  val mockReturnsOrchestrator = mock[ReturnsOrchestrator]

  "onPageLoad" - {
    "a request to submit a return for a valid return period is submitted" - {
      "for a user who has small producer activity" - {
        "should redirect to PackagedContractPacker page" - {
          "when a none nilReturn" in {
            val application = applicationBuilder(subscription = Some(subscriptionWithSmallProducerActivity)).overrides(
              bind[ReturnsOrchestrator].toInstance(mockReturnsOrchestrator)).build()

            running(application) {
              val request = FakeRequest(GET, routes.ReturnsController.onPageLoad(returnPeriod.year, returnPeriod.quarter, false).url)
              when(mockReturnsOrchestrator.handleReturnRequest(any(), any(), any())(any(), any(), any()))
                .thenReturn(createSuccessReturnResult((): Unit))
              val result = route(application, request).value

              status(result) mustEqual SEE_OTHER
              redirectLocation(result).get mustBe routes.PackagedContractPackerController.onPageLoad(NormalMode).url
            }
          }
        }

        "should redirect to check your answers" - {
          "when a nilReturn" in {
            val application = applicationBuilder(subscription = Some(subscriptionWithSmallProducerActivity)).overrides(
              bind[ReturnsOrchestrator].toInstance(mockReturnsOrchestrator)).build()

            running(application) {
              val request = FakeRequest(GET, routes.ReturnsController.onPageLoad(returnPeriod.year, returnPeriod.quarter, true).url)
              when(mockReturnsOrchestrator.handleReturnRequest(any(), any(), any())(any(), any(), any()))
                .thenReturn(createSuccessReturnResult((): Unit))
              val result = route(application, request).value

              status(result) mustEqual SEE_OTHER
              redirectLocation(result).get mustBe routes.CheckYourAnswersController.onPageLoad.url
            }
          }
        }

        "should redirect sdil frontend" - {
          "when there are no pending returns for the given return period" in {
            val application = applicationBuilder(subscription = Some(subscriptionWithSmallProducerActivity)).overrides(
              bind[ReturnsOrchestrator].toInstance(mockReturnsOrchestrator)).build()

            running(application) {
              val request = FakeRequest(GET, routes.ReturnsController.onPageLoad(returnPeriod.year, returnPeriod.quarter, false).url)
              when(mockReturnsOrchestrator.handleReturnRequest(any(), any(), any())(any(), any(), any()))
                .thenReturn(createFailureReturnResult(NoPendingReturnForGivenPeriod))
              val result = route(application, request).value
              val config = application.injector.instanceOf[FrontendAppConfig]

              status(result) mustEqual SEE_OTHER
              redirectLocation(result).get mustBe config.sdilHomeUrl
            }
          }
        }
      }

      "for a user who has no small producer activity" - {
        "should redirect to OwnBrands page" - {
          "when a none nilReturn" in {
            val application = applicationBuilder().overrides(
              bind[ReturnsOrchestrator].toInstance(mockReturnsOrchestrator)).build()

            running(application) {
              val request = FakeRequest(GET, routes.ReturnsController.onPageLoad(returnPeriod.year, returnPeriod.quarter, false).url)
              when(mockReturnsOrchestrator.handleReturnRequest(any(), any(), any())(any(), any(), any()))
                .thenReturn(createSuccessReturnResult((): Unit))
              val result = route(application, request).value

              status(result) mustEqual SEE_OTHER
              redirectLocation(result).get mustBe routes.OwnBrandsController.onPageLoad(NormalMode).url
            }
          }
        }

        "should redirect to check your answers" - {
          "when a nilReturn" in {
            val application = applicationBuilder().overrides(
              bind[ReturnsOrchestrator].toInstance(mockReturnsOrchestrator)).build()

            running(application) {
              val request = FakeRequest(GET, routes.ReturnsController.onPageLoad(returnPeriod.year, returnPeriod.quarter, true).url)
              when(mockReturnsOrchestrator.handleReturnRequest(any(), any(), any())(any(), any(), any()))
                .thenReturn(createSuccessReturnResult((): Unit))
              val result = route(application, request).value

              status(result) mustEqual SEE_OTHER
              redirectLocation(result).get mustBe routes.CheckYourAnswersController.onPageLoad.url
            }
          }
        }

        "should redirect sdil frontend" - {
          "when there are no pending returns for the given return period" in {
            val application = applicationBuilder().overrides(
              bind[ReturnsOrchestrator].toInstance(mockReturnsOrchestrator)).build()

            running(application) {
              val request = FakeRequest(GET, routes.ReturnsController.onPageLoad(returnPeriod.year, returnPeriod.quarter, false).url)
              when(mockReturnsOrchestrator.handleReturnRequest(any(), any(), any())(any(), any(), any()))
                .thenReturn(createFailureReturnResult(NoPendingReturnForGivenPeriod))
              val result = route(application, request).value
              val config = application.injector.instanceOf[FrontendAppConfig]

              status(result) mustEqual SEE_OTHER
              redirectLocation(result).get mustBe config.sdilHomeUrl
            }
          }
        }
      }
    }
  }
}