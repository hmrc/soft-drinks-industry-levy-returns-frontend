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
import controllers.routes
import models.{CheckMode, UserAnswers}
import pages.{OwnBrandsPage, QuestionPage}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{SummaryList, SummaryListRow}
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object OwnBrandsSummary extends ReturnDetailsSummaryList  {

//  def returnsRow(answers: UserAnswers)(implicit messages: Messages): Option[SummaryListRow] = {
//    answers.get(OwnBrandsPage).map {
//      answer =>
//        val value = if (answer) "site.yes" else "site.no"
//        SummaryListRowViewModel(
//          key = "ReportingOwnBrandsPackagedAtYourOwnSite.checkYourAnswersLabel",
//          value = ValueViewModel(value).withCssClass("align-right")
//        )
//    }
//  }

  override def summaryList(userAnswers: UserAnswers, isCheckAnswers: Boolean)(implicit messages: Messages, config: FrontendAppConfig): SummaryList = {
    val litreDetails = if (userAnswers.get(OwnBrandsPage).contains(true)) {
      BrandsPackagedAtOwnSitesSummary.rows(userAnswers, isCheckAnswers)
    } else {
      Seq.empty
    }
    SummaryListViewModel(rows =
      ownBrandRow(userAnswers, isCheckAnswers) ++ litreDetails
    )
  }


  def ownBrandRow(answers: UserAnswers, isCheckAnswers: Boolean = true)(implicit messages: Messages, config: FrontendAppConfig): Seq[SummaryListRow] = {
      answers.get(OwnBrandsPage).fold[Seq[SummaryListRow]](Seq.empty) {
        answer =>
          val value = if (answer) "site.yes" else "site.no"
          Seq(
            SummaryListRowViewModel(
              key = "reportingOwnBrandsPackagedAtYourOwnSite",
              value = ValueViewModel(value).withCssClass("align-right"),
              actions = if(isCheckAnswers) {
                Seq(
                  ActionItemViewModel("site.change", routes.OwnBrandsController.onPageLoad(CheckMode).url)
                    .withAttribute("id", "change-own-brands")
                    .withVisuallyHiddenText(messages("ownBrands.change.hidden")) // TODO - what should this say?
                )
              } else {
                Seq.empty
              }
            )
          )
      }
    }
}
