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

import base.LevyCalculationTestHelper.levyCalculation
import base.ReturnsTestData.*
import base.{SpecBase, UserAnswersTestData}
import config.FrontendAppConfig
import connectors.SoftDrinksIndustryLevyConnector
import models.{LevyCalculation, ReturnPeriod, SdilReturn, SmallProducer}
import org.mockito.ArgumentMatchers.*
import org.mockito.Mockito.{reset, times, verify, when}
import play.api.test.Helpers.*
import uk.gov.hmrc.http.HeaderCarrier
import org.scalatestplus.mockito.MockitoSugar.mock
import org.mockito.ArgumentMatchers.eq as eqTo
import org.scalatest.BeforeAndAfterEach
import play.api.libs.json.{JsBoolean, Json}

import scala.concurrent.Future

class ReturnServiceSpec extends SpecBase with BeforeAndAfterEach {

  val mockSdilConnector: SoftDrinksIndustryLevyConnector = mock[SoftDrinksIndustryLevyConnector]
  val mockConfig:        FrontendAppConfig               = mock[FrontendAppConfig]

  val service: ReturnService = new ReturnService(mockSdilConnector, mockConfig)

  private def stubCalculateLevyWithZero(): Unit =
    when(mockSdilConnector.calculateLevy(any(), anyLong(), anyLong(), any())(using any()))
      .thenReturn(Future.successful(LevyCalculation.zero))

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockSdilConnector)
    stubCalculateLevyWithZero()
  }

  "getPendingReturns" - {
    "return a list of return periods" in {
      when(mockSdilConnector.getPendingReturnPeriods("123456789")(using hc)).thenReturn(Future.successful(List.empty[ReturnPeriod]))

      val res = service.getPendingReturns("123456789")

      whenReady(res) { result =>
        result mustBe List.empty[ReturnPeriod]
      }
    }
  }

  "returnsUpdate" - {

    "should send the return and return Unit" - {
      "when a nil return is being submitted" in {
        val emptyReturn = SdilReturn((0, 0), (0, 0), List.empty, (0, 0), (0, 0), (0, 0), (0, 0), None)

        when(
          mockSdilConnector.returns_update(
            eqTo(aSubscription.utr),
            eqTo(returnPeriod),
            argThat[SdilReturn] { sdilReturn =>
              sdilReturn.ownBrand == emptyReturn.ownBrand &&
              sdilReturn.packLarge == emptyReturn.packLarge &&
              sdilReturn.packSmall == emptyReturn.packSmall &&
              sdilReturn.importLarge == emptyReturn.importLarge &&
              sdilReturn.importSmall == emptyReturn.importSmall &&
              sdilReturn.`export` == emptyReturn.`export` &&
              sdilReturn.wastage == emptyReturn.wastage &&
              sdilReturn.submittedOn.isDefined
            }
          )(using any[HeaderCarrier])
        ).thenReturn(Future.successful(Some(200)))

        when(mockSdilConnector.returns_variation(eqTo(aSubscription.sdilRef), eqTo(returnVariationForNilReturn))(using any[HeaderCarrier]))
          .thenReturn(Future.successful(Some(204)))

        val res = service.sendReturn(aSubscription, returnPeriod, emptyUserAnswers)

        whenReady(res) { result =>
          result mustBe ((): Unit)
        }
      }

      "when a none nil return is being submitted" in {
        val userAnswers           = UserAnswersTestData.withQuestionsAllTrueAllLitresInAllBands1SmallProducer
        val returnFromUserAnswers =
          SdilReturn((1000, 1000), (1000, 1000), userAnswers.smallProducerList, (1000, 1000), (1000, 1000), (1000, 1000), (1000, 1000), None)
        val returnVariation = returnVariationForNilReturn.copy(
          importer = (true, (8000, 8000)),
          packer = (true, (8000, 12000)),
          packingSites = userAnswers.packagingSiteList.values.toList,
          taxEstimation = 0
        )

        when(
          mockSdilConnector.returns_update(
            eqTo(aSubscription.utr),
            eqTo(returnPeriod),
            argThat[SdilReturn] { sdilReturn =>
              sdilReturn.ownBrand == returnFromUserAnswers.ownBrand &&
              sdilReturn.packLarge == returnFromUserAnswers.packLarge &&
              sdilReturn.packSmall == returnFromUserAnswers.packSmall &&
              sdilReturn.importLarge == returnFromUserAnswers.importLarge &&
              sdilReturn.importSmall == returnFromUserAnswers.importSmall &&
              sdilReturn.`export` == returnFromUserAnswers.`export` &&
              sdilReturn.wastage == returnFromUserAnswers.wastage &&
              sdilReturn.submittedOn.isDefined
            }
          )(using any[HeaderCarrier])
        ).thenReturn(Future.successful(Some(200)))

        when(mockSdilConnector.returns_variation(eqTo(aSubscription.sdilRef), eqTo(returnVariation))(using any[HeaderCarrier]))
          .thenReturn(Future.successful(Some(204)))

        val res = service.sendReturn(aSubscription, returnPeriod, userAnswers)

        whenReady(res) { result =>
          result mustBe ((): Unit)
        }
      }

      "when a none nil return and all answers no is being submitted" in {
        val userAnswers = UserAnswersTestData.withQuestionsAllFalseAndNoLitres

        when(
          mockSdilConnector.returns_update(
            eqTo(aSubscription.utr),
            eqTo(returnPeriod),
            argThat[SdilReturn] { sdilReturn =>
              sdilReturn.ownBrand == emptyReturn.ownBrand &&
              sdilReturn.packLarge == emptyReturn.packLarge &&
              sdilReturn.packSmall == emptyReturn.packSmall &&
              sdilReturn.importLarge == emptyReturn.importLarge &&
              sdilReturn.importSmall == emptyReturn.importSmall &&
              sdilReturn.`export` == emptyReturn.`export` &&
              sdilReturn.wastage == emptyReturn.wastage &&
              sdilReturn.submittedOn.isDefined
            }
          )(using any[HeaderCarrier])
        ).thenReturn(Future.successful(Some(200)))

        when(mockSdilConnector.returns_variation(eqTo(aSubscription.sdilRef), eqTo(returnVariationForNilReturn))(using any[HeaderCarrier]))
          .thenReturn(Future.successful(Some(204)))

        val res = service.sendReturn(aSubscription, returnPeriod, userAnswers)

        whenReady(res) { result =>
          result mustBe ((): Unit)
        }
      }
    }

    "throw an exception when sending the return fails" in {
      val emptyReturn = SdilReturn((0, 0), (0, 0), List.empty, (0, 0), (0, 0), (0, 0), (0, 0))

      when(mockSdilConnector.returns_update(eqTo(aSubscription.utr), eqTo(returnPeriod), eqTo(emptyReturn))(using any[HeaderCarrier]))
        .thenReturn(Future.successful(None))

      lazy val res = service.sendReturn(aSubscription, returnPeriod, emptyUserAnswers)

      intercept[RuntimeException](await(res))
    }
  }

  "calculateAmounts" - {
    "should return amounts using small producer status, balance and levy total" in {
      when(mockSdilConnector.checkSmallProducerStatus(eqTo(sdilNumber), eqTo(returnPeriod))(using any()))
        .thenReturn(Future.successful(Some(false)))
      when(mockConfig.balanceAllEnabled).thenReturn(false)
      when(mockSdilConnector.balance(eqTo(sdilNumber), eqTo(false))(using any()))
        .thenReturn(Future.successful(BigDecimal(500)))
      when(mockSdilConnector.calculateLevy(any(), anyLong(), anyLong(), any())(using any()))
        .thenReturn(Future.successful(levyCalculation(BigDecimal("100"), BigDecimal("200"))))

      val res = service.calculateAmounts(sdilNumber, emptyUserAnswers, returnPeriod)

      whenReady(res) { result =>
        result.balanceBroughtForward mustBe BigDecimal(500)
      }
    }
  }

  "getBalanceBroughtForward" - {
    "should use balance when balanceAllEnabled is false" in {
      when(mockConfig.balanceAllEnabled).thenReturn(false)
      when(mockSdilConnector.balance(eqTo(sdilNumber), eqTo(false))(using any()))
        .thenReturn(Future.successful(BigDecimal(1000)))

      val res = service.getBalanceBroughtForward(sdilNumber)

      whenReady(res) { result =>
        result mustBe BigDecimal(1000)
      }
    }

    "should use balanceHistory when balanceAllEnabled is true" in {
      when(mockConfig.balanceAllEnabled).thenReturn(true)
      when(mockSdilConnector.balanceHistory(eqTo(sdilNumber), eqTo(false))(using any()))
        .thenReturn(Future.successful(financialItemList))

      val res = service.getBalanceBroughtForward(sdilNumber)

      whenReady(res) { result =>
        result mustBe a[BigDecimal]
      }
    }
  }

  "calculateLevyCalculations" - {

    val levyCalc = levyCalculation(BigDecimal("180"), BigDecimal("240"))

    "should return an empty map when user answers have no litres data" in {
      val userAnswers = emptyUserAnswers

      val res = service.calculateLevyCalculations(sdilNumber, userAnswers)

      whenReady(res) { result =>
        result mustBe Map.empty
      }
    }

    "should call connector for each unique litres pair from user answers" in {
      reset(mockSdilConnector)
      val userAnswersData = Json.obj(
        "ownBrands"                -> true,
        "brandsPackagedAtOwnSites" -> Json.obj("lowBand" -> 1000, "highBand" -> 2000),
        "packagedContractPacker"   -> true,
        "howManyAsAContractPacker"  -> Json.obj("lowBand" -> 3000, "highBand" -> 4000)
      )
      val userAnswers = emptyUserAnswers.copy(data = userAnswersData)

      when(mockSdilConnector.calculateLevy(any(), eqTo(1000L), eqTo(2000L), eqTo(defaultReturnsPeriod))(using any()))
        .thenReturn(Future.successful(levyCalc))
      when(mockSdilConnector.calculateLevy(any(), eqTo(3000L), eqTo(4000L), eqTo(defaultReturnsPeriod))(using any()))
        .thenReturn(Future.successful(levyCalculation(BigDecimal("540"), BigDecimal("960"))))

      val res = service.calculateLevyCalculations(sdilNumber, userAnswers)

      whenReady(res) { result =>
        result.size mustBe 2
        result((1000L, 2000L)) mustBe levyCalc
        result((3000L, 4000L)) mustBe levyCalculation(BigDecimal("540"), BigDecimal("960"))
      }
    }

    "should deduplicate litres pairs when multiple pages have the same values" in {
      reset(mockSdilConnector)
      val userAnswersData = Json.obj(
        "ownBrands"                -> true,
        "brandsPackagedAtOwnSites" -> Json.obj("lowBand" -> 1000, "highBand" -> 2000),
        "packagedContractPacker"   -> true,
        "howManyAsAContractPacker"  -> Json.obj("lowBand" -> 1000, "highBand" -> 2000)
      )
      val userAnswers = emptyUserAnswers.copy(data = userAnswersData)

      when(mockSdilConnector.calculateLevy(any(), eqTo(1000L), eqTo(2000L), eqTo(defaultReturnsPeriod))(using any()))
        .thenReturn(Future.successful(levyCalc))

      val res = service.calculateLevyCalculations(sdilNumber, userAnswers)

      whenReady(res) { result =>
        result.size mustBe 1
        result((1000L, 2000L)) mustBe levyCalc
        verify(mockSdilConnector, times(1)).calculateLevy(any(), eqTo(1000L), eqTo(2000L), eqTo(defaultReturnsPeriod))(using any())
      }
    }

    "should aggregate small producer litres and include them" in {
      reset(mockSdilConnector)
      val userAnswersData = Json.obj(
        "exemptionsForSmallProducers" -> true
      )
      val superCola   = SmallProducer("Super Cola Ltd", "XCSDIL000000069", (1000L, 2000L))
      val sparkyJuice = SmallProducer("Sparky Juice Co", "XCSDIL000000070", (3000L, 4000L))
      val userAnswers = emptyUserAnswers.copy(data = userAnswersData, smallProducerList = List(superCola, sparkyJuice))

      when(mockSdilConnector.calculateLevy(any(), eqTo(4000L), eqTo(6000L), eqTo(defaultReturnsPeriod))(using any()))
        .thenReturn(Future.successful(levyCalc))

      val res = service.calculateLevyCalculations(sdilNumber, userAnswers)

      whenReady(res) { result =>
        result.size mustBe 1
        result((4000L, 6000L)) mustBe levyCalc
      }
    }

    "should not include small producers when the list is empty" in {
      reset(mockSdilConnector)
      val userAnswersData = Json.obj(
        "ownBrands"                -> true,
        "brandsPackagedAtOwnSites" -> Json.obj("lowBand" -> 1000, "highBand" -> 2000)
      )
      val userAnswers = emptyUserAnswers.copy(data = userAnswersData, smallProducerList = List.empty)

      when(mockSdilConnector.calculateLevy(any(), eqTo(1000L), eqTo(2000L), eqTo(defaultReturnsPeriod))(using any()))
        .thenReturn(Future.successful(levyCalc))

      val res = service.calculateLevyCalculations(sdilNumber, userAnswers)

      whenReady(res) { result =>
        result.size mustBe 1
        result((1000L, 2000L)) mustBe levyCalc
      }
    }

    "should collect litres from all pages with different values per page" in {
      reset(mockSdilConnector)
      val userAnswersData = Json.obj(
        "ownBrands"                              -> true,
        "brandsPackagedAtOwnSites"               -> Json.obj("lowBand" -> 100, "highBand" -> 200),
        "packagedContractPacker"                  -> true,
        "howManyAsAContractPacker"                -> Json.obj("lowBand" -> 300, "highBand" -> 400),
        "broughtIntoUK"                          -> true,
        "HowManyBroughtIntoUk"                   -> Json.obj("lowBand" -> 500, "highBand" -> 600),
        "broughtIntoUkFromSmallProducers"         -> true,
        "howManyBroughtIntoTheUKFromSmallProducers" -> Json.obj("lowBand" -> 700, "highBand" -> 800),
        "claimCreditsForExports"                  -> true,
        "howManyCreditsForExport"                 -> Json.obj("lowBand" -> 900, "highBand" -> 1000),
        "claimCreditsForLostDamaged"              -> true,
        "howManyCreditsForLostDamaged"            -> Json.obj("lowBand" -> 1100, "highBand" -> 1200),
        "exemptionsForSmallProducers"             -> true
      )
      val smallProducers = List(
        SmallProducer("A", "XCSDIL000000069", (1300L, 1400L))
      )
      val userAnswers = emptyUserAnswers.copy(data = userAnswersData, smallProducerList = smallProducers)

      when(mockSdilConnector.calculateLevy(any(), anyLong(), anyLong(), eqTo(defaultReturnsPeriod))(using any()))
        .thenReturn(Future.successful(levyCalc))

      val res = service.calculateLevyCalculations(sdilNumber, userAnswers)

      whenReady(res) { result =>
        result.keySet mustBe Set(
          (100L, 200L),
          (300L, 400L),
          (500L, 600L),
          (700L, 800L),
          (900L, 1000L),
          (1100L, 1200L),
          (1300L, 1400L) // small producers aggregated
        )
      }
    }
  }

  override def afterEach(): Unit = {
    reset(mockSdilConnector)
    super.afterEach()
  }

}
