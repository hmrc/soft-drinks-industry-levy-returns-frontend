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
import pages.BrandsPackagedAtOwnSitesPage
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object BrandsPackagedAtOwnSitesSummary  {

  def lowBandRow(answers: UserAnswers)(implicit messages: Messages): Option[SummaryListRow] =
    answers.get(BrandsPackagedAtOwnSitesPage).map {
      answer =>
        val value = HtmlFormat.escape(answer.lowBand.toString).toString
        SummaryListRowViewModel(
          key     = "litresInTheLowBand",
          value   = ValueViewModel(HtmlContent(value)),
          actions = Seq(
            ActionItemViewModel("site.change", routes.BrandsPackagedAtOwnSitesController.onPageLoad(CheckMode).url)//TODO - replace with correct TARGET PAGE
              .withVisuallyHiddenText(messages("brandsPackagedAtOwnSites.change.hidden"))//TODO - replace with correct hidden content
          )
        )
    }

  def lowBandLevyRow(answers: UserAnswers)(implicit messages: Messages): Option[SummaryListRow] =
    answers.get(BrandsPackagedAtOwnSitesPage).map {
      answer =>
        //TODO - replace with actual calculation
        val value = HtmlFormat.escape("£10000").toString

        SummaryListRowViewModel(
          key = "lowBandLevy",
          value = ValueViewModel(HtmlContent(value)),
          actions = Seq(
            ActionItemViewModel("site.change", routes.BrandsPackagedAtOwnSitesController.onPageLoad(CheckMode).url)//TODO - replace with correct TARGET PAGE
              .withVisuallyHiddenText(messages("brandsPackagedAtOwnSites.change.hidden"))//TODO - replace with correct hidden content
          )
        )
    }

  def highBandRow(answers: UserAnswers)(implicit messages: Messages): Option[SummaryListRow] =
    answers.get(BrandsPackagedAtOwnSitesPage).map {
      answer =>
        val value = HtmlFormat.escape(answer.highBand.toString).toString + "<br/>"

        SummaryListRowViewModel(
          key = "litresInTheHighBand",
          value = ValueViewModel(HtmlContent(value)),
          actions = Seq(
            ActionItemViewModel("site.change", routes.BrandsPackagedAtOwnSitesController.onPageLoad(CheckMode).url)//TODO - replace with correct TARGET PAGE
              .withVisuallyHiddenText(messages("brandsPackagedAtOwnSites.change.hidden")) //TODO - replace with correct hidden content
          )
        )
    }

  def highBandLevyRow(answers: UserAnswers)(implicit messages: Messages): Option[SummaryListRow] =
    answers.get(BrandsPackagedAtOwnSitesPage).map {
      answer =>
        //TODO - replace with actual calculation
        val value = HtmlFormat.escape("£2000").toString

        SummaryListRowViewModel(
          key = "highBandLevy",
          value = ValueViewModel(HtmlContent(value)),
          actions = Seq(
            ActionItemViewModel("site.change", routes.BrandsPackagedAtOwnSitesController.onPageLoad(CheckMode).url)//TODO - replace with correct TARGET PAGE
              .withVisuallyHiddenText(messages("brandsPackagedAtOwnSites.change.hidden"))//TODO - replace with correct hidden content
          )
        )
    }
}
