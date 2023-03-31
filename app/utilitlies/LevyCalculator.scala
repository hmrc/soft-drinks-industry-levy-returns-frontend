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
import models.{SdilCalculation, UserAnswers}
import pages.{BrandsPackagedAtOwnSitesPage, HowManyAsAContractPackerPage, QuestionPage}

import scala.util.{Failure, Success, Try}

object LevyCalculator {


  val lowBandRate = 0.18
  val highBandRate = 0.24

  def levyForLowBand(litreage: Long): Double = {
    // TODO - do results such as 779.939999 need to be rounded down?
    litreage * lowBandRate
  }

  def levyForHighBand(litreage: Long): Double = {
    // TODO - do results such as 779.939999 need to be rounded down?
    litreage * highBandRate
  }

  def calculateLevyForAnswers(answers: UserAnswers): Map[String, SdilCalculation] = {

    val nilCalculation = SdilCalculation(0, 0)

    val brandsPackagedAtOwnSitesCalculation = answers.get(BrandsPackagedAtOwnSitesPage) match {
      case Some(page) => SdilCalculation(levyForLowBand(page.lowBand), levyForHighBand(page.highBand))
      case _ => nilCalculation
    }

    val howManyAsAContractPackerCalculation = answers.get(HowManyAsAContractPackerPage) match {
      case Some(page) => SdilCalculation(levyForLowBand(page.lowBand), levyForHighBand(page.highBand))
      case _ => nilCalculation
    }

    Map(
      BrandsPackagedAtOwnSitesPage.toString -> brandsPackagedAtOwnSitesCalculation,
      HowManyAsAContractPackerPage.toString -> howManyAsAContractPackerCalculation
    )

  }

}
