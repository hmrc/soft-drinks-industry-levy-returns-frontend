/*
 * Copyright 2025 HM Revenue & Customs
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

package util

import base.LevyCalculationTestHelper.levyCalculation
import base.ReturnsTestData.{emptyUserAnswers, sdilNumber}
import base.SpecBase
import connectors.SoftDrinksIndustryLevyConnector
import models.{LevyCalculation, ReturnPeriod, SmallProducer, UserAnswers}
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.{reset, verify, when}
import org.scalacheck.Gen
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.libs.json.{JsBoolean, JsObject, Json}
import uk.gov.hmrc.http.HeaderCarrier
import util.TotalForQuarter.*

import java.time.LocalDate
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class TotalForQuarterSpec extends SpecBase with ScalaCheckPropertyChecks with MockitoSugar {

  implicit override lazy val hc: HeaderCarrier                   = HeaderCarrier()
  val mockConnector:             SoftDrinksIndustryLevyConnector = mock[SoftDrinksIndustryLevyConnector]

  val janToMarInt: Gen[Int] = Gen.choose(1, 3)
  val aprToDecInt: Gen[Int] = Gen.choose(4, 12)

  private def getRandomLitres:   Long         = Math.floor(Math.random() * 1000000).toLong
  private def getRandomLitreage: (Long, Long) = (getRandomLitres, getRandomLitres)

  private def getLitresJson(boolFieldKey: String, litreageFieldKey: String)(litresOpt: Option[(Long, Long)]): JsObject =
    litresOpt match {
      case Some((low, high)) =>
        JsObject(Seq(boolFieldKey -> JsBoolean(true), litreageFieldKey -> Json.obj("lowBand" -> low, "highBand" -> high)))
      case None => JsObject(Seq(boolFieldKey -> JsBoolean(false)))
    }

  private def userAnswersData(
    ownBrandsLitres:                       Option[(Long, Long)] = None,
    contractPackerLitres:                  Option[(Long, Long)] = None,
    broughtIntoUKLitres:                   Option[(Long, Long)] = None,
    broughtIntoUkFromSmallProducersLitres: Option[(Long, Long)] = None,
    claimCreditsForExportsLitres:          Option[(Long, Long)] = None,
    claimCreditsForLostDamagedLitres:      Option[(Long, Long)] = None,
    smallProducerLitres:                   List[(Long, Long)] = List.empty,
    returnPeriod:                          ReturnPeriod
  ): UserAnswers = {

    val ownBrandsJson      = getLitresJson(boolFieldKey = "ownBrands", litreageFieldKey = "brandsPackagedAtOwnSites")(ownBrandsLitres)
    val contractPackerJson =
      getLitresJson(boolFieldKey = "packagedContractPacker", litreageFieldKey = "howManyAsAContractPacker")(contractPackerLitres)
    val broughtIntoUKJson = getLitresJson(boolFieldKey = "broughtIntoUK", litreageFieldKey = "HowManyBroughtIntoUk")(broughtIntoUKLitres)
    val broughtIntoUkFromSmallProducersJson =
      getLitresJson(boolFieldKey = "broughtIntoUkFromSmallProducers", litreageFieldKey = "howManyBroughtIntoTheUKFromSmallProducers")(
        broughtIntoUkFromSmallProducersLitres
      )
    val claimCreditsForExportsJson =
      getLitresJson(boolFieldKey = "claimCreditsForExports", litreageFieldKey = "howManyCreditsForExport")(claimCreditsForExportsLitres)
    val claimCreditsForLostDamagedJson =
      getLitresJson(boolFieldKey = "claimCreditsForLostDamaged", litreageFieldKey = "howManyCreditsForLostDamaged")(claimCreditsForLostDamagedLitres)
    val data = JsObject(
      ownBrandsJson.fields ++
        contractPackerJson.fields ++
        broughtIntoUKJson.fields ++
        broughtIntoUkFromSmallProducersJson.fields ++
        claimCreditsForExportsJson.fields ++
        claimCreditsForLostDamagedJson.fields
    )
    val smallProducerList = smallProducerLitres.size match {
      case 2 =>
        val superCola   = SmallProducer("Super Cola Ltd", "XCSDIL000000069", smallProducerLitres.head)
        val sparkyJuice = SmallProducer("Sparky Juice Co", "XCSDIL000000070", smallProducerLitres.last)
        List(superCola, sparkyJuice)
      case _ => List.empty
    }
    emptyUserAnswers.copy(data = data, smallProducerList = smallProducerList, returnPeriod = returnPeriod)
  }

  "TotalForQuarter" - {

    val posLitresInts  = Gen.choose(1000, 10000000)
    val returnPeriod   = ReturnPeriod(2024, 1)
    val expectedResult = BigDecimal("123.45")
    val levyCalc       = levyCalculation(BigDecimal("50"), BigDecimal("73.45"))

    "getTotalLowBandLitres" - {

      "should include own brands litres when not small producer" in
        forAll(posLitresInts) { lowLitres =>
          val userAnswers = userAnswersData(ownBrandsLitres = Some((lowLitres, 0L)), returnPeriod = returnPeriod)
          getTotalLowBandLitres(userAnswers, smallProducer = false) mustBe lowLitres
        }

      "should exclude own brands litres when small producer" in
        forAll(posLitresInts) { lowLitres =>
          val userAnswers = userAnswersData(ownBrandsLitres = Some((lowLitres, 0L)), returnPeriod = returnPeriod)
          getTotalLowBandLitres(userAnswers, smallProducer = true) mustBe 0L
        }

      "should include contract packer litres" in
        forAll(posLitresInts) { lowLitres =>
          val userAnswers = userAnswersData(contractPackerLitres = Some((lowLitres, 0L)), returnPeriod = returnPeriod)
          getTotalLowBandLitres(userAnswers, smallProducer = false) mustBe lowLitres
        }

      "should include brought into UK litres" in
        forAll(posLitresInts) { lowLitres =>
          val userAnswers = userAnswersData(broughtIntoUKLitres = Some((lowLitres, 0L)), returnPeriod = returnPeriod)
          getTotalLowBandLitres(userAnswers, smallProducer = false) mustBe lowLitres
        }

      "should subtract export credits" in
        forAll(posLitresInts) { lowLitres =>
          val userAnswers = userAnswersData(claimCreditsForExportsLitres = Some((lowLitres, 0L)), returnPeriod = returnPeriod)
          getTotalLowBandLitres(userAnswers, smallProducer = false) mustBe -lowLitres
        }

      "should subtract lost/damaged credits" in
        forAll(posLitresInts) { lowLitres =>
          val userAnswers = userAnswersData(claimCreditsForLostDamagedLitres = Some((lowLitres, 0L)), returnPeriod = returnPeriod)
          getTotalLowBandLitres(userAnswers, smallProducer = false) mustBe -lowLitres
        }

      "should not include small producers litres or brought into UK from small producers litres" in
        forAll(posLitresInts) { lowLitres =>
          val userAnswers = userAnswersData(
            broughtIntoUkFromSmallProducersLitres = Some((lowLitres, 0L)),
            returnPeriod = returnPeriod
          )
          getTotalLowBandLitres(userAnswers, smallProducer = false) mustBe 0L
        }
    }

    "getTotalHighBandLitres" - {

      "should include own brands litres when not small producer" in
        forAll(posLitresInts) { highLitres =>
          val userAnswers = userAnswersData(ownBrandsLitres = Some((0L, highLitres)), returnPeriod = returnPeriod)
          getTotalHighBandLitres(userAnswers, smallProducer = false) mustBe highLitres
        }

      "should exclude own brands litres when small producer" in
        forAll(posLitresInts) { highLitres =>
          val userAnswers = userAnswersData(ownBrandsLitres = Some((0L, highLitres)), returnPeriod = returnPeriod)
          getTotalHighBandLitres(userAnswers, smallProducer = true) mustBe 0L
        }
    }

    "calculateTotal" - {

      "should call connector with aggregated litres and return totalRoundedDown" in {
        reset(mockConnector)
        val lowLitres   = 5000L
        val highLitres  = 3000L
        val userAnswers = userAnswersData(
          contractPackerLitres = Some((lowLitres, highLitres)),
          returnPeriod = returnPeriod
        )

        when(mockConnector.calculateLevy(any(), eqTo(lowLitres), eqTo(highLitres), eqTo(returnPeriod))(using any()))
          .thenReturn(Future.successful(levyCalc))

        val result = calculateTotal(sdilNumber, userAnswers, smallProducer = false, mockConnector)

        whenReady(result) { total =>
          total mustBe levyCalc.totalRoundedDown
          verify(mockConnector).calculateLevy(any(), eqTo(lowLitres), eqTo(highLitres), eqTo(returnPeriod))(using any())
        }
      }

      "should pass zero litres for own brands when small producer" in {
        reset(mockConnector)
        val lowLitres   = 5000L
        val highLitres  = 3000L
        val userAnswers = userAnswersData(
          ownBrandsLitres = Some((lowLitres, highLitres)),
          returnPeriod = returnPeriod
        )

        when(mockConnector.calculateLevy(any(), eqTo(0L), eqTo(0L), eqTo(returnPeriod))(using any()))
          .thenReturn(Future.successful(LevyCalculation.zero))

        val result = calculateTotal(sdilNumber, userAnswers, smallProducer = true, mockConnector)

        whenReady(result) { total =>
          total mustBe BigDecimal("0.00")
          verify(mockConnector).calculateLevy(any(), eqTo(0L), eqTo(0L), eqTo(returnPeriod))(using any())
        }
      }

      "should aggregate multiple sources correctly" in {
        reset(mockConnector)
        val ownLow    = 1000L; val ownHigh   = 2000L
        val packLow   = 500L; val packHigh   = 600L
        val importLow = 300L; val importHigh = 400L
        val exportLow = 200L; val exportHigh = 100L

        val expectedLow  = packLow + importLow - exportLow + ownLow // 500 + 300 - 200 + 1000 = 1600
        val expectedHigh = packHigh + importHigh - exportHigh + ownHigh // 600 + 400 - 100 + 2000 = 2900

        val userAnswers = userAnswersData(
          ownBrandsLitres = Some((ownLow, ownHigh)),
          contractPackerLitres = Some((packLow, packHigh)),
          broughtIntoUKLitres = Some((importLow, importHigh)),
          claimCreditsForExportsLitres = Some((exportLow, exportHigh)),
          returnPeriod = returnPeriod
        )

        when(mockConnector.calculateLevy(any(), eqTo(expectedLow), eqTo(expectedHigh), eqTo(returnPeriod))(using any()))
          .thenReturn(Future.successful(levyCalc))

        val result = calculateTotal(sdilNumber, userAnswers, smallProducer = false, mockConnector)

        whenReady(result) { total =>
          total mustBe levyCalc.totalRoundedDown
        }
      }
    }
  }
}
