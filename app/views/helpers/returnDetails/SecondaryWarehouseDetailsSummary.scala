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
import uk.gov.hmrc.govukfrontend.views.Aliases.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.hmrcfrontend.views.viewmodels.listwithactions.{ListWithActions, ListWithActionsAction, ListWithActionsItem}
import viewmodels.AddressFormattingHelper

object SecondaryWarehouseDetailsSummary {

  def warehouseDetailRow(warehouseList: Map[String, Site])(implicit messages: Messages): ListWithActions =
    ListWithActions(
      items = warehouseList.map { warehouse =>
        ListWithActionsItem(
          name = HtmlContent(AddressFormattingHelper.addressFormatting(warehouse._2.address, warehouse._2.tradingName)),
          actions = Seq(
            ListWithActionsAction(
              href = routes.RemoveWarehouseConfirmController.onPageLoad(warehouse._1).url,
              content = Text(messages("site.remove")),
              visuallyHiddenText = Some(
                messages("secondaryWarehouseDetails.remove.hidden", warehouse._2.tradingName.getOrElse(""), warehouse._2.address.lines.head)
              )
            )
          )
        )
      }.toSeq
    )
}
