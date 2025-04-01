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

package models

import base.SpecBase
import config.FrontendAppConfig
import models.LevyCalculator._
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

import java.time.LocalDate

class LevyCalculatorSpec extends SpecBase with ScalaCheckPropertyChecks {

  override lazy val frontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

  "getTaxYear" - {
    val janToMarInt = Gen.choose(1, 3)
    val aprToDecInt = Gen.choose(4, 12)

    (2018 to 2024).foreach(year => {
      s"return Pre2025 when in April - December $year" in {
        forAll(aprToDecInt) { month =>
          val returnPeriod = ReturnPeriod(LocalDate.of(year, month, 1))
          getTaxYear(returnPeriod) mustBe Pre2025
        }
      }

      s"return Pre2025 when in January - March ${year + 1}" in {
        forAll(janToMarInt) { month =>
          val returnPeriod = ReturnPeriod(LocalDate.of(year + 1, month, 1))
          getTaxYear(returnPeriod) mustBe Pre2025
        }
      }
    })

    "return Year2025 when in April - December 2025" in {
      forAll(aprToDecInt) { month =>
        val returnPeriod = ReturnPeriod(LocalDate.of(2025, month, 1))
        getTaxYear(returnPeriod) mustBe Year2025
      }
    }

    "return Year2025 when in January - March 2026" in {
      forAll(janToMarInt) { month =>
        val returnPeriod = ReturnPeriod(LocalDate.of(2026, month, 1))
        getTaxYear(returnPeriod) mustBe Year2025
      }
    }

    "return Year2026 when in April - December 2026" in {
      forAll(aprToDecInt) { month =>
        val returnPeriod = ReturnPeriod(LocalDate.of(2026, month, 1))
        getTaxYear(returnPeriod) mustBe Year2026
      }
    }

    "return Year2026 when in January - March 2027" in {
      forAll(janToMarInt) { month =>
        val returnPeriod = ReturnPeriod(LocalDate.of(2027, month, 1))
        getTaxYear(returnPeriod) mustBe Year2026
      }
    }
  }

  "getBandRates" - {
    (2018 to 2024).foreach(taxYear => {
      val bandRates: BandRates = getBandRates(TaxYear.fromYear(taxYear))(frontendAppConfig)

      s"return 0.18 for lower band when tax year is $taxYear" in {
        bandRates.lowerBandCostPerLites mustBe BigDecimal("0.18")
      }

      s"return 0.24 for higher band when tax year is $taxYear" in {
        bandRates.higherBandCostPerLitre mustBe BigDecimal("0.24")
      }
    })

    "return 0.194 for lower band when tax year is 2025" in {
      val bandRates: BandRates = getBandRates(TaxYear.fromYear(2025))(frontendAppConfig)
      bandRates.lowerBandCostPerLites mustBe BigDecimal("0.194")
    }

    "return 0.259 for higher band when tax year is 2025" in {
      val bandRates: BandRates = getBandRates(TaxYear.fromYear(2025))(frontendAppConfig)
      bandRates.higherBandCostPerLitre mustBe BigDecimal("0.259")
    }
  }

