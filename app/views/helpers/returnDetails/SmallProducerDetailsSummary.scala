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
import models.{CheckMode, SmallProducer}
import pages.{QuestionPage, SmallProducerDetailsPage}
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.Aliases.SummaryList
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import viewmodels.govuk.summarylist._
import viewmodels.implicits._


object SmallProducerDetailsSummary extends SummaryListRowLitresHelper with ReturnDetailsSummaryRowHelper {


  override val actionUrl: String = routes.SmallProducerDetailsController.onPageLoad(CheckMode).url
  override val bandActionIdKey: String = "small-producers"
  override val bandHiddenKey: String = "contractPackedForRegisteredSmallProducers"
  override val page: QuestionPage[Boolean] = SmallProducerDetailsPage
  override val key: String = "smallProducerDetails.checkYourAnswersLabel"
  override val action: String = routes.SmallProducerDetailsController.onPageLoad(CheckMode).url
  override val actionId: String = "change-small-producer-details"
  override val hiddenText: String = "smallProducerDetails"
  override val hasZeroLevy: Boolean = true

  def producerList(smallProducersList: List[SmallProducer])(implicit messages: Messages): SummaryList = {
    val rows = smallProducersList.map {
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

    SummaryListViewModel(rows)
  }


}
