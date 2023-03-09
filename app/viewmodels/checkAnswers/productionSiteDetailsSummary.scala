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
import models.backend.Site
import models.{CheckMode, ProductionSite, UserAnswers}
import pages.ProductionSiteDetailsPage
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.Aliases.{Actions, Content}
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{Key, SummaryListRow}
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

import scala.reflect.internal.util.NoSourceFile.content

object productionSiteDetailsSummary  {

  def row(answers: UserAnswers)(implicit messages: Messages): Option[SummaryListRow] =
    answers.get(ProductionSiteDetailsPage).map {
      answer =>

        val value = if (answer) "site.yes" else "site.no"

        SummaryListRowViewModel(
          key     = "productionSiteDetails.checkYourAnswersLabel",
          value   = ValueViewModel(value),
          actions = Seq(
            ActionItemViewModel("site.change", routes.ProductionSiteDetailsController.onPageLoad(CheckMode).url)
              .withVisuallyHiddenText(messages("productionSiteDetails.change.hidden"))
          )
        )
    }


  def row2(productionSiteList: List[Site])(implicit messages: Messages): List[SummaryListRow] = {
    productionSiteList.map {
      site =>
        val value = ValueViewModel(
          HtmlContent(
            HtmlFormat.escape(site.address.lines.toString())
          )
        )
        SummaryListRow(
          key = Key(
            content = HtmlContent(s"""${site.tradingName.get}<br>${HtmlFormat.escape(site.address.lines.mkString(", "))}<br>${HtmlFormat.escape(site.address.postCode)}"""),
            classes = "govuk-!-font-weight-regular govuk-!-width-two-thirds"
          ),
          actions = if(productionSiteList.length > 1){
            Some(Actions("",Seq(
            ActionItemViewModel("site.edit", routes.IndexController.onPageLoad().url) //TODO
              .withVisuallyHiddenText(messages("smallProducerDetails.edit.hidden")),
            ActionItemViewModel("site.remove", routes.IndexController.onPageLoad().url) //TODO
              .withVisuallyHiddenText(messages("smallProducerDetails.remove.hidden"))
          )))}else    Some(Actions("",Seq(
            ActionItemViewModel("site.edit", routes.IndexController.onPageLoad().url) //TODO
              .withVisuallyHiddenText(messages("smallProducerDetails.edit.hidden")))))
        )
    }
  }
}