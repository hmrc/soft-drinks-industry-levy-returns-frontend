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

import cats.implicits.catsSyntaxSemigroup
import config.FrontendAppConfig
import models.{ SdilCalculation, UserAnswers }
import pages._

import javax.inject.Inject

class LevyCalculator @Inject() (config: FrontendAppConfig) {

  val lowBandRate = config.lowerBandCostPerLitre
  val highBandRate = config.higherBandCostPerLitre
  val nilCalculation = SdilCalculation(0, 0)
  def calculateLevyForAnswers(answers: UserAnswers): Map[String, SdilCalculation] = {
    Map(
      BrandsPackagedAtOwnSitesPage.toString -> brandsPackagedAtOwnSitesCalculation(answers),
      HowManyAsAContractPackerPage.toString -> howManyAsAContractPackerCalculation(answers),
      ExemptionsForSmallProducersPage.toString -> exemptionForSmallProducersCalculation(answers),
      HowManyBroughtIntoUkPage.toString -> howManyBroughtIntoUKCalculation(answers),
      HowManyBroughtIntoTheUKFromSmallProducersPage.toString -> howManyBroughtIntoUKFromSmallProducersCalculation(answers),
      HowManyCreditsForExportPage.toString -> howManyCreditsForExportsCalculation(answers),
      HowManyCreditsForLostDamagedPage.toString -> howManyCreditsForLostOrDamagedCalculation(answers))
  }

  private def brandsPackagedAtOwnSitesCalculation(answers: UserAnswers) = {
    answers.get(BrandsPackagedAtOwnSitesPage) match {
      case Some(page) => SdilCalculation(levyForLowBand(page.lowBand), levyForHighBand(page.highBand))
      case _ => nilCalculation
    }
  }

  private def howManyAsAContractPackerCalculation(answers: UserAnswers) = {
    answers.get(HowManyAsAContractPackerPage) match {
      case Some(page) => SdilCalculation(levyForLowBand(page.lowBand), levyForHighBand(page.highBand))
      case _ => nilCalculation
    }
  }

  private def exemptionForSmallProducersCalculation(answers: UserAnswers) = {
    val smallProducerTotals = answers.smallProducerList.foldLeft((0L, 0L))((x, y) => (x |+| (y.litreage._1, y.litreage._2)))
    SdilCalculation(levyForLowBand(smallProducerTotals._1), levyForHighBand(smallProducerTotals._2))
  }

  private def howManyBroughtIntoUKCalculation(answers: UserAnswers) = {
    answers.get(HowManyBroughtIntoUkPage) match {
      case Some(page) => SdilCalculation(levyForLowBand(page.lowBand), levyForHighBand(page.highBand))
      case _ => nilCalculation
    }
  }

  private def howManyBroughtIntoUKFromSmallProducersCalculation(answers: UserAnswers) = {
    answers.get(HowManyBroughtIntoTheUKFromSmallProducersPage) match {
      case Some(page) => SdilCalculation(levyForLowBand(page.lowBand), levyForHighBand(page.highBand))
      case _ => nilCalculation
    }
  }

  private def howManyCreditsForExportsCalculation(answers: UserAnswers) = {
    answers.get(HowManyCreditsForExportPage) match {
      case Some(page) => SdilCalculation(levyForLowBand(page.lowBand) * -1, levyForHighBand(page.highBand) * -1)
      case _ => nilCalculation
    }
  }
  private def howManyCreditsForLostOrDamagedCalculation(answers: UserAnswers) = {
    answers.get(HowManyCreditsForLostDamagedPage) match {
      case Some(page) => SdilCalculation(levyForLowBand(page.lowBand) * -1, levyForHighBand(page.highBand) * -1)
      case _ => nilCalculation
    }
  }

  private def levyForLowBand(litreage: Long): Double = {
    (litreage * lowBandRate).toDouble
  }

  private def levyForHighBand(litreage: Long): Double = {
    (litreage * highBandRate).toDouble
  }

}
