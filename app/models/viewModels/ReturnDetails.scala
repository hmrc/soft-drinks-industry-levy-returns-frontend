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
  def this(userAnswers: UserAnswers, isCheckAnswers: Boolean = false)(implicit request: DataRequest[AnyContent], messages: Messages, config: FrontendAppConfig) = this(
      ownBrandsAnswer = OwnBrandsSummary.summaryList(userAnswers, isCheckAnswers),
      packagedContractPackerAnswers = ReturnDetails.packagedContractPackerAnswers(request, userAnswers),
      exemptionsForSmallProducersAnswers = ReturnDetails.exemptionForSmallProducersAnswers(userAnswers),
      broughtIntoUkAnswers = ReturnDetails.broughtIntoUKAnswers(userAnswers),
      broughtIntoUkSmallProducerAnswers =,
      claimCreditsForExportsAnswers =,
      claimCreditsForLostDamagedAnswers =,
      smallProducerCheck = ,
      warehouseCheck = ,
      smallProducerAnswers =,
      warehouseAnswers =,
      amountsRow =
    )
}

object ReturnDetails {

  def broughtIntoUKFromSmallProducerAnswers(userAnswers: UserAnswers)(implicit messages: Messages) = {
    if (userAnswers.get(BroughtIntoUkFromSmallProducersPage).getOrElse(false)) {
      SummaryListViewModel(rows = Seq(
        BroughtIntoUkFromSmallProducersSummary.returnsRow(userAnswers),
        HowManyBroughtIntoTheUKFromSmallProducersSummary.returnsLowBandRow(userAnswers),
        HowManyBroughtIntoTheUKFromSmallProducersSummary.returnsLowBandLevyRow(userAnswers, config.lowerBandCostPerLitre),
        HowManyBroughtIntoTheUKFromSmallProducersSummary.returnsHighBandRow(userAnswers),
        HowManyBroughtIntoTheUKFromSmallProducersSummary.returnsHighBandLevyRow(userAnswers, config.higherBandCostPerLitre)
      ).flatten)
    } else {
      SummaryListViewModel(rows = Seq(BroughtIntoUkFromSmallProducersSummary.returnsRow(userAnswers)).flatten)
    }
  }

  def broughtIntoUKAnswers(userAnswers: UserAnswers)(implicit messages: Messages) = {
    if (userAnswers.get(BroughtIntoUKPage).getOrElse(false)) {
      SummaryListViewModel(rows = Seq(
        BroughtIntoUKSummary.returnsRow(userAnswers),
        HowManyBroughtIntoUkSummary.returnsLowBandRow(userAnswers),
        HowManyBroughtIntoUkSummary.returnsLowBandLevyRow(userAnswers, config.lowerBandCostPerLitre),
        HowManyBroughtIntoUkSummary.returnsHighBandRow(userAnswers),
        HowManyBroughtIntoUkSummary.returnsHighBandLevyRow(userAnswers, config.higherBandCostPerLitre)
      ).flatten)
    } else {
      SummaryListViewModel(rows = Seq(BroughtIntoUKSummary.returnsRow(userAnswers)).flatten)
    }
  }

  def exemptionForSmallProducersAnswers(userAnswers: UserAnswers)(implicit messages: Messages) = {
    if (userAnswers.get(ExemptionsForSmallProducersPage).getOrElse(false)) {
      SummaryListViewModel(rows = Seq(
        ExemptionsForSmallProducersSummary.returnsRow(userAnswers),
        SmallProducerDetailsSummary.returnsLowBandRow(userAnswers),
        SmallProducerDetailsSummary.returnsLowBandLevyRow(userAnswers, config.lowerBandCostPerLitre),
        SmallProducerDetailsSummary.returnsHighBandRow(userAnswers),
        SmallProducerDetailsSummary.returnsHighBandLevyRow(userAnswers, config.higherBandCostPerLitre)
      ).flatten)
    } else {
      SummaryListViewModel(rows = Seq(ExemptionsForSmallProducersSummary.returnsRow(userAnswers)).flatten)
    }
  }

  def packagedContractPackerAnswers(request: DataRequest[AnyContent], userAnswers: UserAnswers)(implicit messages: Messages) = {

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

}
