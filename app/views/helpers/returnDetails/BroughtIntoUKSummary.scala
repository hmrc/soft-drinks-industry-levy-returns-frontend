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
import pages.BroughtIntoUKPage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.SummaryList
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object BroughtIntoUKSummary extends ReturnDetailsSummaryList  {

  override def summaryList(userAnswers: UserAnswers, isCheckAnswers: Boolean)(implicit messages: Messages, config: FrontendAppConfig): SummaryList = {
    val litresDetails = if (userAnswers.get(BroughtIntoUKPage).contains(true)) {
      HowManyBroughtIntoUkSummary.rows(userAnswers, isCheckAnswers)
    } else {
      Seq.empty
    }
    SummaryListViewModel(rows =
      row(userAnswers) ++ litresDetails
    )
  }

  def row(answers: UserAnswers, isCheckAnswers: Boolean = true)(implicit messages: Messages): Seq[SummaryListRow] = {
    answers.get(BroughtIntoUKPage).fold[Seq[SummaryListRow]](Seq.empty) {
      answer =>
        val value = if (answer) "site.yes" else "site.no"
        Seq(
          SummaryListRowViewModel(
            key     = "reportingLiableDrinksBroughtIntoTheUK",
            value   = ValueViewModel(value).withCssClass("align-right"),
            actions = if(isCheckAnswers) {
              Seq(
                ActionItemViewModel("site.change", routes.BroughtIntoUKController.onPageLoad(CheckMode).url)
                  .withAttribute(("id", "change-brought-into-uk"))
                  .withVisuallyHiddenText(messages("broughtIntoUK.change.hidden"))
              )
            } else {
              Seq.empty
            }
          )
        )
    }
  }
}
