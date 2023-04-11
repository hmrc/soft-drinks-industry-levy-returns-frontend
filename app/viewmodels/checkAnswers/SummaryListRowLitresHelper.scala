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
import models.{LitresInBands, UserAnswers}
import pages.QuestionPage
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{Actions, SummaryListRow}
import utilitlies.CurrencyFormatter
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

trait SummaryListRowLitresHelper {

  val actionUrl: String
  val bandActionIdKey: String
  val bandHiddenKey: String
  val page: QuestionPage[LitresInBands]

  val lowBand = "lowband"
  val highBand = "highband"

  def rows(answers: UserAnswers, isCheckAnswers: Boolean)(implicit messages: Messages, config: FrontendAppConfig): Seq[SummaryListRow] = {
    Seq(
      lowBandRow(answers, isCheckAnswers),
      lowBandLevyRow(answers, config.lowerBandCostPerLitre),
      highBandRow(answers, isCheckAnswers),
      highBandLevyRow(answers, config.higherBandCostPerLitre)
    ).flatten
  }

  def lowBandRow(answers: UserAnswers, isCheckAnswers: Boolean)(implicit messages: Messages): Option[SummaryListRow] = {
    bandRow(answers, lowBand, isCheckAnswers)
  }

  def lowBandLevyRow(answers: UserAnswers, lowBandCostPerLitre: BigDecimal)(implicit messages: Messages): Option[SummaryListRow] = {
    bandLevyRow(answers, lowBandCostPerLitre, lowBand)
  }

  def highBandRow(answers: UserAnswers, isCheckAnswers: Boolean)(implicit messages: Messages): Option[SummaryListRow] = {
    bandRow(answers, highBand, isCheckAnswers)
  }

  def highBandLevyRow(answers: UserAnswers, highBandCostPerLitre: BigDecimal)(implicit messages: Messages): Option[SummaryListRow] = {
    bandLevyRow(answers, highBandCostPerLitre, highBand)
  }

  private def bandRow(answers: UserAnswers, band: String, isCheckAnswers: Boolean)(implicit messages: Messages): Option[SummaryListRow] = {
    val key = if(band == lowBand) {
      "litresInTheLowBand"
    } else {
      "litresInTheHighBand"
    }
    answers.get(page).map {
      answer =>
        val bandLitres = if(band == lowBand) {
          answer.lowBand.toString
        } else {
          answer.highBand.toString
        }
        val value = HtmlFormat.escape(bandLitres).toString
        SummaryListRow(
          key = key,
          value = ValueViewModel(HtmlContent(value)).withCssClass("align-right"),
          classes = "govuk-summary-list__row--no-border",
          actions = if(isCheckAnswers) {
            Some(Actions("",
              items =
                Seq(
                  ActionItemViewModel("site.change", actionUrl)
                    .withAttribute(("id", s"change-$band-litreage-$bandActionIdKey"))
                    .withVisuallyHiddenText(messages(s"${bandHiddenKey}.$band.hidden")))))
          } else {
            None
          }
        )
    }
  }

  private def bandLevyRow(answers: UserAnswers, bandCostPerLitre: BigDecimal, band: String)(implicit messages: Messages): Option[SummaryListRow] = {
    val key = if (band == lowBand) {
      "lowBandLevy"
    } else {
      "highBandLevy"
    }
    answers.get(page).map {
      answer =>
        val levy = answer.lowBand * bandCostPerLitre.toDouble
        val value = HtmlFormat.escape(CurrencyFormatter.formatAmountOfMoneyWithPoundSign(levy)).toString

        SummaryListRowViewModel(
          key = key,
          value = ValueViewModel(HtmlContent(value)).withCssClass("align-right"),
          actions = Seq()
        )
    }
  }

}
