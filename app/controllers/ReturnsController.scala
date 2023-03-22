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

package controllers

import config.FrontendAppConfig
import connectors.SoftDrinksIndustryLevyConnector
import controllers.actions._
import models.requests.DataRequest
import models.retrieved.RetrievedSubscription
import models.{Address, Amounts, FinancialLineItem, ReturnPeriod, SdilReturn, SmallProducer, UserAnswers, Warehouse}
import pages._
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import play.api.{Configuration, Logger}
import repositories.{SDILSessionCache, SDILSessionKeys}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utilitlies.{CurrencyFormatter, GenericError, ReturnsHelper}
import viewmodels.checkAnswers._
import viewmodels.govuk.summarylist._
import views.html.ReturnSentView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}


class ReturnsController @Inject()(
                                   override val messagesApi: MessagesApi,
                                   config:FrontendAppConfig,
                                   configuration: Configuration,
                                   identify: IdentifierAction,
                                   getData: DataRetrievalAction,
                                   requireData: DataRequiredAction,
                                   sdilConnector: SoftDrinksIndustryLevyConnector,
                                   val controllerComponents: MessagesControllerComponents,
                                   view: ReturnSentView,
                                   sessionCache: SDILSessionCache,
                                     )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  //Warehouse TODO -> REMOVE WHEN WAREHOUSE LIST IS MADE!
  val tradingName: String = "Soft Juice Ltd"
  val line1: String = "3 Prospect St"
  val line2: String = "Reading"
  val line3: String = "Berkshire"
  val line4: String = "United Kingdom"
  val postcode: String = "CT44 0DF"
  val warehouseList: List[Warehouse] = List(Warehouse(tradingName, Address(line1, line2, line3, line4, postcode)))
  val logger: Logger = Logger(this.getClass())

  def onPageLoad(nilReturn: Boolean): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>

      implicit val format = Json.format[Amounts]
      val sdilEnrolment = request.sdilEnrolment
      val subscription = request.subscription
      val isSmallProducer = subscription.activity.smallProducer
      // TODO - globalise maybe?
      val userAnswers = request.userAnswers

      // TODO - double check if payment due date is the end of the current return period being submitted
      val returnPeriod = request.returnPeriod.getOrElse(GenericError.throwException("No return period found"))

      for {
        session <- sessionCache.fetchEntry(sdilEnrolment,SDILSessionKeys.AMOUNTS)
        pendingReturns <- sdilConnector.returns_pending(subscription.utr)
      } yield {
        session match {
          case Some(amounts) => {

            val returnToBeSubmitted =
              if(nilReturn) {
                ReturnsHelper.emptyReturn
              } else {
                SdilReturn(
                  ownBrandsLitres(subscription, userAnswers),
                  packLargeLitres(userAnswers),
                  userAnswers.smallProducerList,
                  importsLitres(userAnswers),
                  importsSmallLitres(userAnswers),
                  exportLitres(userAnswers),
                  wastageLitres(userAnswers))
              }

            if (pendingReturns.contains(returnPeriod)) {
              sdilConnector.returns_update(subscription.utr, returnPeriod, returnToBeSubmitted).onComplete {
                case Success(_) =>
                  logger.info(s"Return submitted for $sdilEnrolment year ${returnPeriod.year} quarter ${returnPeriod.quarter}")
                case Failure(e) =>
                  logger.error(s"Failed to submit return for $sdilEnrolment year ${returnPeriod.year} quarter ${returnPeriod.quarter}")
                  throw new RuntimeException(s"Failed to submit return $sdilEnrolment year ${returnPeriod.year} quarter ${returnPeriod.quarter} - error ${e.getMessage}" )
              }
            } else {
              logger.error(s"Pending returns for $sdilEnrolment don't contain the return for year ${returnPeriod.year} quarter ${returnPeriod.quarter}")
              Redirect(routes.JourneyRecoveryController.onPageLoad())
            }

            Ok(view(returnPeriod,
              request.subscription,
              CurrencyFormatter.formatAmountOfMoneyWithPoundSign(amounts.total),
              amounts.totalForQuarter,
              returnPeriod, // TODO - I don't think this needs to be passed twice
              financialStatus = financialStatus(amounts.total): String,
              ownBrandsAnswers(userAnswers),
              packagedContractPackerAnswers(request, userAnswers),
              exemptionForSmallProducersAnswers(userAnswers),
              broughtIntoUKAnswers(userAnswers),
              broughtIntoUKFromSmallProducerAnswers(userAnswers),
              claimCreditsForExportsAnswers(userAnswers),
              claimCreditsForLostOrDamagedAnswers(userAnswers),
              smallProducerCheck = smallProducerCheck(request.userAnswers.smallProducerList): Option[List[SmallProducer]],
              warehouseCheck = warehouseCheck(warehouseList): Option[List[Warehouse]], //TODO CHANGE TO CHECK WAREHOUSE LIST!
              smallProducerAnswers(userAnswers),
              warehouseAnswers(userAnswers),
              AmountToPaySummary.amountToPaySummary(amounts.totalForQuarter, amounts.balanceBroughtForward, amounts.total)
            ))

          }
          case _ =>
            logger.error(s"No amount found in the cache for $sdilEnrolment year ${returnPeriod.year} quarter ${returnPeriod.quarter}")
            Redirect(routes.JourneyRecoveryController.onPageLoad())
        }
      }
  }

  private def ownBrandsLitres(subscription: RetrievedSubscription, userAnswers: UserAnswers) = {
    if (!subscription.activity.smallProducer) {
      (userAnswers.get(BrandsPackagedAtOwnSitesPage).map(_.lowBand).getOrElse(0L),
        userAnswers.get(BrandsPackagedAtOwnSitesPage).map(_.highBand).getOrElse(0L))
    } else (0L, 0L)
  }

  private def packLargeLitres(userAnswers: UserAnswers) = {
    (userAnswers.get(HowManyAsAContractPackerPage).map(_.lowBand).getOrElse(0L),
      userAnswers.get(HowManyAsAContractPackerPage).map(_.highBand).getOrElse(0L))
  }

  private def importsLitres(userAnswers: UserAnswers) = {
    (userAnswers.get(HowManyBroughtIntoUkPage).map(_.lowBand).getOrElse(0L),
      userAnswers.get(HowManyBroughtIntoUkPage).map(_.highBand).getOrElse(0L))
  }

  private def importsSmallLitres(userAnswers: UserAnswers) = {
    (userAnswers.smallProducerList.map(smallProducer => smallProducer.litreage._1).sum,
      userAnswers.smallProducerList.map(smallProducer => smallProducer.litreage._2).sum)
  }

  private def exportLitres(userAnswers: UserAnswers) = {
    (userAnswers.get(HowManyCreditsForExportPage).map(_.lowBand).getOrElse(0L),
      userAnswers.get(HowManyCreditsForExportPage).map(_.highBand).getOrElse(0L))
  }

  private def wastageLitres(userAnswers: UserAnswers) = {
    (userAnswers.get(HowManyCreditsForLostDamagedPage).map(_.lowBand).getOrElse(0L),
      userAnswers.get(HowManyCreditsForLostDamagedPage).map(_.highBand).getOrElse(0L))
  }


