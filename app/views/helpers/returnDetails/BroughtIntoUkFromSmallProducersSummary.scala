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
import models.{CheckMode, LitresInBands}
import pages.{BroughtIntoUkFromSmallProducersPage, HowManyBroughtIntoTheUKFromSmallProducersPage, QuestionPage}

object BroughtIntoUkFromSmallProducersSummary extends ReturnDetailsSummaryListWithLitres {

  override val page: QuestionPage[Boolean] = BroughtIntoUkFromSmallProducersPage
  override val optLitresPage: Option[QuestionPage[LitresInBands]] = Some(HowManyBroughtIntoTheUKFromSmallProducersPage)
  override val summaryLitres: SummaryListRowLitresHelper = HowManyBroughtIntoTheUKFromSmallProducersSummary
  override val key: String = "reportingLiableDrinksBroughtIntoTheUKFromSmallProducers"
  override val action: String = routes.BroughtIntoUkFromSmallProducersController.onPageLoad(CheckMode).url
  override val actionId: String = "change-brought-into-uk-small-producers"
  override val hiddenText: String = "broughtIntoUkFromSmallProducers"
}
