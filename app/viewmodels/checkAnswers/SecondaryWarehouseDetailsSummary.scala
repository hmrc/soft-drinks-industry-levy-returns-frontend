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
import models.{CheckMode, UserAnswers, Warehouse}
import pages.{AskSecondaryWarehouseInReturnPage, SecondaryWarehouseDetailsPage}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.{Actions, Key, SummaryList}
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.AddressFormattingHelper
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object SecondaryWarehouseDetailsSummary  {

  def warehouseList(answers: UserAnswers)(implicit messages: Messages): SummaryListRow = {
    val value = 1.toString
    SummaryListRow(
      key = "secondaryWarehouseDetails.warehouseList.checkYourAnswersLabel",
      value = ValueViewModel(value)
    )
  }

  def row(answers: UserAnswers)(implicit messages: Messages): Option[SummaryListRow] =
    answers.get(SecondaryWarehouseDetailsPage).map {
      answer =>
        val value = if (answer) "site.yes" else "site.no"
        SummaryListRowViewModel(
          key     = "secondaryWarehouseDetails.checkYourAnswersLabel",
          value   = ValueViewModel(value).withCssClass("align-right"),
          actions = Seq(
            ActionItemViewModel("site.change", routes.SecondaryWarehouseDetailsController.onPageLoad(CheckMode).url)
              .withVisuallyHiddenText(messages("secondaryWarehouseDetails.change.hidden"))
          )
        )
    }

  def row2(warehouseList: Map[String, Warehouse])(implicit messages: Messages): List[SummaryListRow] = {
    warehouseList.map {
      warehouse =>
        SummaryListRow(
          key     = Key(HtmlContent(AddressFormattingHelper.addressFormatting(warehouse._2.address, warehouse._2.tradingName))),
          classes = "govuk-!-font-weight-regular govuk-!-width-two-thirds",
          actions = Some(Actions("",Seq(
            ActionItemViewModel("site.remove", routes.RemoveWarehouseConfirmController.onPageLoad(warehouse._1).url)
              .withVisuallyHiddenText(messages("secondaryWarehouseDetails.remove.hidden"))
          )))
        )
    }
  }.toList
}
