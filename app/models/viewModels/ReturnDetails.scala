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
import connectors.SoftDrinksIndustryLevyConnector
import controllers.actions._
import models.requests.DataRequest
import models.retrieved.RetrievedSubscription
import models.viewModels.ReturnDetails
import models.{Address, Amounts, SdilReturn, SmallProducer, UserAnswers, Warehouse}
import pages._
import play.api.Logger
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.{SDILSessionCache, SDILSessionKeys}
import services.ReturnService
import uk.gov.hmrc.govukfrontend.views.Aliases.SummaryList
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utilitlies.ReturnsHelper.extractReturnPeriod
import utilitlies.{CurrencyFormatter, ReturnsHelper}
import viewmodels.checkAnswers._
import viewmodels.govuk.summarylist._
import views.html.ReturnSentView

import javax.inject.Inject
import scala.concurrent.ExecutionContext

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
  def this(userAnswers: UserAnswers, isCheckAnswers: Boolean = false, amounts: Amounts, warehouseList: List[Warehouse])(implicit request: DataRequest[AnyContent], messages: Messages, config: FrontendAppConfig) = this(
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


  def packagedContractPackerAnswers(request: DataRequest[AnyContent], userAnswers: UserAnswers, isCheckAnswers: Boolean = false)(implicit messages: Messages, config: FrontendAppConfig) = {
    PackagedContractPackerSummary.summaryList(userAnswers, isCheckAnswers)
  }

  def exemptionForSmallProducersAnswers(userAnswers: UserAnswers, isCheckAnswers: Boolean = false)(implicit messages: Messages, config: FrontendAppConfig) = {
    ExemptionsForSmallProducersSummary.summaryList(userAnswers, isCheckAnswers)
  }

  def broughtIntoUKAnswers(userAnswers: UserAnswers, isCheckAnswers: Boolean = false)(implicit messages: Messages, config: FrontendAppConfig) = {
    BroughtIntoUKSummary.summaryList(userAnswers, isCheckAnswers)
  }

  def broughtIntoUKFromSmallProducerAnswers(userAnswers: UserAnswers, isCheckAnswers: Boolean = false)(implicit messages: Messages, config: FrontendAppConfig) = {
        BroughtIntoUkFromSmallProducersSummary.summaryList(userAnswers,false)
  }

  def warehouseAnswers(userAnswers: UserAnswers)(implicit messages: Messages) = {
    SummaryListViewModel(rows = Seq(SecondaryWarehouseDetailsSummary.warehouseList(userAnswers)))
  }

  def smallProducerAnswers(userAnswers: UserAnswers)(implicit messages: Messages) = {
    SummaryListViewModel(rows = Seq(SmallProducerDetailsSummary.producerList(userAnswers)).flatten)
  }

  def claimCreditsForLostOrDamagedAnswers(userAnswers: UserAnswers)(implicit messages: Messages) = {
    if (userAnswers.get(ClaimCreditsForLostDamagedPage).getOrElse(false)) {
      SummaryListViewModel(rows = Seq(
        ClaimCreditsForLostDamagedSummary.returnsRow(userAnswers),
        HowManyCreditsForLostDamagedSummary.returnsLowBandRow(userAnswers),
        HowManyCreditsForLostDamagedSummary.returnsLowBandLevyRow(userAnswers, config.lowerBandCostPerLitre),
        HowManyCreditsForLostDamagedSummary.returnsHighBandRow(userAnswers),
        HowManyCreditsForLostDamagedSummary.returnsHighBandLevyRow(userAnswers, config.higherBandCostPerLitre)
      ).flatten)
    } else {
      SummaryListViewModel(rows = Seq(ClaimCreditsForLostDamagedSummary.returnsRow(userAnswers)).flatten)
    }
  }

  def claimCreditsForExportsAnswers(userAnswers: UserAnswers)(implicit messages: Messages) = {
    if (userAnswers.get(ClaimCreditsForExportsPage).getOrElse(false)) {
      SummaryListViewModel(rows = Seq(
        ClaimCreditsForExportsSummary.returnsRow(userAnswers),
        HowManyCreditsForExportSummary.returnsLowBandRow(userAnswers),
        HowManyCreditsForExportSummary.returnsLowBandLevyRow(userAnswers, config.lowerBandCostPerLitre),
        HowManyCreditsForExportSummary.returnsHighBandRow(userAnswers),
        HowManyCreditsForExportSummary.returnsHighBandLevyRow(userAnswers, config.higherBandCostPerLitre)
      ).flatten)
    } else {
      SummaryListViewModel(rows = Seq(ClaimCreditsForExportsSummary.returnsRow(userAnswers)).flatten)
    }
  }

  private def smallProducerCheck(userAnswers: UserAnswers): Option[List[SmallProducer]] = {
    val smallProducerList = userAnswers.smallProducerList
    if (smallProducerList.length > 0) Some(smallProducerList) else None
  }

  //TODO use UserAnswers when warehouseList is in model
  private def warehouseCheck(warehouseList: List[Warehouse]): Option[List[Warehouse]] = {
    if (warehouseList.length > 0) Some(warehouseList) else None
  }

}
