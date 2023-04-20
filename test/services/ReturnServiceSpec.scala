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

import base.{SpecBase, UserAnswersTestData}
import connectors.SoftDrinksIndustryLevyConnector
import models.{ReturnPeriod, SdilReturn}
import org.mockito.MockitoSugar.{mock, when}
import play.api.test.Helpers._
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ReturnServiceSpec extends SpecBase {

  implicit val hc = HeaderCarrier()


  val mockSdilConnector = mock[SoftDrinksIndustryLevyConnector]

  val service = new ReturnService(mockSdilConnector)

  "getPendingReturns" - {
    "return a list of return periods" in {
      when(mockSdilConnector.returns_pending("123456789")(hc)).thenReturn(Future.successful(List.empty[ReturnPeriod]))

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
        val res = service.returnsUpdate(aSubscription, returnPeriod, emptyUserAnswers, true)

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
        when(mockSdilConnector.returns_update(aSubscription.utr, returnPeriod, returnFromUserAnswers)(hc)).thenReturn(Future.successful(Some(200)))
        val res = service.returnsUpdate(aSubscription, returnPeriod, userAnswers, false)

        whenReady(res) { result =>
          result mustBe ((): Unit)
        }
      }

      "when a none nil return and all answers no is being submitted" in {
        val userAnswers = UserAnswersTestData.withQuestionsAllFalseAndNoLitres
        val emptyReturn = SdilReturn((0, 0), (0, 0), List.empty, (0, 0), (0, 0), (0, 0), (0, 0))

        when(mockSdilConnector.returns_update(aSubscription.utr, returnPeriod, emptyReturn)(hc)).thenReturn(Future.successful(Some(200)))
        val res = service.returnsUpdate(aSubscription, returnPeriod, userAnswers, false)

        whenReady(res) { result =>
          result mustBe ((): Unit)
        }
      }
    }

    "throw an exception when sending the return fails" in {
      val emptyReturn = SdilReturn((0, 0), (0, 0), List.empty, (0, 0), (0, 0), (0, 0), (0, 0))
      when(mockSdilConnector.returns_update(aSubscription.utr, returnPeriod, emptyReturn)(hc)).thenReturn(Future.successful(None))
      lazy val res = service.returnsUpdate(aSubscription, returnPeriod, emptyUserAnswers, true)

      intercept[RuntimeException](await(res))
    }

  }

}