//  TODO - refactor function to work based on page type as opposed to function per type
//  private def pageLitresInfo(userAnswers: UserAnswers, page: QuestionPage[_]): Unit = {
//    (
//      userAnswers.get(HowManyCreditsForLostDamagedPage).map(_.lowBand).getOrElse(0L),
//      userAnswers.get(HowManyCreditsForLostDamagedPage).map(_.highBand).getOrElse(0L)
//    )
//  }

  private def warehouseAnswers(userAnswers: UserAnswers)(implicit messages: Messages) = {
    SummaryListViewModel(rows = Seq(SecondaryWarehouseDetailsSummary.warehouseList(userAnswers)))
  }

  private def smallProducerAnswers(userAnswers: UserAnswers)(implicit messages: Messages) = {
    SummaryListViewModel(rows = Seq(SmallProducerDetailsSummary.producerList(userAnswers)).flatten)
  }

  private def claimCreditsForLostOrDamagedAnswers(userAnswers: UserAnswers)(implicit messages: Messages) = {
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

  private def claimCreditsForExportsAnswers(userAnswers: UserAnswers)(implicit messages: Messages) = {
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

  private def broughtIntoUKFromSmallProducerAnswers(userAnswers: UserAnswers)(implicit messages: Messages) = {
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

  private def broughtIntoUKAnswers(userAnswers: UserAnswers)(implicit messages: Messages) = {
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

  private def exemptionForSmallProducersAnswers(userAnswers: UserAnswers)(implicit messages: Messages) = {
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

  private def packagedContractPackerAnswers(request: DataRequest[AnyContent], userAnswers: UserAnswers)(implicit messages: Messages) = {
    if (userAnswers.get(PackagedContractPackerPage).getOrElse(false)) {
      SummaryListViewModel(rows = Seq(
        PackagedContractPackerSummary.returnsRow(userAnswers),
        HowManyAsAContractPackerSummary.returnsLowBandRow(userAnswers),
        HowManyAsAContractPackerSummary.returnsLowBandLevyRow(userAnswers, config.lowerBandCostPerLitre),
        HowManyAsAContractPackerSummary.returnsHighBandRow(userAnswers),
        HowManyAsAContractPackerSummary.returnsHighBandLevyRow(userAnswers, config.higherBandCostPerLitre)
      ).flatten)
    } else {
      SummaryListViewModel(rows = Seq(PackagedContractPackerSummary.returnsRow(request.userAnswers)).flatten)
    }
  }

  private def ownBrandsAnswers(userAnswers: UserAnswers)(implicit messages: Messages) = {
    if (userAnswers.get(OwnBrandsPage).getOrElse(false)) {
      SummaryListViewModel(rows = Seq(
        OwnBrandsSummary.returnsRow(userAnswers),
        BrandsPackagedAtOwnSitesSummary.returnsLowBandRow(userAnswers),
        BrandsPackagedAtOwnSitesSummary.returnsLowBandLevyRow(userAnswers, config.lowerBandCostPerLitre),
        BrandsPackagedAtOwnSitesSummary.returnsHighBandRow(userAnswers),
        BrandsPackagedAtOwnSitesSummary.returnsHighBandLevyRow(userAnswers, config.higherBandCostPerLitre)
      ).flatten)
    } else {
      SummaryListViewModel(rows = Seq(OwnBrandsSummary.returnsRow(userAnswers)).flatten)
    }
  }

  private def financialStatus(total: BigDecimal): String = {
    total match {
      case total if total > 0 => "amountToPay"
      case total if total < 0 => "creditedPay"
      case total if total == 0 => "noPayNeeded"
    }
  }

  private def smallProducerCheck(smallProducerList: List[SmallProducer]): Option[List[SmallProducer]] = {
    if (smallProducerList.length > 0) Some(smallProducerList) else None
  }

  private def warehouseCheck(warehouseList: List[Warehouse]): Option[List[Warehouse]] = {
    if (warehouseList.length > 0) Some(warehouseList) else None
  }

  private def listItemsWithTotal(items: List[FinancialLineItem]): List[(FinancialLineItem, BigDecimal)] =
    items.distinct.foldLeft(List.empty[(FinancialLineItem, BigDecimal)]) { (acc, n) =>
      (n, acc.headOption.fold(n.amount)(_._2 + n.amount)) :: acc
    }

  private def extractTotal(l: List[(FinancialLineItem, BigDecimal)]): BigDecimal = l.headOption.fold(BigDecimal(0))(_._2)

}
