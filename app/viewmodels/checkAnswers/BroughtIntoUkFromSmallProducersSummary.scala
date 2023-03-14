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

import controllers.routes
import models.{CheckMode, UserAnswers}
import pages.BroughtIntoUkFromSmallProducersPage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object BroughtIntoUkFromSmallProducersSummary  {

  def returnsRow(answers: UserAnswers)(implicit messages: Messages): Option[SummaryListRow] = {
    answers.get(BroughtIntoUkFromSmallProducersPage).map {
      answer =>
        val value = if (answer) "site.yes" else "site.no"
        SummaryListRow(
          key = "broughtIntoUKFromSmallProducers.checkYourAnswersLabel",
          value = ValueViewModel(value)
        )
    }
  }

  def row(answers: UserAnswers)(implicit messages: Messages): Option[SummaryListRow] = {
    answers.get(BroughtIntoUkFromSmallProducersPage).map {
      answer =>

        val value = if (answer) "site.yes" else "site.no"

        SummaryListRowViewModel(
          key     = "reportingLiableDrinksBroughtIntoTheUKFromSmallProducers",
          value   = ValueViewModel(value).withCssClass("align-right"),
          actions = Seq(
            ActionItemViewModel("site.change", routes.BroughtIntoUkFromSmallProducersController.onPageLoad(CheckMode).url)
              .withAttribute("id", "change-brought-into-uk-small-producers")
              .withVisuallyHiddenText(messages("broughtIntoUkFromSmallProducers.change.hidden"))
          )
        )
    }
  }

}