  "getLevyCalculation" - {

    val smallPosInts = Gen.choose(0, 1000)
    val largePosInts = Gen.choose(1000, 10000000)
    val janToMarInt = Gen.choose(1, 3)
    val aprToDecInt = Gen.choose(4, 12)

    (2018 to 2024).foreach(year => {

      val lowerBandCostPerLitre = BigDecimal("0.18")
      val higherBandCostPerLitre = BigDecimal("0.24")

      s"calculate low levy, high levy, and total correctly with zero litres totals using original rates for Apr - Dec $year" in {
        forAll(aprToDecInt) { month =>
          val returnPeriod = ReturnPeriod(LocalDate.of(year, month, 1))
          val levyCalculation = getLevyCalculation(0, 0, returnPeriod)(frontendAppConfig)
          levyCalculation.lowLevy mustBe BigDecimal("0.00")
          levyCalculation.highLevy mustBe BigDecimal("0.00")
          levyCalculation.total mustBe BigDecimal("0.00")
        }
      }

      s"calculate low levy, high levy, and total correctly with small litres totals using original rates for Apr - Dec $year" in {
        forAll(smallPosInts) { lowLitres =>
          forAll(smallPosInts) { highLitres =>
            forAll(aprToDecInt) { month =>
              val returnPeriod = ReturnPeriod(LocalDate.of(year, month, 1))
              val levyCalculation = getLevyCalculation(lowLitres, highLitres, returnPeriod)(frontendAppConfig)
              val expectedLowLevy = lowerBandCostPerLitre * lowLitres
              val expectedHighLevy = higherBandCostPerLitre * highLitres
              levyCalculation.lowLevy mustBe expectedLowLevy
              levyCalculation.highLevy mustBe expectedHighLevy
              levyCalculation.total mustBe expectedLowLevy + expectedHighLevy
            }
          }
        }
      }

      s"calculate low levy, high levy, and total correctly with large litres totals using original rates for Apr - Dec $year" in {
        forAll(largePosInts) { lowLitres =>
          forAll(largePosInts) { highLitres =>
            forAll(aprToDecInt) { month =>
              val returnPeriod = ReturnPeriod(LocalDate.of(year, month, 1))
              val levyCalculation = getLevyCalculation(lowLitres, highLitres, returnPeriod)(frontendAppConfig)
              val expectedLowLevy = lowerBandCostPerLitre * lowLitres
              val expectedHighLevy = higherBandCostPerLitre * highLitres
              levyCalculation.lowLevy mustBe expectedLowLevy
              levyCalculation.highLevy mustBe expectedHighLevy
              levyCalculation.total mustBe expectedLowLevy + expectedHighLevy
            }
          }
        }
      }

      s"calculate low levy, high levy, and total correctly with zero litres totals using original rates for Jan - Mar ${year + 1}" in {
        forAll(janToMarInt) { month =>
          val returnPeriod = ReturnPeriod(LocalDate.of(year + 1, month, 1))
          val levyCalculation = getLevyCalculation(0, 0, returnPeriod)(frontendAppConfig)
          levyCalculation.lowLevy mustBe BigDecimal("0.00")
          levyCalculation.highLevy mustBe BigDecimal("0.00")
          levyCalculation.total mustBe BigDecimal("0.00")
        }
      }

      s"calculate low levy, high levy, and total correctly with small litres totals using original rates for Jan - Mar ${year + 1}" in {
        forAll(smallPosInts) { lowLitres =>
          forAll(smallPosInts) { highLitres =>
            forAll(janToMarInt) { month =>
              val returnPeriod = ReturnPeriod(LocalDate.of(year + 1, month, 1))
              val levyCalculation = getLevyCalculation(lowLitres, highLitres, returnPeriod)(frontendAppConfig)
              val expectedLowLevy = lowerBandCostPerLitre * lowLitres
              val expectedHighLevy = higherBandCostPerLitre * highLitres
              levyCalculation.lowLevy mustBe expectedLowLevy
              levyCalculation.highLevy mustBe expectedHighLevy
              levyCalculation.total mustBe expectedLowLevy + expectedHighLevy
            }
          }
        }
      }

      s"calculate low levy, high levy, and total correctly with large litres totals using original rates for Jan - Mar ${year + 1}" in {
        forAll(largePosInts) { lowLitres =>
          forAll(largePosInts) { highLitres =>
            forAll(janToMarInt) { month =>
              val returnPeriod = ReturnPeriod(LocalDate.of(year + 1, month, 1))
              val levyCalculation = getLevyCalculation(lowLitres, highLitres, returnPeriod)(frontendAppConfig)
              val expectedLowLevy = lowerBandCostPerLitre * lowLitres
              val expectedHighLevy = higherBandCostPerLitre * highLitres
              levyCalculation.lowLevy mustBe expectedLowLevy
              levyCalculation.highLevy mustBe expectedHighLevy
              levyCalculation.total mustBe expectedLowLevy + expectedHighLevy
            }
          }
        }
      }
    })

    (2025 to 2025).foreach(year => {

      val lowerBandCostPerLitreMap: Map[Int, BigDecimal] = Map(2025 -> BigDecimal("0.194"))
      val higherBandCostPerLitreMap: Map[Int, BigDecimal] = Map(2025 -> BigDecimal("0.259"))

      s"calculate low levy, high levy, and total correctly with zero litres totals using $year rates for Apr - Dec $year" in {
        forAll(aprToDecInt) { month =>
          val returnPeriod = ReturnPeriod(LocalDate.of(year, month, 1))
          val levyCalculation = getLevyCalculation(0, 0, returnPeriod)(frontendAppConfig)
          levyCalculation.lowLevy mustBe BigDecimal("0.00")
          levyCalculation.highLevy mustBe BigDecimal("0.00")
          levyCalculation.total mustBe BigDecimal("0.00")
        }
      }

      s"calculate low levy, high levy, and total correctly with small litres totals using $year rates for Apr - Dec $year" in {
        forAll(aprToDecInt) { month =>
          forAll(smallPosInts) { lowLitres =>
            forAll(smallPosInts) { highLitres =>
              val returnPeriod = ReturnPeriod(LocalDate.of(year, month, 1))
              val levyCalculation = getLevyCalculation(lowLitres, highLitres, returnPeriod)(frontendAppConfig)
              val expectedLowLevy = lowerBandCostPerLitreMap(year) * lowLitres
              val expectedHighLevy = higherBandCostPerLitreMap(year) * highLitres
              levyCalculation.lowLevy mustBe expectedLowLevy.setScale(2, BigDecimal.RoundingMode.HALF_UP)
              levyCalculation.highLevy mustBe expectedHighLevy.setScale(2, BigDecimal.RoundingMode.HALF_UP)
              levyCalculation.total mustBe (expectedLowLevy + expectedHighLevy).setScale(2, BigDecimal.RoundingMode.HALF_UP)
            }
          }
        }
      }

      s"calculate low levy, high levy, and total correctly with large litres totals using $year rates for Apr - Dec $year" in {
        forAll(aprToDecInt) { month =>
          forAll(largePosInts) { lowLitres =>
            forAll(largePosInts) { highLitres =>
              val returnPeriod = ReturnPeriod(LocalDate.of(year, month, 1))
              val levyCalculation = getLevyCalculation(lowLitres, highLitres, returnPeriod)(frontendAppConfig)
              val expectedLowLevy = lowerBandCostPerLitreMap(year) * lowLitres
              val expectedHighLevy = higherBandCostPerLitreMap(year) * highLitres
              levyCalculation.lowLevy mustBe expectedLowLevy.setScale(2, BigDecimal.RoundingMode.HALF_UP)
              levyCalculation.highLevy mustBe expectedHighLevy.setScale(2, BigDecimal.RoundingMode.HALF_UP)
              levyCalculation.total mustBe (expectedLowLevy + expectedHighLevy).setScale(2, BigDecimal.RoundingMode.HALF_UP)
            }
          }
        }
      }

      s"calculate low levy, high levy, and total correctly with zero litres totals using $year rates for Jan - Mar ${year + 1}" in {
        forAll(janToMarInt) { month =>
          val returnPeriod = ReturnPeriod(LocalDate.of(year + 1, month, 1))
          val levyCalculation = getLevyCalculation(0, 0, returnPeriod)(frontendAppConfig)
          levyCalculation.lowLevy mustBe BigDecimal("0.00")
          levyCalculation.highLevy mustBe BigDecimal("0.00")
          levyCalculation.total mustBe BigDecimal("0.00")
        }
      }

      s"calculate low levy, high levy, and total correctly with small litres totals using $year rates for Jan - Mar ${year + 1}" in {
        forAll(janToMarInt) { month =>
          forAll(smallPosInts) { lowLitres =>
            forAll(smallPosInts) { highLitres =>
              val returnPeriod = ReturnPeriod(LocalDate.of(year + 1, month, 1))
              val levyCalculation = getLevyCalculation(lowLitres, highLitres, returnPeriod)(frontendAppConfig)
              val expectedLowLevy = lowerBandCostPerLitreMap(year) * lowLitres
              val expectedHighLevy = higherBandCostPerLitreMap(year) * highLitres
              levyCalculation.lowLevy mustBe expectedLowLevy.setScale(2, BigDecimal.RoundingMode.HALF_UP)
              levyCalculation.highLevy mustBe expectedHighLevy.setScale(2, BigDecimal.RoundingMode.HALF_UP)
              levyCalculation.total mustBe (expectedLowLevy + expectedHighLevy).setScale(2, BigDecimal.RoundingMode.HALF_UP)
            }
          }
        }
      }

      s"calculate low levy, high levy, and total correctly with large litres totals using $year rates for Jan - Mar ${year + 1}" in {
        forAll(janToMarInt) { month =>
          forAll(largePosInts) { lowLitres =>
            forAll(largePosInts) { highLitres =>
              val returnPeriod = ReturnPeriod(LocalDate.of(year + 1, month, 1))
              val levyCalculation = getLevyCalculation(lowLitres, highLitres, returnPeriod)(frontendAppConfig)
              val expectedLowLevy = lowerBandCostPerLitreMap(year) * lowLitres
              val expectedHighLevy = higherBandCostPerLitreMap(year) * highLitres
              levyCalculation.lowLevy mustBe expectedLowLevy.setScale(2, BigDecimal.RoundingMode.HALF_UP)
              levyCalculation.highLevy mustBe expectedHighLevy.setScale(2, BigDecimal.RoundingMode.HALF_UP)
              levyCalculation.total mustBe (expectedLowLevy + expectedHighLevy).setScale(2, BigDecimal.RoundingMode.HALF_UP)
            }
          }
        }
      }

    })

  }

}
