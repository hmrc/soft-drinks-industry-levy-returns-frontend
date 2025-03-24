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

import base.ReturnsTestData.emptyUserAnswers
import base.SpecBase
import config.FrontendAppConfig
import models.LevyCalculator._
import models.{ReturnPeriod, SmallProducer, UserAnswers}
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.libs.json.{JsBoolean, JsObject, Json}
import util.TotalForQuarter._

import java.time.LocalDate

class TotalForQuarterSpec extends SpecBase with ScalaCheckPropertyChecks {

  override lazy val frontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

//  private val preApril2025ReturnPeriod = ReturnPeriod(2025, 0)
//  private val taxYear2025ReturnPeriod = ReturnPeriod(2026, 0)

  private def getRandomLitres: Long = Math.floor(Math.random() * 1000000).toLong
  private def getRandomLitreage: (Long, Long) = (getRandomLitres, getRandomLitres)

  private def getLitresJson(boolFieldKey: String, litreageFieldKey: String)(litresOpt: Option[(Long, Long)]): JsObject = {
    litresOpt match {
      case Some((low, high)) =>
        JsObject(Seq(
          boolFieldKey -> JsBoolean(true),
          litreageFieldKey -> Json.obj("lowBand" -> low, "highBand" -> high))
        )
      case None => JsObject(Seq(boolFieldKey -> JsBoolean(false)))
    }
  }

  private def userAnswersData(
                             ownBrandsLitres: Option[(Long, Long)] = None,
                             contractPackerLitres: Option[(Long, Long)] = None,
                             broughtIntoUKLitres: Option[(Long, Long)] = None,
                             broughtIntoUkFromSmallProducersLitres: Option[(Long, Long)] = None,
                             claimCreditsForExportsLitres: Option[(Long, Long)] = None,
                             claimCreditsForLostDamagedLitres: Option[(Long, Long)] = None,
                             smallProducerLitres: List[(Long, Long)] = List.empty,
                             returnPeriod: ReturnPeriod
                             ): UserAnswers = {

    val ownBrandsJson = getLitresJson(boolFieldKey = "ownBrands", litreageFieldKey = "brandsPackagedAtOwnSites")(ownBrandsLitres)
    val contractPackerJson = getLitresJson(boolFieldKey = "packagedContractPacker", litreageFieldKey = "howManyAsAContractPacker")(contractPackerLitres)
    val broughtIntoUKJson = getLitresJson(boolFieldKey = "broughtIntoUK", litreageFieldKey = "HowManyBroughtIntoUk")(broughtIntoUKLitres)
    val broughtIntoUkFromSmallProducersJson = getLitresJson(boolFieldKey = "broughtIntoUkFromSmallProducers", litreageFieldKey = "howManyBroughtIntoTheUKFromSmallProducers")(broughtIntoUkFromSmallProducersLitres)
    val claimCreditsForExportsJson = getLitresJson(boolFieldKey = "claimCreditsForExports", litreageFieldKey = "howManyCreditsForExport")(claimCreditsForExportsLitres)
    val claimCreditsForLostDamagedJson = getLitresJson(boolFieldKey = "claimCreditsForLostDamaged", litreageFieldKey = "howManyCreditsForLostDamaged")(claimCreditsForLostDamagedLitres)
    val data = JsObject(
      ownBrandsJson.fields ++
      contractPackerJson.fields++
      broughtIntoUKJson.fields ++
      broughtIntoUkFromSmallProducersJson.fields ++
      claimCreditsForExportsJson.fields ++
      claimCreditsForLostDamagedJson.fields
    )
    val smallProducerList = smallProducerLitres.size match {
      case 2 =>
        val superCola = SmallProducer("Super Cola Ltd", "XCSDIL000000069", smallProducerLitres.head)
        val sparkyJuice = SmallProducer("Sparky Juice Co", "XCSDIL000000070", smallProducerLitres.last)
        List(superCola, sparkyJuice)
      case _ => List.empty
    }
    emptyUserAnswers.copy(data = data, smallProducerList = smallProducerList, returnPeriod = returnPeriod)
  }
  

