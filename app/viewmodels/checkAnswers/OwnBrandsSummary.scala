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
import pages.OwnBrandsPage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object OwnBrandsSummary  {

  def row(answers: UserAnswers, checkAnswers: Boolean)(implicit messages: Messages): Option[SummaryListRow] = {

    answers.get(OwnBrandsPage).map {
      answer =>

        val value = if (answer) "site.yes" else "site.no"

        SummaryListRowViewModel(
          key = "ReportingOwnBrandsPackagedAtYourOwnSite.checkYourAnswersLabel",
          value = ValueViewModel(value),
          actions = if(checkAnswers == true){ Seq(
            ActionItemViewModel("site.change", routes.OwnBrandsController.onPageLoad(CheckMode).url)
              .withAttribute("id", "change-own-brands")
              .withVisuallyHiddenText(messages("ownBrands.change.hidden")) // TODO - what should this say?
          )}else Seq()
        )

    }
  }

}
