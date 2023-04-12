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
import controllers.routes
import models.{CheckMode, UserAnswers}
import pages.ExemptionsForSmallProducersPage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import uk.gov.hmrc.govukfrontend.views.Aliases.SummaryList
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object ExemptionsForSmallProducersSummary extends ReturnDetailsSummaryList {

  override def summaryList(userAnswers: UserAnswers, isCheckAnswers: Boolean)(implicit messages: Messages, config: FrontendAppConfig): SummaryList = {
    val litreDetails = if (userAnswers.get(ExemptionsForSmallProducersPage).contains(true)) {
      SmallProducerDetailsSummary.rows(userAnswers, isCheckAnswers)
    } else {
      Seq.empty
    }
    SummaryListViewModel(rows =
      row(userAnswers, isCheckAnswers) ++ litreDetails
    )
  }

  def row(answers: UserAnswers, isCheckAnswers: Boolean = false)(implicit messages: Messages): Seq[SummaryListRow] = {
    answers.get(ExemptionsForSmallProducersPage).fold[Seq[SummaryListRow]](Seq.empty) {
      answer =>
        val value = if (answer) "site.yes" else "site.no"
        Seq(
          SummaryListRowViewModel(
            key     = "exemptionForRegisteredSmallProducers",
            value   = ValueViewModel(value).withCssClass("align-right"),
            actions = if (isCheckAnswers) {
              Seq(
              ActionItemViewModel("site.change", routes.ExemptionsForSmallProducersController.onPageLoad(CheckMode).url)
                .withAttribute(("id", "change-exemption-small-producers"))
                .withVisuallyHiddenText(messages("exemptionsForSmallProducers.change.hidden"))
            )
            } else {
              Seq.empty
            }
          )
        )
    }
  }
}