  "TotalForQuarter" - {

    val posLitresInts = Gen.choose(1000, 10000000)
    val janToMarInt = Gen.choose(1, 3)
    val aprToDecInt = Gen.choose(4, 12)

    (2018 to 2024).foreach(year => {

      val lowerBandCostPerLitre = BigDecimal("0.18")
      val higherBandCostPerLitre = BigDecimal("0.24")

      //    val total = litresBroughtIntoTheUk + litresAsContractPacker
      //    val totalCredits = litresExported + litresLostOrDamaged
      //      smallProducer match {
      //        case true => (total - totalCredits) * lowBandCostPerLitre
      //        case _ => (total + litresPackedAtOwnSite - totalCredits) * lowBandCostPerLitre
      //      }
      //      TODO: One for each of lines below - test all three values at once, add in random values for small producer list - use CYAControllerSpec as guide on how to set these

      List(true, false).foreach(isSmallProducer => {

        s"calculate low levy, high levy, and total correctly with non-zero litres totals ${ if(isSmallProducer) "for small producer " else "" }with litres packed at own site using original rates for Apr - Dec $year" in {
          forAll(posLitresInts) { lowLitres =>
            forAll(posLitresInts) { highLitres =>
              forAll(aprToDecInt) { month =>
                val returnPeriod = ReturnPeriod(LocalDate.of(year, month, 1))
                val userAnswers = userAnswersData(ownBrandsLitres = Option((lowLitres, highLitres)), returnPeriod = returnPeriod)
                val lowBand = calculateLowBand(userAnswers, isSmallProducer)(frontendAppConfig)
                val highBand = calculateHighBand(userAnswers, isSmallProducer)(frontendAppConfig)
                val totalForQuarter = calculateTotal(userAnswers, isSmallProducer)(frontendAppConfig)
                val expectedLowLevy = if (isSmallProducer) BigDecimal("0.00") else lowerBandCostPerLitre * lowLitres
                val expectedHighLevy = if (isSmallProducer) BigDecimal("0.00") else higherBandCostPerLitre * highLitres
                lowBand mustBe expectedLowLevy
                highBand mustBe expectedHighLevy
                totalForQuarter mustBe expectedLowLevy + expectedHighLevy
              }
            }
          }
        }

        s"calculate low levy, high levy, and total correctly with non-zero litres totals ${ if(isSmallProducer) "for small producer " else "" }with litres contract packed using original rates for Apr - Dec $year" in {
          forAll(posLitresInts) { lowLitres =>
            forAll(posLitresInts) { highLitres =>
              forAll(aprToDecInt) { month =>
                val returnPeriod = ReturnPeriod(LocalDate.of(year, month, 1))
                val userAnswers = userAnswersData(contractPackerLitres = Option((lowLitres, highLitres)), returnPeriod = returnPeriod)
                val lowBand = calculateLowBand(userAnswers, isSmallProducer)(frontendAppConfig)
                val highBand = calculateHighBand(userAnswers, isSmallProducer)(frontendAppConfig)
                val totalForQuarter = calculateTotal(userAnswers, isSmallProducer)(frontendAppConfig)
                val expectedLowLevy = lowerBandCostPerLitre * lowLitres
                val expectedHighLevy = higherBandCostPerLitre * highLitres
                lowBand mustBe expectedLowLevy
                highBand mustBe expectedHighLevy
                totalForQuarter mustBe expectedLowLevy + expectedHighLevy
              }
            }
          }
        }

        s"calculate low levy, high levy, and total correctly with non-zero litres totals ${ if(isSmallProducer) "for small producer " else "" }with exemptions for small producers using original rates for Apr - Dec $year" in {
          forAll(posLitresInts) { lowLitres =>
            forAll(posLitresInts) { highLitres =>
              forAll(aprToDecInt) { month =>
                val returnPeriod = ReturnPeriod(LocalDate.of(year, month, 1))
                val smallProducerLitresOne: (Long, Long) = (lowLitres, highLitres)
                val smallProducerLitresTwo: (Long, Long) = (Math.floor((1 + Math.random()) * lowLitres / 1.5).toLong, Math.floor((1 + Math.random()) * highLitres / 1.5).toLong)
                val userAnswers = userAnswersData(smallProducerLitres = List(smallProducerLitresOne, smallProducerLitresTwo), returnPeriod = returnPeriod)
                val lowBand = calculateLowBand(userAnswers, isSmallProducer)(frontendAppConfig)
                val highBand = calculateHighBand(userAnswers, isSmallProducer)(frontendAppConfig)
                val totalForQuarter = calculateTotal(userAnswers, isSmallProducer)(frontendAppConfig)
                val expectedLowLevy = BigDecimal("0.00")
                val expectedHighLevy = BigDecimal("0.00")
                lowBand mustBe expectedLowLevy
                highBand mustBe expectedHighLevy
                totalForQuarter mustBe expectedLowLevy + expectedHighLevy
              }
            }
          }
        }

        s"calculate low levy, high levy, and total correctly with non-zero litres totals ${ if(isSmallProducer) "for small producer " else "" }with litres brought into the uk using original rates for Apr - Dec $year" in {
          forAll(posLitresInts) { lowLitres =>
            forAll(posLitresInts) { highLitres =>
              forAll(aprToDecInt) { month =>
                val returnPeriod = ReturnPeriod(LocalDate.of(year, month, 1))
                val userAnswers = userAnswersData(broughtIntoUKLitres = Option((lowLitres, highLitres)), returnPeriod = returnPeriod)
                val lowBand = calculateLowBand(userAnswers, isSmallProducer)(frontendAppConfig)
                val highBand = calculateHighBand(userAnswers, isSmallProducer)(frontendAppConfig)
                val totalForQuarter = calculateTotal(userAnswers, isSmallProducer)(frontendAppConfig)
                val expectedLowLevy = lowerBandCostPerLitre * lowLitres
                val expectedHighLevy = higherBandCostPerLitre * highLitres
                lowBand mustBe expectedLowLevy
                highBand mustBe expectedHighLevy
                totalForQuarter mustBe expectedLowLevy + expectedHighLevy
              }
            }
          }
        }

        s"calculate low levy, high levy, and total correctly with non-zero litres totals ${ if(isSmallProducer) "for small producer " else "" }with litres brought into the uk from small producers using original rates for Apr - Dec $year" in {
          forAll(posLitresInts) { lowLitres =>
            forAll(posLitresInts) { highLitres =>
              forAll(aprToDecInt) { month =>
                val returnPeriod = ReturnPeriod(LocalDate.of(year, month, 1))
                val userAnswers = userAnswersData(broughtIntoUkFromSmallProducersLitres = Option((lowLitres, highLitres)), returnPeriod = returnPeriod)
                val lowBand = calculateLowBand(userAnswers, isSmallProducer)(frontendAppConfig)
                val highBand = calculateHighBand(userAnswers, isSmallProducer)(frontendAppConfig)
                val totalForQuarter = calculateTotal(userAnswers, isSmallProducer)(frontendAppConfig)
                val expectedLowLevy = BigDecimal("0.00")
                val expectedHighLevy = BigDecimal("0.00")
                lowBand mustBe expectedLowLevy
                highBand mustBe expectedHighLevy
                totalForQuarter mustBe expectedLowLevy + expectedHighLevy
              }
            }
          }
        }

        s"calculate low levy, high levy, and total correctly with non-zero litres totals ${ if(isSmallProducer) "for small producer " else "" }with credits for litres exported using original rates for Apr - Dec $year" in {
          forAll(posLitresInts) { lowLitres =>
            forAll(posLitresInts) { highLitres =>
              forAll(aprToDecInt) { month =>
                val returnPeriod = ReturnPeriod(LocalDate.of(year, month, 1))
                val userAnswers = userAnswersData(claimCreditsForExportsLitres = Option((lowLitres, highLitres)), returnPeriod = returnPeriod)
                val lowBand = calculateLowBand(userAnswers, isSmallProducer)(frontendAppConfig)
                val highBand = calculateHighBand(userAnswers, isSmallProducer)(frontendAppConfig)
                val totalForQuarter = calculateTotal(userAnswers, isSmallProducer)(frontendAppConfig)
                val expectedLowLevy = -1 * lowerBandCostPerLitre * lowLitres
                val expectedHighLevy = -1 * higherBandCostPerLitre * highLitres
                lowBand mustBe expectedLowLevy
                highBand mustBe expectedHighLevy
                totalForQuarter mustBe expectedLowLevy + expectedHighLevy
              }
            }
          }
        }

        s"calculate low levy, high levy, and total correctly with non-zero litres totals ${ if(isSmallProducer) "for small producer " else "" }with credits for litres lost or damaged using original rates for Apr - Dec $year" in {
          forAll(posLitresInts) { lowLitres =>
            forAll(posLitresInts) { highLitres =>
              forAll(aprToDecInt) { month =>
                val returnPeriod = ReturnPeriod(LocalDate.of(year, month, 1))
                val userAnswers = userAnswersData(claimCreditsForLostDamagedLitres = Option((lowLitres, highLitres)), returnPeriod = returnPeriod)
                val lowBand = calculateLowBand(userAnswers, isSmallProducer)(frontendAppConfig)
                val highBand = calculateHighBand(userAnswers, isSmallProducer)(frontendAppConfig)
                val totalForQuarter = calculateTotal(userAnswers, isSmallProducer)(frontendAppConfig)
                val expectedLowLevy = -1 * lowerBandCostPerLitre * lowLitres
                val expectedHighLevy = -1 * higherBandCostPerLitre * highLitres
                lowBand mustBe expectedLowLevy
                highBand mustBe expectedHighLevy
                totalForQuarter mustBe expectedLowLevy + expectedHighLevy
              }
            }
          }
        }

        s"calculate low levy, high levy, and total correctly with non-zero litres totals ${ if(isSmallProducer) "for small producer " else "" }when return amount is 0 using original rates for Apr - Dec $year" in {
          forAll(aprToDecInt) { month =>
            val ownBrandsLitres: Option[(Long, Long)] = if (isSmallProducer) Option(getRandomLitreage) else None
            val contractPackerLitres: Option[(Long, Long)] = None
            val broughtIntoUKLitres: Option[(Long, Long)] = None
            val broughtIntoUkFromSmallProducersLitres: Option[(Long, Long)] = Option(getRandomLitreage)
            val claimCreditsForExportsLitres: Option[(Long, Long)] = None
            val claimCreditsForLostDamagedLitres: Option[(Long, Long)] = None
            val smallProducerLitres: List[(Long, Long)] = List(getRandomLitreage, getRandomLitreage)
            val returnPeriod = ReturnPeriod(LocalDate.of(year, month, 1))
            val userAnswers = userAnswersData(ownBrandsLitres, contractPackerLitres, broughtIntoUKLitres, broughtIntoUkFromSmallProducersLitres, claimCreditsForExportsLitres, claimCreditsForLostDamagedLitres, smallProducerLitres, returnPeriod)
            val lowBand = calculateLowBand(userAnswers, isSmallProducer)(frontendAppConfig)
            val highBand = calculateHighBand(userAnswers, isSmallProducer)(frontendAppConfig)
            val totalForQuarter = calculateTotal(userAnswers, isSmallProducer)(frontendAppConfig)
            val expectedLowLevy = BigDecimal("0.00")
            val expectedHighLevy = BigDecimal("0.00")
            lowBand mustBe expectedLowLevy
            highBand mustBe expectedHighLevy
            totalForQuarter mustBe expectedLowLevy + expectedHighLevy
          }
        }

        s"calculate low levy, high levy, and total correctly with non-zero litres totals ${ if(isSmallProducer) "for small producer " else "" }when return amount to pay using original rates for Apr - Dec $year" in {

        }

        s"calculate low levy, high levy, and total correctly with non-zero litres totals ${ if(isSmallProducer) "for small producer " else "" }when return amount is negative using original rates for Apr - Dec $year" in {

        }

        s"calculate low levy, high levy, and total correctly with non-zero litres totals ${ if(isSmallProducer) "for small producer " else "" }with litres packed at own site using original rates for Jan - Mar ${year + 1}" in {
          forAll(posLitresInts) { lowLitres =>
            forAll(posLitresInts) { highLitres =>
              forAll(janToMarInt) { month =>
                val returnPeriod = ReturnPeriod(LocalDate.of(year + 1, month, 1))
                val userAnswers = userAnswersData(ownBrandsLitres = Option((lowLitres, highLitres)), returnPeriod = returnPeriod)
                val lowBand = calculateLowBand(userAnswers, isSmallProducer)(frontendAppConfig)
                val highBand = calculateHighBand(userAnswers, isSmallProducer)(frontendAppConfig)
                val totalForQuarter = calculateTotal(userAnswers, isSmallProducer)(frontendAppConfig)
                val expectedLowLevy = if (isSmallProducer) BigDecimal("0.00") else lowerBandCostPerLitre * lowLitres
                val expectedHighLevy = if (isSmallProducer) BigDecimal("0.00") else higherBandCostPerLitre * highLitres
                lowBand mustBe expectedLowLevy
                highBand mustBe expectedHighLevy
                totalForQuarter mustBe expectedLowLevy + expectedHighLevy
              }
            }
          }
        }

        s"calculate low levy, high levy, and total correctly with non-zero litres totals ${ if(isSmallProducer) "for small producer " else "" }with litres contract packed using original rates for Jan - Mar ${year + 1}" in {
          forAll(posLitresInts) { lowLitres =>
            forAll(posLitresInts) { highLitres =>
              forAll(janToMarInt) { month =>
                val returnPeriod = ReturnPeriod(LocalDate.of(year + 1, month, 1))
                val userAnswers = userAnswersData(contractPackerLitres = Option((lowLitres, highLitres)), returnPeriod = returnPeriod)
                val lowBand = calculateLowBand(userAnswers, isSmallProducer)(frontendAppConfig)
                val highBand = calculateHighBand(userAnswers, isSmallProducer)(frontendAppConfig)
                val totalForQuarter = calculateTotal(userAnswers, isSmallProducer)(frontendAppConfig)
                val expectedLowLevy = lowerBandCostPerLitre * lowLitres
                val expectedHighLevy = higherBandCostPerLitre * highLitres
                lowBand mustBe expectedLowLevy
                highBand mustBe expectedHighLevy
                totalForQuarter mustBe expectedLowLevy + expectedHighLevy
              }
            }
          }
        }

        s"calculate low levy, high levy, and total correctly with non-zero litres totals ${ if(isSmallProducer) "for small producer " else "" }with exemptions for small producers using original rates for Jan - Mar ${year + 1}" in {
          forAll(posLitresInts) { lowLitres =>
            forAll(posLitresInts) { highLitres =>
              forAll(janToMarInt) { month =>
                val returnPeriod = ReturnPeriod(LocalDate.of(year + 1, month, 1))
                val smallProducerLitresOne: (Long, Long) = (lowLitres, highLitres)
                val smallProducerLitresTwo: (Long, Long) = (Math.floor((1 + Math.random()) * lowLitres / 1.5).toLong, Math.floor((1 + Math.random()) * highLitres / 1.5).toLong)
                val userAnswers = userAnswersData(smallProducerLitres = List(smallProducerLitresOne, smallProducerLitresTwo), returnPeriod = returnPeriod)
                val lowBand = calculateLowBand(userAnswers, isSmallProducer)(frontendAppConfig)
                val highBand = calculateHighBand(userAnswers, isSmallProducer)(frontendAppConfig)
                val totalForQuarter = calculateTotal(userAnswers, isSmallProducer)(frontendAppConfig)
                val expectedLowLevy = BigDecimal("0.00")
                val expectedHighLevy = BigDecimal("0.00")
                lowBand mustBe expectedLowLevy
                highBand mustBe expectedHighLevy
                totalForQuarter mustBe expectedLowLevy + expectedHighLevy
              }
            }
          }
        }

        s"calculate low levy, high levy, and total correctly with non-zero litres totals ${ if(isSmallProducer) "for small producer " else "" }with litres brought into the uk using original rates for Jan - Mar ${year + 1}" in {
          forAll(posLitresInts) { lowLitres =>
            forAll(posLitresInts) { highLitres =>
              forAll(janToMarInt) { month =>
                val returnPeriod = ReturnPeriod(LocalDate.of(year + 1, month, 1))
                val userAnswers = userAnswersData(broughtIntoUKLitres = Option((lowLitres, highLitres)), returnPeriod = returnPeriod)
                val lowBand = calculateLowBand(userAnswers, isSmallProducer)(frontendAppConfig)
                val highBand = calculateHighBand(userAnswers, isSmallProducer)(frontendAppConfig)
                val totalForQuarter = calculateTotal(userAnswers, isSmallProducer)(frontendAppConfig)
                val expectedLowLevy = lowerBandCostPerLitre * lowLitres
                val expectedHighLevy = higherBandCostPerLitre * highLitres
                lowBand mustBe expectedLowLevy
                highBand mustBe expectedHighLevy
                totalForQuarter mustBe expectedLowLevy + expectedHighLevy
              }
            }
          }
        }

        s"calculate low levy, high levy, and total correctly with non-zero litres totals ${ if(isSmallProducer) "for small producer " else "" }with litres brought into the uk from small producers using original rates for Jan - Mar ${year + 1}" in {
          forAll(posLitresInts) { lowLitres =>
            forAll(posLitresInts) { highLitres =>
              forAll(janToMarInt) { month =>
                val returnPeriod = ReturnPeriod(LocalDate.of(year + 1, month, 1))
                val userAnswers = userAnswersData(broughtIntoUkFromSmallProducersLitres = Option((lowLitres, highLitres)), returnPeriod = returnPeriod)
                val lowBand = calculateLowBand(userAnswers, isSmallProducer)(frontendAppConfig)
                val highBand = calculateHighBand(userAnswers, isSmallProducer)(frontendAppConfig)
                val totalForQuarter = calculateTotal(userAnswers, isSmallProducer)(frontendAppConfig)
                val expectedLowLevy = BigDecimal("0.00")
                val expectedHighLevy = BigDecimal("0.00")
                lowBand mustBe expectedLowLevy
                highBand mustBe expectedHighLevy
                totalForQuarter mustBe expectedLowLevy + expectedHighLevy
              }
            }
          }
        }

        s"calculate low levy, high levy, and total correctly with non-zero litres totals ${ if(isSmallProducer) "for small producer " else "" }with credits for litres exported using original rates for Jan - Mar ${year + 1}" in {
          forAll(posLitresInts) { lowLitres =>
            forAll(posLitresInts) { highLitres =>
              forAll(janToMarInt) { month =>
                val returnPeriod = ReturnPeriod(LocalDate.of(year + 1, month, 1))
                val userAnswers = userAnswersData(claimCreditsForExportsLitres = Option((lowLitres, highLitres)), returnPeriod = returnPeriod)
                val lowBand = calculateLowBand(userAnswers, isSmallProducer)(frontendAppConfig)
                val highBand = calculateHighBand(userAnswers, isSmallProducer)(frontendAppConfig)
                val totalForQuarter = calculateTotal(userAnswers, isSmallProducer)(frontendAppConfig)
                val expectedLowLevy = -1 * lowerBandCostPerLitre * lowLitres
                val expectedHighLevy = -1 * higherBandCostPerLitre * highLitres
                lowBand mustBe expectedLowLevy
                highBand mustBe expectedHighLevy
                totalForQuarter mustBe expectedLowLevy + expectedHighLevy
              }
            }
          }
        }

        s"calculate low levy, high levy, and total correctly with non-zero litres totals ${ if(isSmallProducer) "for small producer " else "" }with credits for litres lost or damaged using original rates for Jan - Mar ${year + 1}" in {
          forAll(posLitresInts) { lowLitres =>
            forAll(posLitresInts) { highLitres =>
              forAll(janToMarInt) { month =>
                val returnPeriod = ReturnPeriod(LocalDate.of(year + 1, month, 1))
                val userAnswers = userAnswersData(claimCreditsForLostDamagedLitres = Option((lowLitres, highLitres)), returnPeriod = returnPeriod)
                val lowBand = calculateLowBand(userAnswers, isSmallProducer)(frontendAppConfig)
                val highBand = calculateHighBand(userAnswers, isSmallProducer)(frontendAppConfig)
                val totalForQuarter = calculateTotal(userAnswers, isSmallProducer)(frontendAppConfig)
                val expectedLowLevy = -1 * lowerBandCostPerLitre * lowLitres
                val expectedHighLevy = -1 * higherBandCostPerLitre * highLitres
                lowBand mustBe expectedLowLevy
                highBand mustBe expectedHighLevy
                totalForQuarter mustBe expectedLowLevy + expectedHighLevy
              }
            }
          }
        }

        s"calculate low levy, high levy, and total correctly with non-zero litres totals ${ if(isSmallProducer) "for small producer " else "" }when return amount is 0 using original rates for Jan - Mar ${year + 1}" in {
          forAll(janToMarInt) { month =>
            val ownBrandsLitres: Option[(Long, Long)] = if (isSmallProducer) Option(getRandomLitreage) else None
            val contractPackerLitres: Option[(Long, Long)] = None
            val broughtIntoUKLitres: Option[(Long, Long)] = None
            val broughtIntoUkFromSmallProducersLitres: Option[(Long, Long)] = Option(getRandomLitreage)
            val claimCreditsForExportsLitres: Option[(Long, Long)] = None
            val claimCreditsForLostDamagedLitres: Option[(Long, Long)] = None
            val smallProducerLitres: List[(Long, Long)] = List(getRandomLitreage, getRandomLitreage)
            val returnPeriod = ReturnPeriod(LocalDate.of(year + 1, month, 1))
            val userAnswers = userAnswersData(ownBrandsLitres, contractPackerLitres, broughtIntoUKLitres, broughtIntoUkFromSmallProducersLitres, claimCreditsForExportsLitres, claimCreditsForLostDamagedLitres, smallProducerLitres, returnPeriod)
            val lowBand = calculateLowBand(userAnswers, isSmallProducer)(frontendAppConfig)
            val highBand = calculateHighBand(userAnswers, isSmallProducer)(frontendAppConfig)
            val totalForQuarter = calculateTotal(userAnswers, isSmallProducer)(frontendAppConfig)
            val expectedLowLevy = BigDecimal("0.00")
            val expectedHighLevy = BigDecimal("0.00")
            lowBand mustBe expectedLowLevy
            highBand mustBe expectedHighLevy
            totalForQuarter mustBe expectedLowLevy + expectedHighLevy
          }
        }

        s"calculate low levy, high levy, and total correctly with non-zero litres totals ${ if(isSmallProducer) "for small producer " else "" }when return amount to pay using original rates for Jan - Mar ${year + 1}" in {

        }

        s"calculate low levy, high levy, and total correctly with non-zero litres totals ${ if(isSmallProducer) "for small producer " else "" }when return amount is negative using original rates for Jan - Mar ${year + 1}" in {

        }
      })
    })

    (2025 to 2025).foreach(year => {

      val lowerBandCostPerLitreMap: Map[Int, BigDecimal] = Map(2025 -> BigDecimal("0.194"))
      val higherBandCostPerLitreMap: Map[Int, BigDecimal] = Map(2025 -> BigDecimal("0.259"))

      List(true, false).foreach(isSmallProducer => {

        s"calculate low levy, high levy, and total correctly with non-zero litres totals ${ if(isSmallProducer) "for small producer " else "" }with litres packed at own site using $year rates for Apr - Dec $year" in {
          forAll(posLitresInts) { lowLitres =>
            forAll(posLitresInts) { highLitres =>
              forAll(aprToDecInt) { month =>
                val returnPeriod = ReturnPeriod(LocalDate.of(year, month, 1))
                val userAnswers = userAnswersData(ownBrandsLitres = Option((lowLitres, highLitres)), returnPeriod = returnPeriod)
                val lowBand = calculateLowBand(userAnswers, isSmallProducer)(frontendAppConfig)
                val highBand = calculateHighBand(userAnswers, isSmallProducer)(frontendAppConfig)
                val totalForQuarter = calculateTotal(userAnswers, isSmallProducer)(frontendAppConfig)
                val expectedLowLevy = if (isSmallProducer) BigDecimal("0.00") else lowerBandCostPerLitreMap(year) * lowLitres
                val expectedHighLevy = if (isSmallProducer) BigDecimal("0.00") else higherBandCostPerLitreMap(year) * highLitres
                lowBand mustBe expectedLowLevy.setScale(2, BigDecimal.RoundingMode.HALF_UP)
                highBand mustBe expectedHighLevy.setScale(2, BigDecimal.RoundingMode.HALF_UP)
                totalForQuarter mustBe (expectedLowLevy + expectedHighLevy).setScale(2, BigDecimal.RoundingMode.HALF_UP)
              }
            }
          }
        }

        s"calculate low levy, high levy, and total correctly with non-zero litres totals ${ if(isSmallProducer) "for small producer " else "" }with litres contract packed using $year rates for Apr - Dec $year" in {
          forAll(posLitresInts) { lowLitres =>
            forAll(posLitresInts) { highLitres =>
              forAll(aprToDecInt) { month =>
                val returnPeriod = ReturnPeriod(LocalDate.of(year, month, 1))
                val userAnswers = userAnswersData(contractPackerLitres = Option((lowLitres, highLitres)), returnPeriod = returnPeriod)
                val lowBand = calculateLowBand(userAnswers, isSmallProducer)(frontendAppConfig)
                val highBand = calculateHighBand(userAnswers, isSmallProducer)(frontendAppConfig)
                val totalForQuarter = calculateTotal(userAnswers, isSmallProducer)(frontendAppConfig)
                val expectedLowLevy = lowerBandCostPerLitreMap(year) * lowLitres
                val expectedHighLevy = higherBandCostPerLitreMap(year) * highLitres
                lowBand mustBe expectedLowLevy.setScale(2, BigDecimal.RoundingMode.HALF_UP)
                highBand mustBe expectedHighLevy.setScale(2, BigDecimal.RoundingMode.HALF_UP)
                totalForQuarter mustBe (expectedLowLevy + expectedHighLevy).setScale(2, BigDecimal.RoundingMode.HALF_UP)
              }
            }
          }
        }

        s"calculate low levy, high levy, and total correctly with non-zero litres totals ${ if(isSmallProducer) "for small producer " else "" }with exemptions for small producers using $year rates for Apr - Dec $year" in {
          forAll(posLitresInts) { lowLitres =>
            forAll(posLitresInts) { highLitres =>
              forAll(aprToDecInt) { month =>
                val returnPeriod = ReturnPeriod(LocalDate.of(year, month, 1))
                val smallProducerLitresOne: (Long, Long) = (lowLitres, highLitres)
                val smallProducerLitresTwo: (Long, Long) = (Math.floor((1 + Math.random()) * lowLitres / 1.5).toLong, Math.floor((1 + Math.random()) * highLitres / 1.5).toLong)
                val userAnswers = userAnswersData(smallProducerLitres = List(smallProducerLitresOne, smallProducerLitresTwo), returnPeriod = returnPeriod)
                val lowBand = calculateLowBand(userAnswers, isSmallProducer)(frontendAppConfig)
                val highBand = calculateHighBand(userAnswers, isSmallProducer)(frontendAppConfig)
                val totalForQuarter = calculateTotal(userAnswers, isSmallProducer)(frontendAppConfig)
                val expectedLowLevy = if (isSmallProducer) BigDecimal("0.00") else lowerBandCostPerLitreMap(year) * lowLitres
                val expectedHighLevy = if (isSmallProducer) BigDecimal("0.00") else higherBandCostPerLitreMap(year) * highLitres
                lowBand mustBe expectedLowLevy.setScale(2, BigDecimal.RoundingMode.HALF_UP)
                highBand mustBe expectedHighLevy.setScale(2, BigDecimal.RoundingMode.HALF_UP)
                totalForQuarter mustBe (expectedLowLevy + expectedHighLevy).setScale(2, BigDecimal.RoundingMode.HALF_UP)
              }
            }
          }
        }

        s"calculate low levy, high levy, and total correctly with non-zero litres totals ${ if(isSmallProducer) "for small producer " else "" }with litres brought into the uk using $year rates for Apr - Dec $year" in {
          forAll(posLitresInts) { lowLitres =>
            forAll(posLitresInts) { highLitres =>
              forAll(aprToDecInt) { month =>
                val returnPeriod = ReturnPeriod(LocalDate.of(year, month, 1))
                val userAnswers = userAnswersData(broughtIntoUKLitres = Option((lowLitres, highLitres)), returnPeriod = returnPeriod)
                val lowBand = calculateLowBand(userAnswers, isSmallProducer)(frontendAppConfig)
                val highBand = calculateHighBand(userAnswers, isSmallProducer)(frontendAppConfig)
                val totalForQuarter = calculateTotal(userAnswers, isSmallProducer)(frontendAppConfig)
                val expectedLowLevy = lowerBandCostPerLitreMap(year) * lowLitres
                val expectedHighLevy = higherBandCostPerLitreMap(year) * highLitres
                lowBand mustBe expectedLowLevy.setScale(2, BigDecimal.RoundingMode.HALF_UP)
                highBand mustBe expectedHighLevy.setScale(2, BigDecimal.RoundingMode.HALF_UP)
                totalForQuarter mustBe (expectedLowLevy + expectedHighLevy).setScale(2, BigDecimal.RoundingMode.HALF_UP)
              }
            }
          }
        }

        s"calculate low levy, high levy, and total correctly with non-zero litres totals ${ if(isSmallProducer) "for small producer " else "" }with litres brought into the uk from small producers using $year rates for Apr - Dec $year" in {
          forAll(posLitresInts) { lowLitres =>
            forAll(posLitresInts) { highLitres =>
              forAll(aprToDecInt) { month =>
                val returnPeriod = ReturnPeriod(LocalDate.of(year, month, 1))
                val userAnswers = userAnswersData(broughtIntoUkFromSmallProducersLitres = Option((lowLitres, highLitres)), returnPeriod = returnPeriod)
                val lowBand = calculateLowBand(userAnswers, isSmallProducer)(frontendAppConfig)
                val highBand = calculateHighBand(userAnswers, isSmallProducer)(frontendAppConfig)
                val totalForQuarter = calculateTotal(userAnswers, isSmallProducer)(frontendAppConfig)
                val expectedLowLevy = BigDecimal("0.00")
                val expectedHighLevy = BigDecimal("0.00")
                lowBand mustBe expectedLowLevy.setScale(2, BigDecimal.RoundingMode.HALF_UP)
                highBand mustBe expectedHighLevy.setScale(2, BigDecimal.RoundingMode.HALF_UP)
                totalForQuarter mustBe (expectedLowLevy + expectedHighLevy).setScale(2, BigDecimal.RoundingMode.HALF_UP)
              }
            }
          }
        }

        s"calculate low levy, high levy, and total correctly with non-zero litres totals ${ if(isSmallProducer) "for small producer " else "" }with credits for litres exported using $year rates for Apr - Dec $year" in {
          forAll(posLitresInts) { lowLitres =>
            forAll(posLitresInts) { highLitres =>
              forAll(aprToDecInt) { month =>
                val returnPeriod = ReturnPeriod(LocalDate.of(year, month, 1))
                val userAnswers = userAnswersData(claimCreditsForExportsLitres = Option((lowLitres, highLitres)), returnPeriod = returnPeriod)
                val lowBand = calculateLowBand(userAnswers, isSmallProducer)(frontendAppConfig)
                val highBand = calculateHighBand(userAnswers, isSmallProducer)(frontendAppConfig)
                val totalForQuarter = calculateTotal(userAnswers, isSmallProducer)(frontendAppConfig)
                val expectedLowLevy = -1 * lowerBandCostPerLitreMap(year) * lowLitres
                val expectedHighLevy = -1 * higherBandCostPerLitreMap(year) * highLitres
                lowBand mustBe expectedLowLevy.setScale(2, BigDecimal.RoundingMode.HALF_UP)
                highBand mustBe expectedHighLevy.setScale(2, BigDecimal.RoundingMode.HALF_UP)
                totalForQuarter mustBe (expectedLowLevy + expectedHighLevy).setScale(2, BigDecimal.RoundingMode.HALF_UP)
              }
            }
          }
        }

        s"calculate low levy, high levy, and total correctly with non-zero litres totals ${ if(isSmallProducer) "for small producer " else "" }with credits for litres lost or damaged using $year rates for Apr - Dec $year" in {
          forAll(posLitresInts) { lowLitres =>
            forAll(posLitresInts) { highLitres =>
              forAll(aprToDecInt) { month =>
                val returnPeriod = ReturnPeriod(LocalDate.of(year, month, 1))
                val userAnswers = userAnswersData(claimCreditsForLostDamagedLitres = Option((lowLitres, highLitres)), returnPeriod = returnPeriod)
                val lowBand = calculateLowBand(userAnswers, isSmallProducer)(frontendAppConfig)
                val highBand = calculateHighBand(userAnswers, isSmallProducer)(frontendAppConfig)
                val totalForQuarter = calculateTotal(userAnswers, isSmallProducer)(frontendAppConfig)
                val expectedLowLevy = -1 * lowerBandCostPerLitreMap(year) * lowLitres
                val expectedHighLevy = -1 * higherBandCostPerLitreMap(year) * highLitres
                lowBand mustBe expectedLowLevy.setScale(2, BigDecimal.RoundingMode.HALF_UP)
                highBand mustBe expectedHighLevy.setScale(2, BigDecimal.RoundingMode.HALF_UP)
                totalForQuarter mustBe (expectedLowLevy + expectedHighLevy).setScale(2, BigDecimal.RoundingMode.HALF_UP)
              }
            }
          }
        }

        s"calculate low levy, high levy, and total correctly with non-zero litres totals ${ if(isSmallProducer) "for small producer " else "" }when return amount is 0 using $year rates for Apr - Dec $year" in {
          forAll(aprToDecInt) { month =>
            val ownBrandsLitres: Option[(Long, Long)] = if (isSmallProducer) Option(getRandomLitreage) else None
            val contractPackerLitres: Option[(Long, Long)] = None
            val broughtIntoUKLitres: Option[(Long, Long)] = None
            val broughtIntoUkFromSmallProducersLitres: Option[(Long, Long)] = Option(getRandomLitreage)
            val claimCreditsForExportsLitres: Option[(Long, Long)] = None
            val claimCreditsForLostDamagedLitres: Option[(Long, Long)] = None
            val smallProducerLitres: List[(Long, Long)] = List(getRandomLitreage, getRandomLitreage)
            val returnPeriod = ReturnPeriod(LocalDate.of(year, month, 1))
            val userAnswers = userAnswersData(ownBrandsLitres, contractPackerLitres, broughtIntoUKLitres, broughtIntoUkFromSmallProducersLitres, claimCreditsForExportsLitres, claimCreditsForLostDamagedLitres, smallProducerLitres, returnPeriod)
            val lowBand = calculateLowBand(userAnswers, isSmallProducer)(frontendAppConfig)
            val highBand = calculateHighBand(userAnswers, isSmallProducer)(frontendAppConfig)
            val totalForQuarter = calculateTotal(userAnswers, isSmallProducer)(frontendAppConfig)
            val expectedLowLevy = BigDecimal("0.00")
            val expectedHighLevy = BigDecimal("0.00")
            lowBand mustBe expectedLowLevy
            highBand mustBe expectedHighLevy
            totalForQuarter mustBe expectedLowLevy + expectedHighLevy
          }
        }

        s"calculate low levy, high levy, and total correctly with non-zero litres totals ${ if(isSmallProducer) "for small producer " else "" }when return amount to pay using $year rates for Apr - Dec $year" in {

        }

        s"calculate low levy, high levy, and total correctly with non-zero litres totals ${ if(isSmallProducer) "for small producer " else "" }when return amount is negative using $year rates for Apr - Dec $year" in {

        }

        s"calculate low levy, high levy, and total correctly with non-zero litres totals ${ if(isSmallProducer) "for small producer " else "" }with litres packed at own site using $year rates for Jan - Mar ${year + 1}" in {
          forAll(posLitresInts) { lowLitres =>
            forAll(posLitresInts) { highLitres =>
              forAll(aprToDecInt) { month =>
                val returnPeriod = ReturnPeriod(LocalDate.of(year, month, 1))
                val userAnswers = userAnswersData(ownBrandsLitres = Option((lowLitres, highLitres)), returnPeriod = returnPeriod)
                val lowBand = calculateLowBand(userAnswers, isSmallProducer)(frontendAppConfig)
                val highBand = calculateHighBand(userAnswers, isSmallProducer)(frontendAppConfig)
                val totalForQuarter = calculateTotal(userAnswers, isSmallProducer)(frontendAppConfig)
                val expectedLowLevy = if (isSmallProducer) BigDecimal("0.00") else lowerBandCostPerLitreMap(year) * lowLitres
                val expectedHighLevy = if (isSmallProducer) BigDecimal("0.00") else higherBandCostPerLitreMap(year) * highLitres
                lowBand mustBe expectedLowLevy.setScale(2, BigDecimal.RoundingMode.HALF_UP)
                highBand mustBe expectedHighLevy.setScale(2, BigDecimal.RoundingMode.HALF_UP)
                totalForQuarter mustBe (expectedLowLevy + expectedHighLevy).setScale(2, BigDecimal.RoundingMode.HALF_UP)
              }
            }
          }
        }

        s"calculate low levy, high levy, and total correctly with non-zero litres totals ${ if(isSmallProducer) "for small producer " else "" }with litres contract packed using $year rates for Jan - Mar ${year + 1}" in {
          forAll(posLitresInts) { lowLitres =>
            forAll(posLitresInts) { highLitres =>
              forAll(aprToDecInt) { month =>
                val returnPeriod = ReturnPeriod(LocalDate.of(year, month, 1))
                val userAnswers = userAnswersData(contractPackerLitres = Option((lowLitres, highLitres)), returnPeriod = returnPeriod)
                val lowBand = calculateLowBand(userAnswers, isSmallProducer)(frontendAppConfig)
                val highBand = calculateHighBand(userAnswers, isSmallProducer)(frontendAppConfig)
                val totalForQuarter = calculateTotal(userAnswers, isSmallProducer)(frontendAppConfig)
                val expectedLowLevy = lowerBandCostPerLitreMap(year) * lowLitres
                val expectedHighLevy = higherBandCostPerLitreMap(year) * highLitres
                lowBand mustBe expectedLowLevy.setScale(2, BigDecimal.RoundingMode.HALF_UP)
                highBand mustBe expectedHighLevy.setScale(2, BigDecimal.RoundingMode.HALF_UP)
                totalForQuarter mustBe (expectedLowLevy + expectedHighLevy).setScale(2, BigDecimal.RoundingMode.HALF_UP)
              }
            }
          }
        }

        s"calculate low levy, high levy, and total correctly with non-zero litres totals ${ if(isSmallProducer) "for small producer " else "" }with exemptions for small producers using $year rates for Jan - Mar ${year + 1}" in {
          forAll(posLitresInts) { lowLitres =>
            forAll(posLitresInts) { highLitres =>
              forAll(aprToDecInt) { month =>
                val returnPeriod = ReturnPeriod(LocalDate.of(year, month, 1))
                val smallProducerLitresOne: (Long, Long) = (lowLitres, highLitres)
                val smallProducerLitresTwo: (Long, Long) = (Math.floor((1 + Math.random()) * lowLitres / 1.5).toLong, Math.floor((1 + Math.random()) * highLitres / 1.5).toLong)
                val userAnswers = userAnswersData(smallProducerLitres = List(smallProducerLitresOne, smallProducerLitresTwo), returnPeriod = returnPeriod)
                val lowBand = calculateLowBand(userAnswers, isSmallProducer)(frontendAppConfig)
                val highBand = calculateHighBand(userAnswers, isSmallProducer)(frontendAppConfig)
                val totalForQuarter = calculateTotal(userAnswers, isSmallProducer)(frontendAppConfig)
                val expectedLowLevy = if (isSmallProducer) BigDecimal("0.00") else lowerBandCostPerLitreMap(year) * lowLitres
                val expectedHighLevy = if (isSmallProducer) BigDecimal("0.00") else higherBandCostPerLitreMap(year) * highLitres
                lowBand mustBe expectedLowLevy.setScale(2, BigDecimal.RoundingMode.HALF_UP)
                highBand mustBe expectedHighLevy.setScale(2, BigDecimal.RoundingMode.HALF_UP)
                totalForQuarter mustBe (expectedLowLevy + expectedHighLevy).setScale(2, BigDecimal.RoundingMode.HALF_UP)
              }
            }
          }
        }

        s"calculate low levy, high levy, and total correctly with non-zero litres totals ${ if(isSmallProducer) "for small producer " else "" }with litres brought into the uk using $year rates for Jan - Mar ${year + 1}" in {
          forAll(posLitresInts) { lowLitres =>
            forAll(posLitresInts) { highLitres =>
              forAll(aprToDecInt) { month =>
                val returnPeriod = ReturnPeriod(LocalDate.of(year, month, 1))
                val userAnswers = userAnswersData(broughtIntoUKLitres = Option((lowLitres, highLitres)), returnPeriod = returnPeriod)
                val lowBand = calculateLowBand(userAnswers, isSmallProducer)(frontendAppConfig)
                val highBand = calculateHighBand(userAnswers, isSmallProducer)(frontendAppConfig)
                val totalForQuarter = calculateTotal(userAnswers, isSmallProducer)(frontendAppConfig)
                val expectedLowLevy = lowerBandCostPerLitreMap(year) * lowLitres
                val expectedHighLevy = higherBandCostPerLitreMap(year) * highLitres
                lowBand mustBe expectedLowLevy.setScale(2, BigDecimal.RoundingMode.HALF_UP)
                highBand mustBe expectedHighLevy.setScale(2, BigDecimal.RoundingMode.HALF_UP)
                totalForQuarter mustBe (expectedLowLevy + expectedHighLevy).setScale(2, BigDecimal.RoundingMode.HALF_UP)
              }
            }
          }
        }

        s"calculate low levy, high levy, and total correctly with non-zero litres totals ${ if(isSmallProducer) "for small producer " else "" }with litres brought into the uk from small producers using $year rates for Jan - Mar ${year + 1}" in {
          forAll(posLitresInts) { lowLitres =>
            forAll(posLitresInts) { highLitres =>
              forAll(aprToDecInt) { month =>
                val returnPeriod = ReturnPeriod(LocalDate.of(year, month, 1))
                val userAnswers = userAnswersData(broughtIntoUkFromSmallProducersLitres = Option((lowLitres, highLitres)), returnPeriod = returnPeriod)
                val lowBand = calculateLowBand(userAnswers, isSmallProducer)(frontendAppConfig)
                val highBand = calculateHighBand(userAnswers, isSmallProducer)(frontendAppConfig)
                val totalForQuarter = calculateTotal(userAnswers, isSmallProducer)(frontendAppConfig)
                val expectedLowLevy = BigDecimal("0.00")
                val expectedHighLevy = BigDecimal("0.00")
                lowBand mustBe expectedLowLevy.setScale(2, BigDecimal.RoundingMode.HALF_UP)
                highBand mustBe expectedHighLevy.setScale(2, BigDecimal.RoundingMode.HALF_UP)
                totalForQuarter mustBe (expectedLowLevy + expectedHighLevy).setScale(2, BigDecimal.RoundingMode.HALF_UP)
              }
            }
          }
        }

        s"calculate low levy, high levy, and total correctly with non-zero litres totals ${ if(isSmallProducer) "for small producer " else "" }with credits for litres exported using $year rates for Jan - Mar ${year + 1}" in {
          forAll(posLitresInts) { lowLitres =>
            forAll(posLitresInts) { highLitres =>
              forAll(aprToDecInt) { month =>
                val returnPeriod = ReturnPeriod(LocalDate.of(year, month, 1))
                val userAnswers = userAnswersData(claimCreditsForExportsLitres = Option((lowLitres, highLitres)), returnPeriod = returnPeriod)
                val lowBand = calculateLowBand(userAnswers, isSmallProducer)(frontendAppConfig)
                val highBand = calculateHighBand(userAnswers, isSmallProducer)(frontendAppConfig)
                val totalForQuarter = calculateTotal(userAnswers, isSmallProducer)(frontendAppConfig)
                val expectedLowLevy = -1 * lowerBandCostPerLitreMap(year) * lowLitres
                val expectedHighLevy = -1 * higherBandCostPerLitreMap(year) * highLitres
                lowBand mustBe expectedLowLevy.setScale(2, BigDecimal.RoundingMode.HALF_UP)
                highBand mustBe expectedHighLevy.setScale(2, BigDecimal.RoundingMode.HALF_UP)
                totalForQuarter mustBe (expectedLowLevy + expectedHighLevy).setScale(2, BigDecimal.RoundingMode.HALF_UP)
              }
            }
          }
        }

        s"calculate low levy, high levy, and total correctly with non-zero litres totals ${ if(isSmallProducer) "for small producer " else "" }with credits for litres lost or damaged using $year rates for Jan - Mar ${year + 1}" in {
          forAll(posLitresInts) { lowLitres =>
            forAll(posLitresInts) { highLitres =>
              forAll(aprToDecInt) { month =>
                val returnPeriod = ReturnPeriod(LocalDate.of(year, month, 1))
                val userAnswers = userAnswersData(claimCreditsForLostDamagedLitres = Option((lowLitres, highLitres)), returnPeriod = returnPeriod)
                val lowBand = calculateLowBand(userAnswers, isSmallProducer)(frontendAppConfig)
                val highBand = calculateHighBand(userAnswers, isSmallProducer)(frontendAppConfig)
                val totalForQuarter = calculateTotal(userAnswers, isSmallProducer)(frontendAppConfig)
                val expectedLowLevy = -1 * lowerBandCostPerLitreMap(year) * lowLitres
                val expectedHighLevy = -1 * higherBandCostPerLitreMap(year) * highLitres
                lowBand mustBe expectedLowLevy.setScale(2, BigDecimal.RoundingMode.HALF_UP)
                highBand mustBe expectedHighLevy.setScale(2, BigDecimal.RoundingMode.HALF_UP)
                totalForQuarter mustBe (expectedLowLevy + expectedHighLevy).setScale(2, BigDecimal.RoundingMode.HALF_UP)
              }
            }
          }
        }

        s"calculate low levy, high levy, and total correctly with non-zero litres totals ${ if(isSmallProducer) "for small producer " else "" }when return amount is 0 using $year rates for Jan - Mar ${year + 1}" in {
          forAll(janToMarInt) { month =>
            val ownBrandsLitres: Option[(Long, Long)] = if (isSmallProducer) Option(getRandomLitreage) else None
            val contractPackerLitres: Option[(Long, Long)] = None
            val broughtIntoUKLitres: Option[(Long, Long)] = None
            val broughtIntoUkFromSmallProducersLitres: Option[(Long, Long)] = Option(getRandomLitreage)
            val claimCreditsForExportsLitres: Option[(Long, Long)] = None
            val claimCreditsForLostDamagedLitres: Option[(Long, Long)] = None
            val smallProducerLitres: List[(Long, Long)] = List(getRandomLitreage, getRandomLitreage)
            val returnPeriod = ReturnPeriod(LocalDate.of(year + 1, month, 1))
            val userAnswers = userAnswersData(ownBrandsLitres, contractPackerLitres, broughtIntoUKLitres, broughtIntoUkFromSmallProducersLitres, claimCreditsForExportsLitres, claimCreditsForLostDamagedLitres, smallProducerLitres, returnPeriod)
            val lowBand = calculateLowBand(userAnswers, isSmallProducer)(frontendAppConfig)
            val highBand = calculateHighBand(userAnswers, isSmallProducer)(frontendAppConfig)
            val totalForQuarter = calculateTotal(userAnswers, isSmallProducer)(frontendAppConfig)
            val expectedLowLevy = BigDecimal("0.00")
            val expectedHighLevy = BigDecimal("0.00")
            lowBand mustBe expectedLowLevy
            highBand mustBe expectedHighLevy
            totalForQuarter mustBe expectedLowLevy + expectedHighLevy
          }
        }

        s"calculate low levy, high levy, and total correctly with non-zero litres totals ${ if(isSmallProducer) "for small producer " else "" }when return amount to pay using $year rates for Jan - Mar ${year + 1}" in {

        }

        s"calculate low levy, high levy, and total correctly with non-zero litres totals ${ if(isSmallProducer) "for small producer " else "" }when return amount is negative using $year rates for Jan - Mar ${year + 1}" in {

        }
      })
    })

  }

}
