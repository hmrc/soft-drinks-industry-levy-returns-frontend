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

package utilitlies

import config.FrontendAppConfig
import models.UserAnswers
import pages._

object TotalForQuarter {

  def calculateTotal(userAnswers: UserAnswers, smallProducer: Boolean)(config: FrontendAppConfig) = {
    val lowerBandCostPerLitre: BigDecimal = config.lowerBandCostPerLitre
    val higherBandCostPerLitre: BigDecimal = config.higherBandCostPerLitre

    calculateLowBand(userAnswers, lowerBandCostPerLitre, smallProducer) +
      calculateHighBand(userAnswers, higherBandCostPerLitre, smallProducer)
  }

  private def calculateLowBand(
    userAnswers: UserAnswers,
    lowBandCostPerLitre: BigDecimal,
    smallProducer: Boolean): BigDecimal = {

    val litresPackedAtOwnSite = userAnswers.get(BrandsPackagedAtOwnSitesPage).fold(0L)(_.lowBand)
    val litresAsContractPacker = userAnswers.get(HowManyAsAContractPackerPage).fold(0L)(_.lowBand)
    val litresBroughtIntoTheUk = userAnswers.get(HowManyBroughtIntoUkPage).fold(0L)(_.lowBand)
    val litresExported = userAnswers.get(HowManyCreditsForExportPage).fold(0L)(_.lowBand)
    val litresLostOrDamaged = userAnswers.get(HowManyCreditsForLostDamagedPage).fold(0L)(_.lowBand)

    val total = litresBroughtIntoTheUk + litresAsContractPacker
    val totalCredits = litresExported + litresLostOrDamaged

    smallProducer match {
      case true => (total - totalCredits) * lowBandCostPerLitre
      case _ => (total + litresPackedAtOwnSite - totalCredits) * lowBandCostPerLitre
    }
  }

  private def calculateHighBand(
    userAnswers: UserAnswers,
    highBandCostPerLitre: BigDecimal,
    smallProducer: Boolean): BigDecimal = {

    val litresPackedAtOwnSite = userAnswers.get(BrandsPackagedAtOwnSitesPage).fold(0L)(_.highBand)
    val litresAsContractPacker = userAnswers.get(HowManyAsAContractPackerPage).fold(0L)(_.highBand)
    val litresBroughtIntoTheUk = userAnswers.get(HowManyBroughtIntoUkPage).fold(0L)(_.highBand)
    val litresExported = userAnswers.get(HowManyCreditsForExportPage).fold(0L)(_.highBand)
    val litresLostOrDamaged = userAnswers.get(HowManyCreditsForLostDamagedPage).fold(0L)(_.highBand)

    val total = litresBroughtIntoTheUk + litresAsContractPacker
    val totalCredits = litresExported + litresLostOrDamaged

    smallProducer match {
      case true => (total - totalCredits) * highBandCostPerLitre
      case _ => (total + litresPackedAtOwnSite - totalCredits) * highBandCostPerLitre
    }
  }

}
