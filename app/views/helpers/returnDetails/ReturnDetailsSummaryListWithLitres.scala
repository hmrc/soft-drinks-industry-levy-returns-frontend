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

package views.helpers.returnDetails

import models.{LevyCalculation, LitresInBands, UserAnswers}
import pages.QuestionPage
import play.api.Logging
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.SummaryList
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist.*

trait ReturnDetailsSummaryListWithLitres extends ReturnDetailsSummaryRowHelper with Logging {

  val page:          QuestionPage[Boolean]
  val optLitresPage: Option[QuestionPage[LitresInBands]]
  val summaryLitres: SummaryListRowLitresHelper
  val key:           String
  val action:        String
  val actionId:      String
  val hiddenText:    String
  val isSmallProducerLitres: Boolean = false

  def summaryList(userAnswers: UserAnswers, isCheckAnswers: Boolean, levyCalculations: Map[(Long, Long), LevyCalculation])(implicit
    messages: Messages
  ): SummaryList = {
    val litresDetails: Seq[SummaryListRow] = optLitresPage match {
      case Some(litresPage)              => getLitresDetails(userAnswers, isCheckAnswers, litresPage, levyCalculations)
      case None if isSmallProducerLitres => getLitresForSmallProducer(userAnswers, isCheckAnswers, levyCalculations)
      case None                          => Seq.empty
    }
    SummaryListViewModel(rows = row(userAnswers, isCheckAnswers) ++ litresDetails)
  }

  private def getLitresDetails(
    userAnswers:      UserAnswers,
    isCheckAnswers:   Boolean,
    litresPage:       QuestionPage[LitresInBands],
    levyCalculations: Map[(Long, Long), LevyCalculation]
  )(implicit messages: Messages): Seq[SummaryListRow] =
    (userAnswers.get(page), userAnswers.get(litresPage)) match {
      case (Some(true), Some(litresInBands)) =>
        val litresKey = (litresInBands.lowBand, litresInBands.highBand)
        val levyCalculation = levyCalculations.getOrElse(
          litresKey, {
            logger.warn(s"Missing levy calculation for litres $litresKey, using zero")
            LevyCalculation.zero
          }
        )
        summaryLitres.rows(litresInBands, levyCalculation, isCheckAnswers)
      case _ => Seq.empty
    }

  private def getLitresForSmallProducer(
    userAnswers:      UserAnswers,
    isCheckAnswers:   Boolean,
    levyCalculations: Map[(Long, Long), LevyCalculation]
  )(implicit messages: Messages): Seq[SummaryListRow] = {
    val smallProducerList = userAnswers.smallProducerList
    if userAnswers.get(page).contains(true) && smallProducerList.nonEmpty then {
      val lowBandLitres  = smallProducerList.map(_.litreage._1).sum
      val highBandLitres = smallProducerList.map(_.litreage._2).sum
      val litresInBands  = LitresInBands(lowBandLitres, highBandLitres)
      val litresKey = (lowBandLitres, highBandLitres)
      val levyCalculation = levyCalculations.getOrElse(
        litresKey, {
          logger.warn(s"Missing levy calculation for small producer litres $litresKey, using zero")
          LevyCalculation.zero
        }
      )
      summaryLitres.rows(litresInBands, levyCalculation, isCheckAnswers)
    } else {
      Seq.empty
    }
  }

}
