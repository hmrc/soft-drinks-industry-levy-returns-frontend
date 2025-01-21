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

import config.FrontendAppConfig

case class BandRates(lowerBandCostPerLites: BigDecimal, higherBandCostPerLitre: BigDecimal)

case class LevyCalculation(low: BigDecimal, high: BigDecimal) {
  lazy val lowLevy = low.setScale(2, BigDecimal.RoundingMode.HALF_UP)
  lazy val highLevy = high.setScale(2, BigDecimal.RoundingMode.HALF_UP)
  lazy val total = (low + high).setScale(2, BigDecimal.RoundingMode.HALF_UP)
}

object LevyCalculator {

  private[models] def getTaxYear(returnPeriod: ReturnPeriod): Int = {
    returnPeriod.quarter match {
      case 0 => returnPeriod.year - 1
      case _ => returnPeriod.year
    }
  }

  private[models] def getBandRates(taxYear: Int)(implicit frontendAppConfig: FrontendAppConfig): BandRates = {
    taxYear match {
      case year if year < 2025 => BandRates(frontendAppConfig.lowerBandCostPerLitre, frontendAppConfig.higherBandCostPerLitre)
      case 2025 => BandRates(frontendAppConfig.lowerBandCostPerLitrePostApril2025, frontendAppConfig.higherBandCostPerLitrePostApril2025)
//      case 2026 => BandRates(frontendAppConfig.lowerBandCostPerLitrePostApril2026, frontendAppConfig.higherBandCostPerLitrePostApril2026)
    }
  }

  def getLevyCalculation(lowLitres: Long, highLitres: Long, returnPeriod: ReturnPeriod)(implicit frontendAppConfig: FrontendAppConfig): LevyCalculation = {
    val taxYear: Int = getTaxYear(returnPeriod)
    val bandRates: BandRates = getBandRates(taxYear)
    val lowLevy = lowLitres * bandRates.lowerBandCostPerLites
    val highLevy = highLitres * bandRates.higherBandCostPerLitre
    LevyCalculation(lowLevy, highLevy)
  }

}
