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
import pages.HowManyCreditsForExportPage
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{Actions, SummaryListRow}
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object HowManyCreditsForExportSummary  {

  def lowBandRow(answers: UserAnswers)(implicit messages: Messages): Option[SummaryListRow] =
    answers.get(HowManyCreditsForExportPage).map {
      answer =>
        val value = HtmlFormat.escape(answer.lowBand.toString).toString
        SummaryListRow(
          key = "litresInTheLowBand",
          value = ValueViewModel(HtmlContent(value)),
          classes = "govuk-summary-list__row--no-border",
          actions = Some(Actions("",
            items =
              Seq(
                ActionItemViewModel("site.change", routes.HowManyCreditsForExportController.onPageLoad(CheckMode).url)
                  .withAttribute("id", "change-lowband-export-credits")
                  .withVisuallyHiddenText(messages("exported.lowband.hidden"))
              )))
        )
    }


  def lowBandLevyRow(answers: UserAnswers, lowBandCostPerLitre: BigDecimal)(implicit messages: Messages): Option[SummaryListRow] = {

    answers.get(HowManyCreditsForExportPage).map {
      answer =>
        val levy = "-£" + String.format("%.2f", (answer.lowBand * lowBandCostPerLitre.toDouble))
        val value = HtmlFormat.escape(levy).toString

        SummaryListRowViewModel(
          key = "lowBandLevy",
          value = ValueViewModel(HtmlContent(value)),
          actions = Seq()
        )
    }
  }

  def highBandRow(answers: UserAnswers)(implicit messages: Messages): Option[SummaryListRow] =
    answers.get(HowManyCreditsForExportPage).map {
      answer =>
        val value = HtmlFormat.escape(answer.highBand.toString).toString + "<br/>"

        SummaryListRow(
          key = "litresInTheHighBand",
          value = ValueViewModel(HtmlContent(value)),
          classes = "govuk-summary-list__row--no-border",
          actions = Some(
            Actions("",
              items =
                Seq(
                  ActionItemViewModel("site.change", routes.HowManyCreditsForExportController.onPageLoad(CheckMode).url)
                    .withAttribute("id", "change-highband-export-credits")
                    .withVisuallyHiddenText(messages("exported.highband.hidden"))
                )))
        )
    }

  def highBandLevyRow(answers: UserAnswers, highBandCostPerLitre: BigDecimal)(implicit messages: Messages): Option[SummaryListRow] =
    answers.get(HowManyCreditsForExportPage).map {
      answer =>
        val levy = "-£" + String.format("%.2f", (answer.highBand * highBandCostPerLitre.toDouble))
        val value = HtmlFormat.escape(levy).toString

        SummaryListRowViewModel(
          key = "highBandLevy",
          value = ValueViewModel(HtmlContent(value)),
          actions = Seq()
        )
    }

}
