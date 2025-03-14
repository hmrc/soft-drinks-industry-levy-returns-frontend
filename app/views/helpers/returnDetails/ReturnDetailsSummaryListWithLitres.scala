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

import config.FrontendAppConfig
import models.{ LitresInBands, UserAnswers }
import pages.QuestionPage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.SummaryList
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist._

trait ReturnDetailsSummaryListWithLitres extends ReturnDetailsSummaryRowHelper {

  val page: QuestionPage[Boolean]
  val optLitresPage: Option[QuestionPage[LitresInBands]]
  val summaryLitres: SummaryListRowLitresHelper
  val key: String
  val action: String
  val actionId: String
  val hiddenText: String
  val isSmallProducerLitres: Boolean = false

  def summaryList(userAnswers: UserAnswers, isCheckAnswers: Boolean)(implicit messages: Messages, config: FrontendAppConfig): SummaryList = {
    val litresDetails: Seq[SummaryListRow] = optLitresPage match {
      case Some(litresPage) => getLitresDetails(userAnswers, isCheckAnswers, litresPage)
      case None if isSmallProducerLitres => getLitresForSmallProducer(userAnswers, isCheckAnswers)
      case None => Seq.empty
    }
    SummaryListViewModel(rows =
      row(userAnswers, isCheckAnswers) ++ litresDetails)
  }

  private def getLitresDetails(userAnswers: UserAnswers, isCheckAnswers: Boolean, litresPage: QuestionPage[LitresInBands])(implicit messages: Messages, config: FrontendAppConfig): Seq[SummaryListRow] = {
    (userAnswers.get(page), userAnswers.get(litresPage)) match {
      case (Some(true), Some(litresInBands)) => summaryLitres.rows(litresInBands, userAnswers.returnPeriod, isCheckAnswers)
      case _ => Seq.empty
    }
  }

  private def getLitresForSmallProducer(userAnswers: UserAnswers, isCheckAnswers: Boolean)(implicit messages: Messages, config: FrontendAppConfig): Seq[SummaryListRow] = {
    val smallProducerList = userAnswers.smallProducerList
    if (userAnswers.get(page).contains(true) && smallProducerList.nonEmpty) {
      val lowBandLitres = smallProducerList.map(_.litreage._1).sum
      val highBandLitres = smallProducerList.map(_.litreage._2).sum
      val litresInBands = LitresInBands(lowBandLitres, highBandLitres)
      summaryLitres.rows(litresInBands, userAnswers.returnPeriod, isCheckAnswers)
    } else {
      Seq.empty
    }
  }

}
