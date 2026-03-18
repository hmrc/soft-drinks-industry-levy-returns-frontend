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

import base.LevyCalculationTestHelper.levyCalculation
import base.ReturnsTestData.sdilNumber
import base.SpecBase
import cats.implicits.catsSyntaxSemigroup
import connectors.SoftDrinksIndustryLevyConnector
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.{reset, verify, when}
import org.scalacheck.Gen
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import uk.gov.hmrc.http.HeaderCarrier

import java.time.LocalDate
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class SdilReturnsModelSpec extends SpecBase with MockitoSugar with DataHelper with ScalaCheckPropertyChecks {

  implicit override lazy val hc: HeaderCarrier = HeaderCarrier()
  val mockConnector: SoftDrinksIndustryLevyConnector = mock[SoftDrinksIndustryLevyConnector]

  private def getRandomLitres:              Long         = Math.floor(Math.random() * 1000000).toLong
  private def getRandomLitreage:            (Long, Long) = (getRandomLitres, getRandomLitres)
  private def getRandomSdilRef(index: Int): String       = s"${Math.floor(Math.random() * 1000).toLong}SdilRef$index"

  private def getSdilReturn(
    ownBrand:    (Long, Long) = (0L, 0L),
    packLarge:   (Long, Long) = (0L, 0L),
    packSmall:   List[(Long, Long)] = List.empty,
    importLarge: (Long, Long) = (0L, 0L),
    importSmall: (Long, Long) = (0L, 0L),
    `export`:    (Long, Long) = (0L, 0L),
    wastage:     (Long, Long) = (0L, 0L)
  ): SdilReturn = {
    val smallProducers: Seq[SmallProducer] = packSmall.zipWithIndex
      .map(litreageWithIndex => SmallProducer(getRandomSdilRef(litreageWithIndex._2), getRandomSdilRef(litreageWithIndex._2), litreageWithIndex._1))
    SdilReturn(ownBrand, packLarge, packSmall = smallProducers.toList, importLarge, importSmall, `export`, wastage, submittedOn = None)
  }

  private val zero: (Long, Long) = (0L, 0L)

  "SdilReturn" - {
    val posLitresInts  = Gen.choose(1000, 10000000)
    val returnPeriod   = ReturnPeriod(2024, 1)
    val expectedResult = BigDecimal("500.00")
    val levyCalc       = levyCalculation(BigDecimal("200"), BigDecimal("300"))

    "totalPacked" - {
      "should sum packLarge and packSmall litres" in {
        forAll(posLitresInts) { lowLitres =>
          forAll(posLitresInts) { highLitres =>
            val sdilReturn         = getSdilReturn(packLarge = (lowLitres, highLitres), packSmall = List((100L, 200L)))
            val expectedPacked     = (lowLitres + 100L, highLitres + 200L)
            sdilReturn.totalPacked mustBe expectedPacked
          }
        }
      }
    }

    "totalImported" - {
      "should sum importLarge and importSmall litres" in {
        forAll(posLitresInts) { lowLitres =>
          forAll(posLitresInts) { highLitres =>
            val sdilReturn           = getSdilReturn(importLarge = (lowLitres, highLitres), importSmall = (100L, 200L))
            val expectedImported     = (lowLitres + 100L, highLitres + 200L)
            sdilReturn.totalImported mustBe expectedImported
          }
        }
      }
    }

    "taxEstimation" - {

      "should call connector with 4x the levied litreage for ownBrand only" in {
        reset(mockConnector)
        val sdilReturn = getSdilReturn(ownBrand = (1000L, 2000L))
        val expectedLow  = 4 * 1000L
        val expectedHigh = 4 * 2000L

        when(mockConnector.calculateLevy(any(), eqTo(expectedLow), eqTo(expectedHigh), eqTo(returnPeriod))(using any()))
          .thenReturn(Future.successful(levyCalc))

        val result = sdilReturn.taxEstimation(sdilNumber, mockConnector, returnPeriod)

        whenReady(result) { total =>
          total mustBe levyCalc.totalRoundedDown
          verify(mockConnector).calculateLevy(any(), eqTo(expectedLow), eqTo(expectedHigh), eqTo(returnPeriod))(using any())
        }
      }

      "should call connector with 4x the levied litreage for packLarge + importLarge + ownBrand" in {
        reset(mockConnector)
        val sdilReturn = getSdilReturn(ownBrand = (100L, 200L), packLarge = (300L, 400L), importLarge = (500L, 600L))
        val liableLitres: (Long, Long) = (100L, 200L) |+| (300L, 400L) |+| (500L, 600L)
        val expectedLow  = 4 * liableLitres._1
        val expectedHigh = 4 * liableLitres._2

        when(mockConnector.calculateLevy(any(), eqTo(expectedLow), eqTo(expectedHigh), eqTo(returnPeriod))(using any()))
          .thenReturn(Future.successful(levyCalc))

        val result = sdilReturn.taxEstimation(sdilNumber, mockConnector, returnPeriod)

        whenReady(result) { total =>
          total mustBe levyCalc.totalRoundedDown
        }
      }

      "should exclude packSmall, importSmall, export, and wastage from estimation" in {
        reset(mockConnector)
        val sdilReturn = getSdilReturn(
          packSmall = List((1000L, 2000L)),
          importSmall = (3000L, 4000L),
          `export` = (5000L, 6000L),
          wastage = (7000L, 8000L)
        )
        // Only packLarge + importLarge + ownBrand contribute, all are zero
        when(mockConnector.calculateLevy(any(), eqTo(0L), eqTo(0L), eqTo(returnPeriod))(using any()))
          .thenReturn(Future.successful(LevyCalculation.zero))

        val result = sdilReturn.taxEstimation(sdilNumber, mockConnector, returnPeriod)

        whenReady(result) { total =>
          total mustBe BigDecimal("0.00")
        }
      }
    }
  }
}
