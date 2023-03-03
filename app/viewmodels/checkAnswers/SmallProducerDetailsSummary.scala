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
import models.{CheckMode, ProductionSite, SmallProducer, UserAnswers}
import pages.SmallProducerDetailsPage
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist._
import viewmodels.implicits._


object SmallProducerDetailsSummary  {

  def row(answers: UserAnswers)(implicit messages: Messages): Option[SummaryListRow] =
    answers.get(SmallProducerDetailsPage).map {
      answer =>

        val value = if (answer) "site.yes" else "site.no"

        SummaryListRowViewModel(
          key     = "smallProducerDetails.checkYourAnswersLabel",
          value   = ValueViewModel(value),
          actions = Seq(
            ActionItemViewModel("site.change", routes.SmallProducerDetailsController.onPageLoad(CheckMode).url)
              .withVisuallyHiddenText(messages("smallProducerDetails.change.hidden"))
          )
        )
    }

  def row2(smallProducersList: List[SmallProducer])(implicit messages: Messages): List[SummaryListRow] = {
    smallProducersList.map {
    smallProducer =>
      val value = ValueViewModel(
        HtmlContent(
          HtmlFormat.escape(smallProducer.alias)
        )
      )
      SummaryListRowViewModel(
        key     = smallProducer.sdilRef,
        value   = value,
        actions = Seq(
          ActionItemViewModel("site.edit", routes.AddASmallProducerController.onEditPageLoad(smallProducer.sdilRef).url)
            .withVisuallyHiddenText(messages("smallProducerDetails.edit.hidden")),
          ActionItemViewModel("site.remove", routes.RemoveSmallProducerConfirmController.onPageLoad(smallProducer.sdilRef).url)
            .withVisuallyHiddenText(messages("smallProducerDetails.remove.hidden"))
        )
      )
  }
  }
}
