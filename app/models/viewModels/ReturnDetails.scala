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

package models.viewModels

import config.FrontendAppConfig
import models.{Amounts, SmallProducer, UserAnswers, Warehouse}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.SummaryList
import viewmodels.checkAnswers._
import viewmodels.govuk.summarylist._

case class ReturnDetails(ownBrandsAnswer: SummaryList,
                         packagedContractPackerAnswers: SummaryList,
                         exemptionsForSmallProducersAnswers: SummaryList,
                         broughtIntoUkAnswers: SummaryList,
                         broughtIntoUkSmallProducerAnswers: SummaryList,
                         claimCreditsForExportsAnswers: SummaryList,
                         claimCreditsForLostDamagedAnswers: SummaryList,
                         smallProducerCheck: Option[List[SmallProducer]],
                         warehouseCheck: Option[List[Warehouse]],
                         smallProducerAnswers: SummaryList,
                         warehouseAnswers: SummaryList,
                         amountsRow: SummaryList
                        ) {
  //ToDo when warehouse list is in user answers remove it from def and use userAnswers
  def this(userAnswers: UserAnswers, isCheckAnswers: Boolean = false, amounts: Amounts, warehouseList: List[Warehouse])
          (implicit messages: Messages, config: FrontendAppConfig) = this(
      ownBrandsAnswer = OwnBrandsSummary.summaryList(userAnswers, isCheckAnswers),
      packagedContractPackerAnswers = PackagedContractPackerSummary.summaryList(userAnswers, isCheckAnswers),
      exemptionsForSmallProducersAnswers = ExemptionsForSmallProducersSummary.summaryList(userAnswers, isCheckAnswers),
      broughtIntoUkAnswers = BroughtIntoUKSummary.summaryList(userAnswers, isCheckAnswers),
      broughtIntoUkSmallProducerAnswers = BroughtIntoUkFromSmallProducersSummary.summaryList(userAnswers,isCheckAnswers),
      claimCreditsForExportsAnswers = ClaimCreditsForExportsSummary.summaryList(userAnswers,isCheckAnswers),
      claimCreditsForLostDamagedAnswers = ClaimCreditsForLostDamagedSummary.summaryList(userAnswers,isCheckAnswers),
      smallProducerCheck = ReturnDetails.smallProducerCheck(userAnswers),
      warehouseCheck = ReturnDetails.warehouseCheck(warehouseList), //TODO use UserAnswers when warehouse is in model
      smallProducerAnswers = SummaryListViewModel(rows = Seq(SmallProducerDetailsSummary.producerList(userAnswers)).flatten),
      warehouseAnswers = SummaryListViewModel(rows = Seq(SecondaryWarehouseDetailsSummary.warehouseList(userAnswers))),
      amountsRow = AmountToPaySummary.amountToPaySummary(amounts)
    )
}

object ReturnDetails {
  private def smallProducerCheck(userAnswers: UserAnswers): Option[List[SmallProducer]] = {
    val smallProducerList = userAnswers.smallProducerList
    if (smallProducerList.length > 0) Some(smallProducerList) else None
  }

  //TODO use UserAnswers when warehouseList is in model
  private def warehouseCheck(warehouseList: List[Warehouse]): Option[List[Warehouse]] = {
    if (warehouseList.length > 0) Some(warehouseList) else None
  }

}
