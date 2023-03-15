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
import models.{Address, Amounts, FinancialLineItem, ReturnPeriod, SmallProducer, UserAnswers, Warehouse}
import pages._
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import play.api.{Configuration, Logger}
import repositories.{SDILSessionCache, SDILSessionKeys}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utilitlies.Utilities
import viewmodels.checkAnswers._
import viewmodels.govuk.summarylist._
import views.html.ReturnSentView

import java.util.Locale
import javax.inject.Inject
import scala.concurrent.ExecutionContext


class ReturnsController @Inject()(
                                       override val messagesApi: MessagesApi,
                                       config:FrontendAppConfig,
                                       configuration: Configuration,
                                       identify: IdentifierAction,
                                       getData: DataRetrievalAction,
                                       requireData: DataRequiredAction,
                                       connector: SoftDrinksIndustryLevyConnector,
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
  val warhouseList: List[Warehouse] = List(Warehouse(tradingName, Address(line1, line2, line3, line4, postcode)))
  val logger: Logger = Logger(this.getClass())

  def onPageLoad: Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>

      val sdilEnrolment = request.sdilEnrolment
      val subscription = request.subscription
      val isSmallProducer = subscription.activity.smallProducer
      val userAnswers = request.userAnswers
      val paymentDueDate = currentReturnPeriod(request)
      val returnDate = ReturnPeriod(2022, 1) // Is this returns submitted date?
      val amountOwed: String = "£100,000.00"
      implicit val format = Json.format[Amounts]

      sessionCache.fetchEntry(sdilEnrolment,SDILSessionKeys.AMOUNTS).map {
        case Some(amounts) => {
          val totalForQuarter = amounts.totalForQuarter
          val balanceBroughtForward = amounts.balanceBroughtForward
          val total = amounts.total

          // TODO - these needs re-checking by Jake
          val balanceBroughtForwardAnswer = SummaryListViewModel(rows = Seq(AmountToPaySummary.balanceBroughtForward(balanceBroughtForward)))
          val totalAnswer = SummaryListViewModel(rows = Seq(AmountToPaySummary.total(userAnswers, config.lowerBandCostPerLitre, config.higherBandCostPerLitre, isSmallProducer, balanceBroughtForward)))
          val balance = AmountToPaySummary.balance(userAnswers, config.lowerBandCostPerLitre, config.higherBandCostPerLitre, isSmallProducer, balanceBroughtForward)

          println(Console.YELLOW + "balance/quarter " + totalForQuarter + Console.WHITE)
          println(Console.YELLOW + "forward " + balanceBroughtForward + Console.WHITE)
          println(Console.YELLOW + "total " + total + Console.WHITE)

          println(Console.YELLOW + "balance/quarter " + balance + Console.WHITE)
          println(Console.YELLOW + "forward " + balanceBroughtForward + Console.WHITE)
          println(Console.YELLOW + "total " + totalAnswer + Console.WHITE)

          Ok(view(returnDate,
            request.subscription,
            Utilities.formatAmountOfMoneyWithPoundSign(total),
            balance,
            paymentDueDate,
            financialStatus = financialStatus(total): String,
            ownBrandsAnswers(userAnswers),
            packagedContractPackerAnswers(request, userAnswers),
            exemptionForSmallProducersAnswers(userAnswers),
            broughtIntoUKAnswers(userAnswers),
            broughtIntoUKFromSmallProducerAnswers(userAnswers),
            claimCreditsForExportsAnswers(userAnswers),
            claimCreditsForLostOrDamagedAnswers(userAnswers),
            smallProducerCheck = smallProducerCheck(request.userAnswers.smallProducerList): Option[List[SmallProducer]],
            warehouseCheck = warehouseCheck(warhouseList): Option[List[Warehouse]], //TODO CHANGE TO CHECK WAREHOUSE LIST!
            smallProducerAnswers(userAnswers),
            warehouseAnswers(userAnswers),
            totalForQuarterSummary(isSmallProducer, userAnswers),
            balanceBroughtForwardAnswer,
            totalAnswer
          ))
        }
        case _ =>
          logger.error("no amount found in the cache")
          Redirect(routes.JourneyRecoveryController.onPageLoad())
      }
  }

  private def totalForQuarterSummary(isSmallProducer: Boolean, userAnswers: UserAnswers)(implicit messages: Messages) = {
    SummaryListViewModel(
      rows = Seq(AmountToPaySummary.totalThisQuarter(
        userAnswers,
        config.lowerBandCostPerLitre,
        config.higherBandCostPerLitre,
        isSmallProducer)))
  }

  private def warehouseAnswers(userAnswers: UserAnswers)(implicit messages: Messages) = {
    SummaryListViewModel(rows = Seq(
      SecondaryWarehouseDetailsSummary.warehouseList(userAnswers)
    ))
  }

  private def smallProducerAnswers(userAnswers: UserAnswers)(implicit messages: Messages) = {
    SummaryListViewModel(rows = Seq(
      SmallProducerDetailsSummary.producerList(userAnswers)
    ).flatten)
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

  private def currentReturnPeriod(request: DataRequest[AnyContent]) = {
    request.returnPeriod match {
      case Some(returnPeriod) => returnPeriod
      case None => throw new RuntimeException("No return period returned")
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
    if (smallProducerList.length > 0) {
      Some(smallProducerList)
    } else None
  }

  private def warehouseCheck(warehouseList: List[Warehouse]): Option[List[Warehouse]] = {
    if (warehouseList.length > 0) {
      Some(warehouseList)
    } else None
  }

  private def listItemsWithTotal(items: List[FinancialLineItem]): List[(FinancialLineItem, BigDecimal)] =
    items.distinct.foldLeft(List.empty[(FinancialLineItem, BigDecimal)]) { (acc, n) =>
      (n, acc.headOption.fold(n.amount)(_._2 + n.amount)) :: acc
    }

  private def extractTotal(l: List[(FinancialLineItem, BigDecimal)]): BigDecimal = l.headOption.fold(BigDecimal(0))(_._2)

}
