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

package models

import base.SpecBase
import config.FrontendAppConfig
import org.scalacheck.Gen
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

import java.time.LocalDate

class SdilReturnsModelSpec extends SpecBase with MockitoSugar with DataHelper with ScalaCheckPropertyChecks {
  override implicit val frontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

  private def getRandomLitres: Long = Math.floor(Math.random() * 1000000).toLong
  private def getRandomLitreage: (Long, Long) = (getRandomLitres, getRandomLitres)
  private def getRandomSdilRef(index: Int): String = s"${Math.floor(Math.random() * 1000).toLong}SdilRef$index"

  private def getSdilReturn(
                     ownBrand: (Long, Long) = (0L, 0L),
                     packLarge: (Long, Long) = (0L, 0L),
                     numberOfPackSmall: Int = 0,
                     importLarge: (Long, Long) = (0L, 0L),
                     importSmall: (Long, Long) = (0L, 0L),
                     export: (Long, Long) = (0L, 0L),
                     wastage: (Long, Long) = (0L, 0L)
                   ): SdilReturn = {
    val smallProducers: Seq[SmallProducer] = (0 to numberOfPackSmall)
      .map(index => SmallProducer(getRandomSdilRef(index), getRandomSdilRef(index), getRandomLitreage))
    SdilReturn(ownBrand, packLarge, packSmall = smallProducers.toList, importLarge, importSmall, export, wastage, submittedOn = None)
  }

//  TODO: Fix unit tests and add total packed and total imported checks

