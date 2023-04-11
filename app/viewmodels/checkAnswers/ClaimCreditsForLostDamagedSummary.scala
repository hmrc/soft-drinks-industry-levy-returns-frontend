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

package viewmodels.checkAnswers

import config.FrontendAppConfig
import controllers.routes
import models.{CheckMode, UserAnswers}
import pages.ClaimCreditsForLostDamagedPage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.SummaryList
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{SummaryList, SummaryListRow}
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object ClaimCreditsForLostDamagedSummary extends ReturnDetailsSummaryList {

  override def summaryList(userAnswers: UserAnswers, isCheckAnswers: Boolean)(implicit messages: Messages, config: FrontendAppConfig): SummaryList = {
    val litresDetails = if (userAnswers.get(ClaimCreditsForLostDamagedPage).contains(true)) {
      HowManyCreditsForExportSummary.rows(userAnswers, isCheckAnswers)
    } else {
      Seq.empty
    }
    SummaryListViewModel(rows =
      row(userAnswers, isCheckAnswers) ++ litresDetails
    )
  }


  def row(answers: UserAnswers, isCheckAnswers: Boolean = false)(implicit messages: Messages): Seq[SummaryListRow] = {
    answers.get(ClaimCreditsForLostDamagedPage).fold[Seq[SummaryListRow]](Seq.empty) {
      answer =>
        val value = if (answer) "site.yes" else "site.no"
        Seq(
          SummaryListRowViewModel(
            key     = "claimingCreditForLostOrDestroyedLiableDrinks",
            value   = ValueViewModel(value).withCssClass("align-right"),
            actions = if (isCheckAnswers) {
              Seq(
              ActionItemViewModel("site.change", routes.ClaimCreditsForLostDamagedController.onPageLoad(CheckMode).url)
                .withAttribute("id", "change-credits-lost-damaged")
                .withVisuallyHiddenText(messages("claimCreditsForLostDamaged.change.hidden"))
            )
            } else {
              Seq.empty
            }
          )
        )
    }
  }
}
