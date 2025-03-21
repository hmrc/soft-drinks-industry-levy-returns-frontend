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

package utilities

import base.ReturnsTestData.emptyUserAnswers
import base.SpecBase
import config.FrontendAppConfig
import models.LevyCalculator._
import models.{ReturnPeriod, SmallProducer, UserAnswers}
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.libs.json.{JsBoolean, JsObject, Json}

import java.time.LocalDate

class TotalForQuarterSpec extends SpecBase with ScalaCheckPropertyChecks {

  override lazy val frontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

  private val preApril2025ReturnPeriod = ReturnPeriod(2025, 0)
  private val taxYear2025ReturnPeriod = ReturnPeriod(2026, 0)

//JsObject(Seq(    "key1" -> JsString("value"),    "key2" -> JsNumber(123),    "key3" -> JsObject(Seq("key31" -> JsString("value31"))) )) == Json. obj(   "key1" -> "value", "key2" -> 123, "key3" -> Json. obj("key31" -> "value31"))

  private val userAnswersData1 = Json.obj(
    "ownBrands" -> true,
    "brandsPackagedAtOwnSites" -> Json.obj("lowBand" -> 0, "highBand" -> 0),
    "packagedContractPacker" -> true,
    "howManyAsAContractPacker" -> Json.obj("lowBand" -> 10000, "highBand" -> 10000),
    "exemptionsForSmallProducers" -> true,
    "broughtIntoUK" -> true,
    "HowManyBroughtIntoUk" -> Json.obj("lowBand" -> 0, "highBand" -> 0),
    "broughtIntoUkFromSmallProducers" -> true,
    "howManyBroughtIntoTheUKFromSmallProducers" -> Json.obj("lowBand" -> 0, "highBand" -> 0),
    "claimCreditsForExports" -> true,
    "howManyCreditsForExport" -> Json.obj("lowBand" -> 0, "highBand" -> 0),
    "claimCreditsForLostDamaged" -> true,
    "howManyCreditsForLostDamaged" -> Json.obj("lowBand" -> 0, "highBand" -> 0))
  private val superCola = SmallProducer("Super Cola Ltd", "XCSDIL000000069", (0L, 0L))
  private val sparkyJuice = SmallProducer("Sparky Juice Co", "XCSDIL000000070", (0L, 0L))
  private val userAnswers = emptyUserAnswers.copy(data = userAnswersData, smallProducerList = List(sparkyJuice, superCola), returnPeriod = taxYear2025ReturnPeriod)

  private def userAnswersData(
                             ownBrandsLitres: Option[(Long, Long)] = None,
                             contractPackerLitres: Option[(Long, Long)] = None,
                             broughtIntoUKLitres: Option[(Long, Long)] = None,
                             broughtIntoUkFromSmallProducersLitres: Option[(Long, Long)] = None,
                             claimCreditsForExportsLitres: Option[(Long, Long)] = None,
                             claimCreditsForLostDamagedLitres: Option[(Long, Long)] = None,
                             smallProducerList: List[SmallProducer] = List.empty,
                             returnPeriod: ReturnPeriod
                             ): UserAnswers = {
    
    val ownBrandsJson = ownBrandsLitres.map(literage => JsObject(Seq("ownBrands" -> JsBoolean(true), "brandsPackagedAtOwnSites" -> Json.obj("lowBand" -> literage._1, "highBand" -> literage._2)))).getOrElse(JsObject(Seq("ownBrands" -> JsBoolean(false))))
    val contractPackerJson = JsObject(Seq())
    val broughtIntoUKJson = JsObject(Seq())
    val broughtIntoUkFromSmallProducersJson = JsObject(Seq())
    val claimCreditsForExportsJson = JsObject(Seq())
    val claimCreditsForLostDamagedJson = JsObject(Seq())
    val data = JsObject(
      ownBrandsJson.fields ++
      contractPackerJson.fields++
      broughtIntoUKJson.fields ++
      broughtIntoUkFromSmallProducersJson.fields ++
      claimCreditsForExportsJson.fields ++
      claimCreditsForLostDamagedJson.fields
    )
    emptyUserAnswers.copy(data = data, smallProducerList = smallProducerList, returnPeriod = returnPeriod)
  }

//  TODO: Add tests for TotalForQuarter calculateTotal, calculateLowBand, calculateHighBand

