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

import models.Amounts
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.SummaryList
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import utilitlies.CurrencyFormatter
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object AmountToPaySummary {
  def amountToPaySummary(amounts: Amounts)(implicit messages: Messages): SummaryList = {

    val totalForQuarter: BigDecimal = amounts.totalForQuarter
    val balanceBroughtForward: BigDecimal = amounts.balanceBroughtForward
    val total: BigDecimal = amounts.total

    val negatedBalanceBroughtForward = balanceBroughtForward * -1

    SummaryListViewModel(rows = Seq(
      SummaryListRowViewModel(
        key = "totalThisQuarter",
        value = ValueViewModel(HtmlContent(CurrencyFormatter.formatAmountOfMoneyWithPoundSign(totalForQuarter).replace("-", "&minus;"))).withCssClass("total-for-quarter sdil-right-align--desktop")),
      SummaryListRowViewModel(
        key = "balanceBroughtForward",
        value = ValueViewModel(HtmlContent(CurrencyFormatter.formatAmountOfMoneyWithPoundSign(negatedBalanceBroughtForward).replace("-", "&minus;")))
          .withCssClass("balance-brought-forward sdil-right-align--desktop")),
      SummaryListRowViewModel(
        key = "total",
        value = ValueViewModel(HtmlContent(CurrencyFormatter.formatAmountOfMoneyWithPoundSign(total).replace("-", "&minus;"))).withCssClass("total sdil-right-align--desktop govuk-!-font-weight-bold"))))
  }

  def subheader(total: BigDecimal)(implicit messages: Messages): String = {
    if (total < 0) {
      Messages("yourSoftDrinksLevyAccountsWillBeCredited", CurrencyFormatter.formatAmountOfMoneyWithPoundSign(total * -1).replace("-", "&minus;"))
    } else {
      Messages("youNeedToPay", CurrencyFormatter.formatAmountOfMoneyWithPoundSign(total))
    }
  }
}