  "SdilReturn" - {
    val posLitresInts = Gen.choose(1000, 10000000)
    val janToMarInt = Gen.choose(1, 3)
    val aprToDecInt = Gen.choose(4, 12)

    (2018 to 2024).foreach(year => {

      val lowerBandCostPerLitre = BigDecimal("0.18")
      val higherBandCostPerLitre = BigDecimal("0.24")

      s"calculate total packed, total imported, and tax estimation correctly with non-zero litres totals with litres packed at own site using original rates for Apr - Dec $year" in {
        forAll(posLitresInts) { lowLitres =>
          forAll(posLitresInts) { highLitres =>
            forAll(aprToDecInt) { month =>
              implicit val returnPeriod = ReturnPeriod(LocalDate.of(year, month, 1))
              val sdilReturn = getSdilReturn(ownBrand = (lowLitres, highLitres))



              val expectedTotalPacked = 0
              val expectedTotalImported = 0
              val expectedTaxEstimation = 4 * (lowerBandCostPerLitre * lowLitres + higherBandCostPerLitre * highLitres)



              sdilReturn.totalPacked mustBe expectedTotalPacked
              sdilReturn.totalImported mustBe expectedTotalImported
              sdilReturn.taxEstimation mustBe expectedTaxEstimation
            }
          }
        }
      }

      s"calculate total packed, total imported, and tax estimation correctly with non-zero litres totals with litres contract packed using original rates for Apr - Dec $year" in {
        forAll(posLitresInts) { lowLitres =>
          forAll(posLitresInts) { highLitres =>
            forAll(aprToDecInt) { month =>
              implicit val returnPeriod = ReturnPeriod(LocalDate.of(year, month, 1))
              val sdilReturn = getSdilReturn(packLarge = (lowLitres, highLitres))



              val expectedTotalPacked = 0
              val expectedTotalImported = 0
              val expectedTaxEstimation = 4 * (lowerBandCostPerLitre * lowLitres + higherBandCostPerLitre * highLitres)



              sdilReturn.totalPacked mustBe expectedTotalPacked
              sdilReturn.totalImported mustBe expectedTotalImported
              sdilReturn.taxEstimation mustBe expectedTaxEstimation
            }
          }
        }
      }

      s"calculate total packed, total imported, and tax estimation correctly with non-zero litres totals with exemptions for small producers using original rates for Apr - Dec $year" in {
        forAll(posLitresInts) { lowLitres =>
          forAll(posLitresInts) { highLitres =>
            forAll(aprToDecInt) { month =>
              implicit val returnPeriod = ReturnPeriod(LocalDate.of(year, month, 1))
              val sdilReturn = getSdilReturn(numberOfPackSmall = 2)
              val expectedTotalPacked = 0
              val expectedTotalImported = 0
              val expectedTaxEstimation = BigDecimal("0.00")



              sdilReturn.totalPacked mustBe expectedTotalPacked
              sdilReturn.totalImported mustBe expectedTotalImported
              sdilReturn.taxEstimation mustBe expectedTaxEstimation
            }
          }
        }
      }

      s"calculate total packed, total imported, and tax estimation correctly with non-zero litres totals with litres brought into the uk using original rates for Apr - Dec $year" in {
        forAll(posLitresInts) { lowLitres =>
          forAll(posLitresInts) { highLitres =>
            forAll(aprToDecInt) { month =>
              implicit val returnPeriod = ReturnPeriod(LocalDate.of(year, month, 1))
              val sdilReturn = getSdilReturn(importLarge = (lowLitres, highLitres))



              val expectedTotalPacked = 0
              val expectedTotalImported = 0
              val expectedTaxEstimation = 4 * (lowerBandCostPerLitre * lowLitres + higherBandCostPerLitre * highLitres)



              sdilReturn.totalPacked mustBe expectedTotalPacked
              sdilReturn.totalImported mustBe expectedTotalImported
              sdilReturn.taxEstimation mustBe expectedTaxEstimation
            }
          }
        }
      }

      s"calculate total packed, total imported, and tax estimation correctly with non-zero litres totals with litres brought into the uk from small producers using original rates for Apr - Dec $year" in {
        forAll(posLitresInts) { lowLitres =>
          forAll(posLitresInts) { highLitres =>
            forAll(aprToDecInt) { month =>
              implicit val returnPeriod = ReturnPeriod(LocalDate.of(year, month, 1))
              val sdilReturn = getSdilReturn(importSmall = (lowLitres, highLitres))
              val expectedTotalPacked = 0
              val expectedTotalImported = 0
              val expectedTaxEstimation = BigDecimal("0.00")



              sdilReturn.totalPacked mustBe expectedTotalPacked
              sdilReturn.totalImported mustBe expectedTotalImported
              sdilReturn.taxEstimation mustBe expectedTaxEstimation
            }
          }
        }
      }

      s"calculate total packed, total imported, and tax estimation correctly with non-zero litres totals with credits for litres exported using original rates for Apr - Dec $year" in {
        forAll(posLitresInts) { lowLitres =>
          forAll(posLitresInts) { highLitres =>
            forAll(aprToDecInt) { month =>
              implicit val returnPeriod = ReturnPeriod(LocalDate.of(year, month, 1))
              val sdilReturn = getSdilReturn(`export` = (lowLitres, highLitres))

              val expectedTotalPacked = 0
              val expectedTotalImported = 0
              val expectedTaxEstimation = BigDecimal("0.00")



              sdilReturn.totalPacked mustBe expectedTotalPacked
              sdilReturn.totalImported mustBe expectedTotalImported
              sdilReturn.taxEstimation mustBe expectedTaxEstimation
            }
          }
        }
      }

      s"calculate total packed, total imported, and tax estimation correctly with non-zero litres totals with credits for litres lost or damaged using original rates for Apr - Dec $year" in {
        forAll(posLitresInts) { lowLitres =>
          forAll(posLitresInts) { highLitres =>
            forAll(aprToDecInt) { month =>
              implicit val returnPeriod = ReturnPeriod(LocalDate.of(year, month, 1))
              val sdilReturn = getSdilReturn(wastage = (lowLitres, highLitres))

              val expectedTotalPacked = 0
              val expectedTotalImported = 0
              val expectedTaxEstimation = BigDecimal("0.00")



              sdilReturn.totalPacked mustBe expectedTotalPacked
              sdilReturn.totalImported mustBe expectedTotalImported
              sdilReturn.taxEstimation mustBe expectedTaxEstimation
            }
          }
        }
      }

      s"calculate total packed, total imported, and tax estimation correctly with non-zero litres totals when return amount is 0 using original rates for Apr - Dec $year" in {
        forAll(aprToDecInt) { month =>
          implicit val returnPeriod = ReturnPeriod(LocalDate.of(year, month, 1))
          val sdilReturn = getSdilReturn()
          val expectedTaxEstimation = BigDecimal("0.00")
          sdilReturn.taxEstimation mustBe expectedTaxEstimation
        }
      }

      s"calculate total packed, total imported, and tax estimation correctly with non-zero litres totals when return amount to pay using original rates for Apr - Dec $year" in {
        forAll(aprToDecInt) { month =>
          implicit val returnPeriod = ReturnPeriod(LocalDate.of(year, month, 1))
          val ownBrandLitres = getRandomLitreage
          val packLargeLitres = getRandomLitreage
          val importLargeLitres = getRandomLitreage
          val sdilReturn = getSdilReturn(ownBrand = ownBrandLitres, packLarge = packLargeLitres, importLarge = importLargeLitres)
          val expectedTaxEstimation = 4 * (lowerBandCostPerLitre * expectedLevied(Long, Long).lower + higherBandCostPerLitre * expectedLevied(Long, Long).higher)
          sdilReturn.taxEstimation mustBe expectedTaxEstimation
        }
      }

      s"calculate total packed, total imported, and tax estimation correctly with non-zero litres totals when return amount is negative using original rates for Apr - Dec $year" in {
        forAll(aprToDecInt) { month =>
          implicit val returnPeriod = ReturnPeriod(LocalDate.of(year, month, 1))
          val exportLitres = getRandomLitreage
          val wastageLitres = getRandomLitreage
          val sdilReturn = getSdilReturn(`export` = exportLitres, wastage = wastageLitres)
          val expectedTaxEstimation = BigDecimal("0.00")
          sdilReturn.taxEstimation mustBe expectedTaxEstimation
        }
      }

      s"calculate total packed, total imported, and tax estimation correctly with non-zero litres totals with litres packed at own site using original rates for Jan - Mar ${year + 1}" in {
        forAll(posLitresInts) { lowLitres =>
          forAll(posLitresInts) { highLitres =>
            forAll(janToMarInt) { month =>
              implicit val returnPeriod = ReturnPeriod(LocalDate.of(year, month, 1))
              val sdilReturn = getSdilReturn(ownBrand = (lowLitres, highLitres))



              val expectedTotalPacked = 0
              val expectedTotalImported = 0
              val expectedTaxEstimation = 4 * (lowerBandCostPerLitre * lowLitres + higherBandCostPerLitre * highLitres)



              sdilReturn.totalPacked mustBe expectedTotalPacked
              sdilReturn.totalImported mustBe expectedTotalImported
              sdilReturn.taxEstimation mustBe expectedTaxEstimation
            }
          }
        }
      }

      s"calculate total packed, total imported, and tax estimation correctly with non-zero litres totals with litres contract packed using original rates for Jan - Mar ${year + 1}" in {
        forAll(posLitresInts) { lowLitres =>
          forAll(posLitresInts) { highLitres =>
            forAll(janToMarInt) { month =>
              implicit val returnPeriod = ReturnPeriod(LocalDate.of(year, month, 1))
              val sdilReturn = getSdilReturn(packLarge = (lowLitres, highLitres))



              val expectedTotalPacked = 0
              val expectedTotalImported = 0
              val expectedTaxEstimation = 4 * (lowerBandCostPerLitre * lowLitres + higherBandCostPerLitre * highLitres)



              sdilReturn.totalPacked mustBe expectedTotalPacked
              sdilReturn.totalImported mustBe expectedTotalImported
              sdilReturn.taxEstimation mustBe expectedTaxEstimation
            }
          }
        }
      }

      s"calculate total packed, total imported, and tax estimation correctly with non-zero litres totals with exemptions for small producers using original rates for Jan - Mar ${year + 1}" in {
        forAll(posLitresInts) { lowLitres =>
          forAll(posLitresInts) { highLitres =>
            forAll(janToMarInt) { month =>
              implicit val returnPeriod = ReturnPeriod(LocalDate.of(year, month, 1))
              val sdilReturn = getSdilReturn(numberOfPackSmall = 2)
              val expectedTotalPacked = 0
              val expectedTotalImported = 0
              val expectedTaxEstimation = BigDecimal("0.00")



              sdilReturn.totalPacked mustBe expectedTotalPacked
              sdilReturn.totalImported mustBe expectedTotalImported
              sdilReturn.taxEstimation mustBe expectedTaxEstimation
            }
          }
        }
      }

      s"calculate total packed, total imported, and tax estimation correctly with non-zero litres totals with litres brought into the uk using original rates for Jan - Mar ${year + 1}" in {
        forAll(posLitresInts) { lowLitres =>
          forAll(posLitresInts) { highLitres =>
            forAll(janToMarInt) { month =>
              implicit val returnPeriod = ReturnPeriod(LocalDate.of(year, month, 1))
              val sdilReturn = getSdilReturn(importLarge = (lowLitres, highLitres))



              val expectedTotalPacked = 0
              val expectedTotalImported = 0
              val expectedTaxEstimation = 4 * (lowerBandCostPerLitre * lowLitres + higherBandCostPerLitre * highLitres)



              sdilReturn.totalPacked mustBe expectedTotalPacked
              sdilReturn.totalImported mustBe expectedTotalImported
              sdilReturn.taxEstimation mustBe expectedTaxEstimation
            }
          }
        }
      }

      s"calculate total packed, total imported, and tax estimation correctly with non-zero litres totals with litres brought into the uk from small producers using original rates for Jan - Mar ${year + 1}" in {
        forAll(posLitresInts) { lowLitres =>
          forAll(posLitresInts) { highLitres =>
            forAll(janToMarInt) { month =>
              implicit val returnPeriod = ReturnPeriod(LocalDate.of(year, month, 1))
              val sdilReturn = getSdilReturn(importSmall = (lowLitres, highLitres))
              val expectedTotalPacked = 0
              val expectedTotalImported = 0
              val expectedTaxEstimation = BigDecimal("0.00")



              sdilReturn.totalPacked mustBe expectedTotalPacked
              sdilReturn.totalImported mustBe expectedTotalImported
              sdilReturn.taxEstimation mustBe expectedTaxEstimation
            }
          }
        }
      }

      s"calculate total packed, total imported, and tax estimation correctly with non-zero litres totals with credits for litres exported using original rates for Jan - Mar ${year + 1}" in {
        forAll(posLitresInts) { lowLitres =>
          forAll(posLitresInts) { highLitres =>
            forAll(janToMarInt) { month =>
              implicit val returnPeriod = ReturnPeriod(LocalDate.of(year, month, 1))
              val sdilReturn = getSdilReturn(`export` = (lowLitres, highLitres))

              val expectedTotalPacked = 0
              val expectedTotalImported = 0
              val expectedTaxEstimation = BigDecimal("0.00")



              sdilReturn.totalPacked mustBe expectedTotalPacked
              sdilReturn.totalImported mustBe expectedTotalImported
              sdilReturn.taxEstimation mustBe expectedTaxEstimation
            }
          }
        }
      }

      s"calculate total packed, total imported, and tax estimation correctly with non-zero litres totals with credits for litres lost or damaged using original rates for Jan - Mar ${year + 1}" in {
        forAll(posLitresInts) { lowLitres =>
          forAll(posLitresInts) { highLitres =>
            forAll(janToMarInt) { month =>
              implicit val returnPeriod = ReturnPeriod(LocalDate.of(year, month, 1))
              val sdilReturn = getSdilReturn(wastage = (lowLitres, highLitres))

              val expectedTotalPacked = 0
              val expectedTotalImported = 0
              val expectedTaxEstimation = BigDecimal("0.00")



              sdilReturn.totalPacked mustBe expectedTotalPacked
              sdilReturn.totalImported mustBe expectedTotalImported
              sdilReturn.taxEstimation mustBe expectedTaxEstimation
            }
          }
        }
      }

      s"calculate total packed, total imported, and tax estimation correctly with non-zero litres totals when return amount is 0 using original rates for Jan - Mar ${year + 1}" in {
        forAll(janToMarInt) { month =>
          implicit val returnPeriod = ReturnPeriod(LocalDate.of(year + 1, month, 1))
          val sdilReturn = getSdilReturn()
          val expectedTaxEstimation = BigDecimal("0.00")
          sdilReturn.taxEstimation mustBe expectedTaxEstimation
        }
      }

      s"calculate total packed, total imported, and tax estimation correctly with non-zero litres totals when return amount to pay using original rates for Jan - Mar ${year + 1}" in {
        forAll(janToMarInt) { month =>
          implicit val returnPeriod = ReturnPeriod(LocalDate.of(year + 1, month, 1))
          val ownBrandLitres = getRandomLitreage
          val packLargeLitres = getRandomLitreage
          val importLargeLitres = getRandomLitreage
          val sdilReturn = getSdilReturn(ownBrand = ownBrandLitres, packLarge = packLargeLitres, importLarge = importLargeLitres)
          val expectedTaxEstimation = 4 * (lowerBandCostPerLitre * expectedLevied(Long, Long).lower + higherBandCostPerLitre * expectedLevied(Long, Long).higher)
          sdilReturn.taxEstimation mustBe expectedTaxEstimation
        }
      }

      s"calculate total packed, total imported, and tax estimation correctly with non-zero litres totals when return amount is negative using original rates for Jan - Mar ${year + 1}" in {
        forAll(janToMarInt) { month =>
          implicit val returnPeriod = ReturnPeriod(LocalDate.of(year + 1, month, 1))
          val exportLitres = getRandomLitreage
          val wastageLitres = getRandomLitreage
          val sdilReturn = getSdilReturn(`export` = exportLitres, wastage = wastageLitres)
          val expectedTaxEstimation = BigDecimal("0.00")
          sdilReturn.taxEstimation mustBe expectedTaxEstimation
        }
      }
    })

    (2025 to 2025).foreach(year => {

      val lowerBandCostPerLitreMap: Map[Int, BigDecimal] = Map(2025 -> BigDecimal("0.194"))
      val higherBandCostPerLitreMap: Map[Int, BigDecimal] = Map(2025 -> BigDecimal("0.259"))

      s"calculate total packed, total imported, and tax estimation correctly with non-zero litres totals with litres packed at own site using $year rates for Apr - Dec $year" in {
        forAll(posLitresInts) { lowLitres =>
          forAll(posLitresInts) { highLitres =>
            forAll(aprToDecInt) { month =>
              implicit val returnPeriod = ReturnPeriod(LocalDate.of(year, month, 1))
              val sdilReturn = getSdilReturn(ownBrand = (lowLitres, highLitres))



              val expectedTotalPacked = 0
              val expectedTotalImported = 0
              val expectedTaxEstimation = 4 * (lowerBandCostPerLitreMap(year) * lowLitres + higherBandCostPerLitreMap(year) * highLitres)


              sdilReturn.totalPacked mustBe expectedTotalPacked
              sdilReturn.totalImported mustBe expectedTotalImported
              sdilReturn.taxEstimation mustBe expectedTaxEstimation.setScale(2, BigDecimal.RoundingMode.HALF_UP)
            }
          }
        }
      }

      s"calculate total packed, total imported, and tax estimation correctly with non-zero litres totals with litres contract packed using $year rates for Apr - Dec $year" in {
        forAll(posLitresInts) { lowLitres =>
          forAll(posLitresInts) { highLitres =>
            forAll(aprToDecInt) { month =>
              implicit val returnPeriod = ReturnPeriod(LocalDate.of(year, month, 1))
              val sdilReturn = getSdilReturn(packLarge = (lowLitres, highLitres))



              val expectedTotalPacked = 0
              val expectedTotalImported = 0
              val expectedTaxEstimation = 4 * (lowerBandCostPerLitreMap(year) * lowLitres + higherBandCostPerLitreMap(year) * highLitres)


              sdilReturn.totalPacked mustBe expectedTotalPacked
              sdilReturn.totalImported mustBe expectedTotalImported
              sdilReturn.taxEstimation mustBe expectedTaxEstimation.setScale(2, BigDecimal.RoundingMode.HALF_UP)
            }
          }
        }
      }

      s"calculate total packed, total imported, and tax estimation correctly with non-zero litres totals with exemptions for small producers using $year rates for Apr - Dec $year" in {
        forAll(posLitresInts) { lowLitres =>
          forAll(posLitresInts) { highLitres =>
            forAll(aprToDecInt) { month =>
              implicit val returnPeriod = ReturnPeriod(LocalDate.of(year, month, 1))
              val sdilReturn = getSdilReturn(numberOfPackSmall = 2)
              val expectedTotalPacked = 0
              val expectedTotalImported = 0
              val expectedTaxEstimation = BigDecimal("0.00")


              sdilReturn.totalPacked mustBe expectedTotalPacked
              sdilReturn.totalImported mustBe expectedTotalImported
              sdilReturn.taxEstimation mustBe expectedTaxEstimation.setScale(2, BigDecimal.RoundingMode.HALF_UP)
            }
          }
        }
      }

      s"calculate total packed, total imported, and tax estimation correctly with non-zero litres totals with litres brought into the uk using $year rates for Apr - Dec $year" in {
        forAll(posLitresInts) { lowLitres =>
          forAll(posLitresInts) { highLitres =>
            forAll(aprToDecInt) { month =>
              implicit val returnPeriod = ReturnPeriod(LocalDate.of(year, month, 1))
              val sdilReturn = getSdilReturn(importLarge = (lowLitres, highLitres))



              val expectedTotalPacked = 0
              val expectedTotalImported = 0
              val expectedTaxEstimation = 4 * (lowerBandCostPerLitreMap(year) * lowLitres + higherBandCostPerLitreMap(year) * highLitres)


              sdilReturn.totalPacked mustBe expectedTotalPacked
              sdilReturn.totalImported mustBe expectedTotalImported
              sdilReturn.taxEstimation mustBe expectedTaxEstimation.setScale(2, BigDecimal.RoundingMode.HALF_UP)
            }
          }
        }
      }

      s"calculate total packed, total imported, and tax estimation correctly with non-zero litres totals with litres brought into the uk from small producers using $year rates for Apr - Dec $year" in {
        forAll(posLitresInts) { lowLitres =>
          forAll(posLitresInts) { highLitres =>
            forAll(aprToDecInt) { month =>
              implicit val returnPeriod = ReturnPeriod(LocalDate.of(year, month, 1))
              val sdilReturn = getSdilReturn(importSmall = (lowLitres, highLitres))
              val expectedTotalPacked = 0
              val expectedTotalImported = 0
              val expectedTaxEstimation = BigDecimal("0.00")


              sdilReturn.totalPacked mustBe expectedTotalPacked
              sdilReturn.totalImported mustBe expectedTotalImported
              sdilReturn.taxEstimation mustBe expectedTaxEstimation.setScale(2, BigDecimal.RoundingMode.HALF_UP)
            }
          }
        }
      }

      s"calculate total packed, total imported, and tax estimation correctly with non-zero litres totals with credits for litres exported using $year rates for Apr - Dec $year" in {
        forAll(posLitresInts) { lowLitres =>
          forAll(posLitresInts) { highLitres =>
            forAll(aprToDecInt) { month =>
              implicit val returnPeriod = ReturnPeriod(LocalDate.of(year, month, 1))
              val sdilReturn = getSdilReturn(`export` = (lowLitres, highLitres))
                  val expectedTotalPacked = 0
              val expectedTotalImported = 0
              val expectedTaxEstimation = BigDecimal("0.00")


              sdilReturn.totalPacked mustBe expectedTotalPacked
              sdilReturn.totalImported mustBe expectedTotalImported
              sdilReturn.taxEstimation mustBe expectedTaxEstimation.setScale(2, BigDecimal.RoundingMode.HALF_UP)
            }
          }
        }
      }

        s"calculate total packed, total imported, and tax estimation correctly with non-zero litres totals with credits for litres lost or damaged using $year rates for Apr - Dec $year" in {
          forAll(posLitresInts) { lowLitres =>
            forAll(posLitresInts) { highLitres =>
              forAll(aprToDecInt) { month =>
                implicit val returnPeriod = ReturnPeriod(LocalDate.of(year, month, 1))
                val sdilReturn = getSdilReturn(wastage = (lowLitres, highLitres))

                val expectedTotalPacked = 0
              val expectedTotalImported = 0
              val expectedTaxEstimation = BigDecimal("0.00")


                sdilReturn.totalPacked mustBe expectedTotalPacked
              sdilReturn.totalImported mustBe expectedTotalImported
              sdilReturn.taxEstimation mustBe expectedTaxEstimation.setScale(2, BigDecimal.RoundingMode.HALF_UP)
              }
            }
          }
        }

      s"calculate total packed, total imported, and tax estimation correctly with non-zero litres totals when return amount is 0 using $year rates for Apr - Dec $year" in {
        forAll(aprToDecInt) { month =>
          implicit val returnPeriod = ReturnPeriod(LocalDate.of(year, month, 1))
          val sdilReturn = getSdilReturn()
          val expectedTaxEstimation = BigDecimal("0.00")
          sdilReturn.taxEstimation mustBe expectedTaxEstimation
        }
      }

      s"calculate total packed, total imported, and tax estimation correctly with non-zero litres totals when return amount to pay using $year rates for Apr - Dec $year" in {
        forAll(aprToDecInt) { month =>
          implicit val returnPeriod = ReturnPeriod(LocalDate.of(year, month, 1))
          val ownBrandLitres = getRandomLitreage
          val packLargeLitres = getRandomLitreage
          val importLargeLitres = getRandomLitreage
          val sdilReturn = getSdilReturn(ownBrand = ownBrandLitres, packLarge = packLargeLitres, importLarge = importLargeLitres)
          val expectedTaxEstimation = 4 * (lowerBandCostPerLitreMap(year) * expectedLevied(Long, Long).lower + higherBandCostPerLitreMap(year) * expectedLevied(Long, Long).higher)
          sdilReturn.taxEstimation mustBe expectedTaxEstimation.setScale(2, BigDecimal.RoundingMode.HALF_UP)
        }
      }

      s"calculate total packed, total imported, and tax estimation correctly with non-zero litres totals when return amount is negative using $year rates for Apr - Dec $year" in {
        forAll(aprToDecInt) { month =>
          implicit val returnPeriod = ReturnPeriod(LocalDate.of(year, month, 1))
          val exportLitres = getRandomLitreage
          val wastageLitres = getRandomLitreage
          val sdilReturn = getSdilReturn(`export` = exportLitres, wastage = wastageLitres)
          val expectedTaxEstimation = BigDecimal("0.00")
          sdilReturn.taxEstimation mustBe expectedTaxEstimation.setScale(2, BigDecimal.RoundingMode.HALF_UP)
        }
      }

      s"calculate total packed, total imported, and tax estimation correctly with non-zero litres totals with litres packed at own site using $year rates for Jan - Mar ${year + 1}" in {
        forAll(posLitresInts) { lowLitres =>
          forAll(posLitresInts) { highLitres =>
            forAll(janToMarInt) { month =>
              implicit val returnPeriod = ReturnPeriod(LocalDate.of(year + 1, month, 1))
              val sdilReturn = getSdilReturn(ownBrand = (lowLitres, highLitres))



              val expectedTotalPacked = 0
              val expectedTotalImported = 0
              val expectedTaxEstimation = 4 * (lowerBandCostPerLitreMap(year) * lowLitres + higherBandCostPerLitreMap(year) * highLitres)


              sdilReturn.totalPacked mustBe expectedTotalPacked
              sdilReturn.totalImported mustBe expectedTotalImported
              sdilReturn.taxEstimation mustBe expectedTaxEstimation.setScale(2, BigDecimal.RoundingMode.HALF_UP)
            }
          }
        }
      }

      s"calculate total packed, total imported, and tax estimation correctly with non-zero litres totals with litres contract packed using $year rates for Jan - Mar ${year + 1}" in {
        forAll(posLitresInts) { lowLitres =>
          forAll(posLitresInts) { highLitres =>
            forAll(janToMarInt) { month =>
              implicit val returnPeriod = ReturnPeriod(LocalDate.of(year + 1, month, 1))
              val sdilReturn = getSdilReturn(packLarge = (lowLitres, highLitres))



              val expectedTotalPacked = 0
              val expectedTotalImported = 0
              val expectedTaxEstimation = 4 * (lowerBandCostPerLitreMap(year) * lowLitres + higherBandCostPerLitreMap(year) * highLitres)


              sdilReturn.totalPacked mustBe expectedTotalPacked
              sdilReturn.totalImported mustBe expectedTotalImported
              sdilReturn.taxEstimation mustBe expectedTaxEstimation.setScale(2, BigDecimal.RoundingMode.HALF_UP)
            }
          }
        }
      }

      s"calculate total packed, total imported, and tax estimation correctly with non-zero litres totals with exemptions for small producers using $year rates for Jan - Mar ${year + 1}" in {
        forAll(posLitresInts) { lowLitres =>
          forAll(posLitresInts) { highLitres =>
            forAll(janToMarInt) { month =>
              implicit val returnPeriod = ReturnPeriod(LocalDate.of(year + 1, month, 1))
              val sdilReturn = getSdilReturn(numberOfPackSmall = 2)
              val expectedTotalPacked = 0
              val expectedTotalImported = 0
              val expectedTaxEstimation = BigDecimal("0.00")


              sdilReturn.totalPacked mustBe expectedTotalPacked
              sdilReturn.totalImported mustBe expectedTotalImported
              sdilReturn.taxEstimation mustBe expectedTaxEstimation.setScale(2, BigDecimal.RoundingMode.HALF_UP)
            }
          }
        }
      }

      s"calculate total packed, total imported, and tax estimation correctly with non-zero litres totals with litres brought into the uk using $year rates for Jan - Mar ${year + 1}" in {
        forAll(posLitresInts) { lowLitres =>
          forAll(posLitresInts) { highLitres =>
            forAll(janToMarInt) { month =>
              implicit val returnPeriod = ReturnPeriod(LocalDate.of(year + 1, month, 1))
              val sdilReturn = getSdilReturn(importLarge = (lowLitres, highLitres))



              val expectedTotalPacked = 0
              val expectedTotalImported = 0
              val expectedTaxEstimation = 4 * (lowerBandCostPerLitreMap(year) * lowLitres + higherBandCostPerLitreMap(year) * highLitres)


              sdilReturn.totalPacked mustBe expectedTotalPacked
              sdilReturn.totalImported mustBe expectedTotalImported
              sdilReturn.taxEstimation mustBe expectedTaxEstimation.setScale(2, BigDecimal.RoundingMode.HALF_UP)
            }
          }
        }
      }

      s"calculate total packed, total imported, and tax estimation correctly with non-zero litres totals with litres brought into the uk from small producers using $year rates for Jan - Mar ${year + 1}" in {
        forAll(posLitresInts) { lowLitres =>
          forAll(posLitresInts) { highLitres =>
            forAll(janToMarInt) { month =>
              implicit val returnPeriod = ReturnPeriod(LocalDate.of(year + 1, month, 1))
              val sdilReturn = getSdilReturn(importSmall = (lowLitres, highLitres))
              val expectedTotalPacked = 0
              val expectedTotalImported = 0
              val expectedTaxEstimation = BigDecimal("0.00")


              sdilReturn.totalPacked mustBe expectedTotalPacked
              sdilReturn.totalImported mustBe expectedTotalImported
              sdilReturn.taxEstimation mustBe expectedTaxEstimation.setScale(2, BigDecimal.RoundingMode.HALF_UP)
            }
          }
        }
      }

      s"calculate total packed, total imported, and tax estimation correctly with non-zero litres totals with credits for litres exported using $year rates for Jan - Mar ${year + 1}" in {
        forAll(posLitresInts) { lowLitres =>
          forAll(posLitresInts) { highLitres =>
            forAll(janToMarInt) { month =>
              implicit val returnPeriod = ReturnPeriod(LocalDate.of(year + 1, month, 1))
              val sdilReturn = getSdilReturn(`export` = (lowLitres, highLitres))
                  val expectedTotalPacked = 0
              val expectedTotalImported = 0
              val expectedTaxEstimation = BigDecimal("0.00")


              sdilReturn.totalPacked mustBe expectedTotalPacked
              sdilReturn.totalImported mustBe expectedTotalImported
              sdilReturn.taxEstimation mustBe expectedTaxEstimation.setScale(2, BigDecimal.RoundingMode.HALF_UP)
            }
          }
        }
      }

      s"calculate total packed, total imported, and tax estimation correctly with non-zero litres totals with credits for litres lost or damaged using $year rates for Jan - Mar ${year + 1}" in {
        forAll(posLitresInts) { lowLitres =>
          forAll(posLitresInts) { highLitres =>
            forAll(janToMarInt) { month =>
              implicit val returnPeriod = ReturnPeriod(LocalDate.of(year + 1, month, 1))
              val sdilReturn = getSdilReturn(wastage = (lowLitres, highLitres))
                  val expectedTotalPacked = 0
              val expectedTotalImported = 0
              val expectedTaxEstimation = BigDecimal("0.00")


              sdilReturn.totalPacked mustBe expectedTotalPacked
              sdilReturn.totalImported mustBe expectedTotalImported
              sdilReturn.taxEstimation mustBe expectedTaxEstimation.setScale(2, BigDecimal.RoundingMode.HALF_UP)
            }
          }
        }
      }

      s"calculate total packed, total imported, and tax estimation correctly with non-zero litres totals when return amount is 0 using $year rates for Jan - Mar ${year + 1}" in {
        forAll(janToMarInt) { month =>
          implicit val returnPeriod = ReturnPeriod(LocalDate.of(year + 1, month, 1))
          val sdilReturn = getSdilReturn()
          val expectedTaxEstimation = BigDecimal("0.00")
          sdilReturn.taxEstimation mustBe expectedTaxEstimation
        }
      }

      s"calculate total packed, total imported, and tax estimation correctly with non-zero litres totals when return amount to pay using $year rates for Jan - Mar ${year + 1}" in {
        forAll(janToMarInt) { month =>
          implicit val returnPeriod = ReturnPeriod(LocalDate.of(year + 1, month, 1))
          val ownBrandLitres = getRandomLitreage
          val packLargeLitres = getRandomLitreage
          val importLargeLitres = getRandomLitreage
          val sdilReturn = getSdilReturn(ownBrand = ownBrandLitres, packLarge = packLargeLitres, importLarge = importLargeLitres)
          val expectedTaxEstimation = 4 * (lowerBandCostPerLitreMap(year) * expectedLevied(Long, Long).lower + higherBandCostPerLitreMap(year) * expectedLevied(Long, Long).higher)
          sdilReturn.taxEstimation mustBe expectedTaxEstimation.setScale(2, BigDecimal.RoundingMode.HALF_UP)
        }
      }

      s"calculate total packed, total imported, and tax estimation correctly with non-zero litres totals when return amount is negative using $year rates for Jan - Mar ${year + 1}" in {
        forAll(janToMarInt) { month =>
          implicit val returnPeriod = ReturnPeriod(LocalDate.of(year + 1, month, 1))
          val exportLitres = getRandomLitreage
          val wastageLitres = getRandomLitreage
          val sdilReturn = getSdilReturn(`export` = exportLitres, wastage = wastageLitres)
          val expectedTaxEstimation = BigDecimal("0.00")
          sdilReturn.taxEstimation mustBe expectedTaxEstimation.setScale(2, BigDecimal.RoundingMode.HALF_UP)
        }
      }
    })

  }
}