  "TotalForQuarter" - {

    val smallPosInts = Gen.choose(0, 1000)
    val largePosInts = Gen.choose(1000, 10000000)
    val janToMarInt = Gen.choose(1, 3)
    val aprToDecInt = Gen.choose(4, 12)

    (2018 to 2024).foreach(year => {

      val lowerBandCostPerLitre = BigDecimal("0.18")
      val higherBandCostPerLitre = BigDecimal("0.24")

//      TODO: One for each of lines below - test all three values at once, add in random values for small producer list - use CYAControllerSpec as guide on how to set these
//          val litresPackedAtOwnSite = userAnswers.get(BrandsPackagedAtOwnSitesPage).fold(0L)(_.lowBand) "must show own brands packaged at own site row containing calculation when yes is selected - pre April 2025 rates"
      //    val litresAsContractPacker = userAnswers.get(HowManyAsAContractPackerPage).fold(0L)(_.lowBand) "must show packaged contract packer row containing calculation when yes is selected - pre April 2025 rates"
//      "must show exemption for small producers row when yes is selected - pre April 2025 rates"
      //    val litresBroughtIntoTheUk = userAnswers.get(HowManyBroughtIntoUkPage).fold(0L)(_.lowBand) "must show brought into the UK row containing calculation when yes is selected - pre April 2025 rates"
//      "must show brought into the UK from small producers row containing calculation when yes is selected - pre April 2025 rates"
      //    val litresExported = userAnswers.get(HowManyCreditsForExportPage).fold(0L)(_.lowBand) "must show claim credits for exports row containing calculation when yes is selected - pre April 2025 rates"
      //    val litresLostOrDamaged = userAnswers.get(HowManyCreditsForLostDamagedPage).fold(0L)(_.lowBand) "must show lost or damaged row containing calculation when yes is selected - 2025 tax year rates"
      //
      //    val total = litresBroughtIntoTheUk + litresAsContractPacker
      //    val totalCredits = litresExported + litresLostOrDamaged
//      smallProducer match {
//        case true => (total - totalCredits) * lowBandCostPerLitre
//        case _ => (total + litresPackedAtOwnSite - totalCredits) * lowBandCostPerLitre
//      }
//      "must return OK and contain you do not need to pay anything when return amount is 0"
//      "must return OK and contain amount to pay header when return amount - pre April 2025 rates"
//      "must return OK and contain amount owed header when total is negative - pre April 2025 rates"

//      isSmallProducer / notSmallProducer
//        s"calculate low levy, high levy, and total correctly with non-zero litres totals ${ isSmallProducer ? "for small producer " : ""}with litres packed at own site using original rates for Apr - Dec $year" in {}
//        s"calculate low levy, high levy, and total correctly with non-zero litres totals ${ isSmallProducer ? "for small producer " : ""}with litres contract packed using original rates for Apr - Dec $year" in {}
//        s"calculate low levy, high levy, and total correctly with non-zero litres totals ${ isSmallProducer ? "for small producer " : ""}with exemptions for small producers using original rates for Apr - Dec $year" in {}
//        s"calculate low levy, high levy, and total correctly with non-zero litres totals ${ isSmallProducer ? "for small producer " : ""}with litres brought into the uk using original rates for Apr - Dec $year" in {}
//        s"calculate low levy, high levy, and total correctly with non-zero litres totals ${ isSmallProducer ? "for small producer " : ""}with litres brought into the uk from small producers using original rates for Apr - Dec $year" in {}
//        s"calculate low levy, high levy, and total correctly with non-zero litres totals ${ isSmallProducer ? "for small producer " : ""}with credits for litres exported using original rates for Apr - Dec $year" in {}
//        s"calculate low levy, high levy, and total correctly with non-zero litres totals ${ isSmallProducer ? "for small producer " : ""}with credits for litres lost or damaged using original rates for Apr - Dec $year" in {}
//        s"calculate low levy, high levy, and total correctly with non-zero litres totals ${ isSmallProducer ? "for small producer " : ""}when return amount is 0 using original rates for Apr - Dec $year" in {}
//        s"calculate low levy, high levy, and total correctly with non-zero litres totals ${ isSmallProducer ? "for small producer " : ""}when return amount to pay using original rates for Apr - Dec $year" in {}
//        s"calculate low levy, high levy, and total correctly with non-zero litres totals ${ isSmallProducer ? "for small producer " : ""}when return amount is negative using original rates for Apr - Dec $year" in {}

//
//      s"calculate low levy, high levy, and total correctly with non-zero litres totals using original rates for Apr - Dec $year" in {
//        forAll(largePosInts) { lowLitres =>
//          forAll(largePosInts) { highLitres =>
//            forAll(aprToDecInt) { month =>
//              val returnPeriod = ReturnPeriod(LocalDate.of(year, month, 1))
//              val levyCalculation = getLevyCalculation(lowLitres, highLitres, returnPeriod)(frontendAppConfig)
//              val expectedLowLevy = lowerBandCostPerLitre * lowLitres
//              val expectedHighLevy = higherBandCostPerLitre * highLitres
//              levyCalculation.lowLevy mustBe expectedLowLevy
//              levyCalculation.highLevy mustBe expectedHighLevy
//              levyCalculation.total mustBe expectedLowLevy + expectedHighLevy
//            }
//          }
//        }
//      }
//
//      s"calculate low levy, high levy, and total correctly with non-zero litres totals using original rates for Jan - Mar ${year + 1}" in {
//        forAll(largePosInts) { lowLitres =>
//          forAll(largePosInts) { highLitres =>
//            forAll(janToMarInt) { month =>
//              val returnPeriod = ReturnPeriod(LocalDate.of(year + 1, month, 1))
//              val levyCalculation = getLevyCalculation(lowLitres, highLitres, returnPeriod)(frontendAppConfig)
//              val expectedLowLevy = lowerBandCostPerLitre * lowLitres
//              val expectedHighLevy = higherBandCostPerLitre * highLitres
//              levyCalculation.lowLevy mustBe expectedLowLevy
//              levyCalculation.highLevy mustBe expectedHighLevy
//              levyCalculation.total mustBe expectedLowLevy + expectedHighLevy
//            }
//          }
//        }
//      }
    })

    (2025 to 2025).foreach(year => {

      val lowerBandCostPerLitreMap: Map[Int, BigDecimal] = Map(2025 -> BigDecimal("0.194"))

      val higherBandCostPerLitreMap: Map[Int, BigDecimal] = Map(2025 -> BigDecimal("0.259"))
//
//      s"calculate low levy, high levy, and total correctly with large litres totals using $year rates for Apr - Dec $year" in {
//        forAll(aprToDecInt) { month =>
//          forAll(largePosInts) { lowLitres =>
//            forAll(largePosInts) { highLitres =>
//              val returnPeriod = ReturnPeriod(LocalDate.of(year, month, 1))
//              val levyCalculation = getLevyCalculation(lowLitres, highLitres, returnPeriod)(frontendAppConfig)
//              val expectedLowLevy = lowerBandCostPerLitreMap(year) * lowLitres
//              val expectedHighLevy = higherBandCostPerLitreMap(year) * highLitres
//              levyCalculation.lowLevy mustBe expectedLowLevy.setScale(2, BigDecimal.RoundingMode.HALF_UP)
//              levyCalculation.highLevy mustBe expectedHighLevy.setScale(2, BigDecimal.RoundingMode.HALF_UP)
//              levyCalculation.total mustBe (expectedLowLevy + expectedHighLevy).setScale(2, BigDecimal.RoundingMode.HALF_UP)
//            }
//          }
//        }
//      }
//
//      s"calculate low levy, high levy, and total correctly with large litres totals using $year rates for Jan - Mar ${year + 1}" in {
//        forAll(janToMarInt) { month =>
//          forAll(largePosInts) { lowLitres =>
//            forAll(largePosInts) { highLitres =>
//              val returnPeriod = ReturnPeriod(LocalDate.of(year + 1, month, 1))
//              val levyCalculation = getLevyCalculation(lowLitres, highLitres, returnPeriod)(frontendAppConfig)
//              val expectedLowLevy = lowerBandCostPerLitreMap(year) * lowLitres
//              val expectedHighLevy = higherBandCostPerLitreMap(year) * highLitres
//              levyCalculation.lowLevy mustBe expectedLowLevy.setScale(2, BigDecimal.RoundingMode.HALF_UP)
//              levyCalculation.highLevy mustBe expectedHighLevy.setScale(2, BigDecimal.RoundingMode.HALF_UP)
//              levyCalculation.total mustBe (expectedLowLevy + expectedHighLevy).setScale(2, BigDecimal.RoundingMode.HALF_UP)
//            }
//          }
//        }
//      }

    })

  }

}
