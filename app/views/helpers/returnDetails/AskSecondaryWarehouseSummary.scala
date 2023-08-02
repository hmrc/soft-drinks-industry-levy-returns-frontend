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

object AskSecondaryWarehouseSummary {

  def summaryList(userAnswers: UserAnswers, isCheckAnswers: Boolean)
                 (implicit messages: Messages): Option[SummaryList] = {
    userAnswers.get(AskSecondaryWarehouseInReturnPage) match {
      case Some(true) =>
        Some(
          SummaryListViewModel(
            rows = Seq(SummaryListRowViewModel(
              key = "warehouseSites",
              value = ValueViewModel(userAnswers.warehouseList.size.toString).withCssClass("sdil-right-align--desktop"),
              actions = if (isCheckAnswers) {
                Seq(
                  ActionItemViewModel("site.change", routes.AskSecondaryWarehouseInReturnController.onPageLoad(CheckMode).url)
                    .withAttribute(("id", "change-packaging-sites"))
                    .withVisuallyHiddenText(messages("SecondaryWarehouse.change.hidden"))
                )
              } else {
                Seq.empty
              }
            )
            )
          )
        )
      case _ => None
    }
  }

}
