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
import models.LevyCalculator.getLevyCalculation
import models.{ LevyCalculation, LitresInBands, ReturnPeriod }
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{ Actions, SummaryListRow }
import utilitlies.CurrencyFormatter
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

trait SummaryListRowLitresHelper {

  val actionUrl: String
  val bandActionIdKey: String
  val bandHiddenKey: String
  val hasZeroLevy: Boolean = false
  val isNegativeLevy: Boolean = false

  val lowBand = "lowband"
  val highBand = "highband"

  def rows(litresInBands: LitresInBands, returnPeriod: ReturnPeriod, isCheckAnswers: Boolean)(implicit messages: Messages, config: FrontendAppConfig): Seq[SummaryListRow] = {
    val levyCalculation: LevyCalculation = getLevyCalculation(litresInBands.lowBand, litresInBands.highBand, returnPeriod)
    Seq(
      bandRow(litresInBands.lowBand, lowBand, isCheckAnswers),
      bandLevyRow(levyCalculation.lowLevy, lowBand),
      bandRow(litresInBands.highBand, highBand, isCheckAnswers),
      bandLevyRow(levyCalculation.highLevy, highBand))
  }

  private def bandRow(litres: Long, band: String, isCheckAnswers: Boolean)(implicit messages: Messages): SummaryListRow = {
    val key = if (band == lowBand) {
      "litresInTheLowBand"
    } else {
      "litresInTheHighBand"
    }
    val value = HtmlFormat.escape(litres.toString).toString
    SummaryListRow(
      key = key,
      value = ValueViewModel(HtmlContent(value)).withCssClass("sdil-right-align--desktop"),
      classes = "govuk-summary-list__row--no-border",
      actions = action(isCheckAnswers, band))
  }

  private def bandLevyRow(levyAmount: BigDecimal, band: String)(implicit messages: Messages): SummaryListRow = {
    val key = if (band == lowBand) {
      "lowBandLevy"
    } else {
      "highBandLevy"
    }

    val value = HtmlFormat.escape(CurrencyFormatter.formatAmountOfMoneyWithPoundSign(levy(levyAmount))).toString.replace("-", "&minus;")

    SummaryListRow(
      key = key,
      value = ValueViewModel(HtmlContent(value)).withCssClass("sdil-right-align--desktop"),
      classes = "govuk-summary-list__row--no-actions")
  }

  private def levy(levyAmount: BigDecimal): BigDecimal = {
    if (hasZeroLevy) {
      0
    } else if (isNegativeLevy) {
      levyAmount.toDouble * -1
    } else {
      levyAmount.toDouble
    }
  }

  def action(isCheckAnswers: Boolean, band: String)(implicit messages: Messages): Option[Actions] = if (isCheckAnswers) {
    Some(Actions(
      "",
      items =
        Seq(
          ActionItemViewModel("site.change", actionUrl)
            .withAttribute(("id", s"change-$band-litreage-$bandActionIdKey"))
            .withVisuallyHiddenText(messages(s"${bandHiddenKey}.$band.litres.hidden")))))
  } else {
    None
  }

}
