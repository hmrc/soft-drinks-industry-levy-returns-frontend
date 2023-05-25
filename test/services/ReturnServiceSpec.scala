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

package services

import base.ReturnsTestData._
import base.{SpecBase, UserAnswersTestData}
import config.FrontendAppConfig
import connectors.SoftDrinksIndustryLevyConnector
import models.{ReturnPeriod, SdilReturn}
import org.mockito.MockitoSugar.{mock, when}
import play.api.test.Helpers._
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.Future

class ReturnServiceSpec extends SpecBase {


  val mockSdilConnector = mock[SoftDrinksIndustryLevyConnector]
  val mockConfig = mock[FrontendAppConfig]

  val service = new ReturnService(mockSdilConnector, mockConfig) {
    override val costHigher: BigDecimal = BigDecimal("0.24")
    override val costLower: BigDecimal = BigDecimal("0.18")
  }

  "getPendingReturns" - {
    "return a list of return periods" in {
      when(mockSdilConnector.getPendingReturnPeriods("123456789")(hc)).thenReturn(Future.successful(List.empty[ReturnPeriod]))

      val res = service.getPendingReturns("123456789")

      whenReady(res) {result =>
        result mustBe List.empty[ReturnPeriod]
      }
    }
  }

  "returnsUpdate" - {

    "should send the return and return Unit" - {
      "when a nil return is being submitted" in {
        val emptyReturn = SdilReturn((0, 0), (0, 0), List.empty, (0, 0), (0, 0), (0, 0), (0, 0))
        when(mockSdilConnector.returns_update(aSubscription.utr, returnPeriod, emptyReturn)(hc)).thenReturn(Future.successful(Some(200)))
        when(mockSdilConnector.returns_variation(aSubscription.sdilRef, returnVariationForNilReturn)(hc)).thenReturn(Future.successful(Some(204)))

        val res = service.sendReturn(aSubscription, returnPeriod, emptyUserAnswers, true)

        whenReady(res) {result =>
          result mustBe ((): Unit)
        }
      }

      "when a none nil return is being submitted" in {
        val userAnswers = UserAnswersTestData.withQuestionsAllTrueAllLitresInAllBands1SmallProducer
        val returnFromUserAnswers = SdilReturn((1000, 1000),
          (1000, 1000),
          userAnswers.smallProducerList,
          (1000, 1000),
          (1000, 1000),
          (1000, 1000),
          (1000, 1000)
        )
        val returnVariation = returnVariationForNilReturn.copy(
          importer = (true, (8000, 8000)),
          packer = (true, (8000, 12000)),
          packingSites = userAnswers.packagingSiteList.values.toList,
          taxEstimation = 5040.00
        )
        when(mockSdilConnector.returns_update(aSubscription.utr, returnPeriod, returnFromUserAnswers)(hc)).thenReturn(Future.successful(Some(200)))
        when(mockSdilConnector.returns_variation(aSubscription.sdilRef, returnVariation)(hc)).thenReturn(Future.successful(Some(204)))
        val res = service.sendReturn(aSubscription, returnPeriod, userAnswers, false)

        whenReady(res) { result =>
          result mustBe ((): Unit)
        }
      }

      "when a none nil return and all answers no is being submitted" in {
        val userAnswers = UserAnswersTestData.withQuestionsAllFalseAndNoLitres
        when(mockSdilConnector.returns_update(aSubscription.utr, returnPeriod, emptyReturn)(hc)).thenReturn(Future.successful(Some(200)))
        when(mockSdilConnector.returns_variation(aSubscription.sdilRef, returnVariationForNilReturn)(hc)).thenReturn(Future.successful(Some(204)))

        val res = service.sendReturn(aSubscription, returnPeriod, userAnswers, false)

        whenReady(res) { result =>
          result mustBe ((): Unit)
        }
      }
    }

    "throw an exception when sending the return fails" in {
      val emptyReturn = SdilReturn((0, 0), (0, 0), List.empty, (0, 0), (0, 0), (0, 0), (0, 0))
      when(mockSdilConnector.returns_update(aSubscription.utr, returnPeriod, emptyReturn)(hc)).thenReturn(Future.successful(None))
      lazy val res = service.sendReturn(aSubscription, returnPeriod, emptyUserAnswers, true)

      intercept[RuntimeException](await(res))
    }

  }

}
