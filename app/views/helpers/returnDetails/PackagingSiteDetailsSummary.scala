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
import models.backend.Site
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.{Actions, SummaryList}
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{Key, SummaryListRow}
import viewmodels.AddressFormattingHelper
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object PackagingSiteDetailsSummary {

  def summaryList(packagingSiteList: Map[String, Site])(implicit messages: Messages): SummaryList = {
    SummaryListViewModel(
      rows = row2(packagingSiteList)
    )
  }

  def row2(packagingSiteList: Map[String, Site])(implicit messages: Messages): List[SummaryListRow] = {
    packagingSiteList.map {
      site =>
        SummaryListRow(
          key = Key(
            content =
              HtmlContent(AddressFormattingHelper.addressFormatting(site._2.address, site._2.tradingName)),
            classes = "govuk-!-font-weight-regular govuk-!-width-two-thirds"
          ),
          actions = if (packagingSiteList.size > 1) {
            Some(Actions("", Seq(
              ActionItemViewModel("site.edit", routes.IndexController.onPageLoad().url)
                .withVisuallyHiddenText(messages("packagingSiteDetails.hidden")),
              ActionItemViewModel("site.remove", routes.IndexController.onPageLoad().url)
                .withVisuallyHiddenText(messages("packagingSiteDetails.hidden"))
            )))
          } else {
            Some(Actions("", Seq(
              ActionItemViewModel("site.edit", routes.IndexController.onPageLoad().url)
                .withVisuallyHiddenText(messages("packagingSiteDetails.edit.hidden"))
            )))
          }
        )
    }.toList
  }
}
