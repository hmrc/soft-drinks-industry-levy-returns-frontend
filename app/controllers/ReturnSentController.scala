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

import scala.concurrent.{Await, ExecutionContext, Future}
import java.util.Locale
import scala.concurrent.duration.DurationInt
import connectors.SoftDrinksIndustryLevyConnector
import controllers.actions._
import models.{Address, FinancialLineItem, ReturnPeriod, SmallProducer, Warehouse}
import pages.{BrandsPackagedAtOwnSitesPage, BroughtIntoUKPage, BroughtIntoUkFromSmallProducersPage, ClaimCreditsForExportsPage, ClaimCreditsForLostDamagedPage, ExemptionsForSmallProducersPage, HowManyCreditsForLostDamagedPage, OwnBrandsPage, PackagedContractPackerPage}
import viewmodels.govuk.summarylist._

import scala.math.BigDecimal
import javax.inject.Inject
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.ReturnSentView

import java.time.format.DateTimeFormatter
import config.FrontendAppConfig
import play.api.Configuration

import java.time.{LocalTime, ZoneId}
import viewmodels.checkAnswers.{AmountToPaySummary, BrandsPackagedAtOwnSitesSummary, BroughtIntoUKSummary, BroughtIntoUkFromSmallProducersSummary, ClaimCreditsForExportsSummary, ClaimCreditsForLostDamagedSummary, ExemptionsForSmallProducersSummary, HowManyAsAContractPackerSummary, HowManyBroughtIntoTheUKFromSmallProducersSummary, HowManyBroughtIntoUkSummary, HowManyCreditsForExportSummary, HowManyCreditsForLostDamagedSummary, OwnBrandsSummary, PackagedContractPackerSummary, SecondaryWarehouseDetailsSummary, SmallProducerDetailsSummary}


class ReturnSentController @Inject()(
                                       override val messagesApi: MessagesApi,
                                       config:FrontendAppConfig,
                                       configuration: Configuration,
                                       identify: IdentifierAction,
                                       getData: DataRetrievalAction,
                                       requireData: DataRequiredAction,
                                       connector: SoftDrinksIndustryLevyConnector,
                                       val controllerComponents: MessagesControllerComponents,
                                       view: ReturnSentView
                                     )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  def onPageLoad: Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      val subscription = Await.result(connector.retrieveSubscription(request.userAnswers.id,"sdil"),4.seconds).get

      val userAnswers = request.userAnswers

      val ownBrandsAnswer =
        if(userAnswers.get(OwnBrandsPage).getOrElse(false)) {
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

      val packagedContractPackerAnswers =
        if(userAnswers.get(PackagedContractPackerPage).getOrElse(false)){
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

      val exemptionsForSmallProducersAnswers =
        if(userAnswers.get(ExemptionsForSmallProducersPage).getOrElse(false)){
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

      val broughtIntoUkAnswers =
        if(userAnswers.get(BroughtIntoUKPage).getOrElse(false)){
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

      val broughtIntoUkSmallProducerAnswers =
        if(userAnswers.get(BroughtIntoUkFromSmallProducersPage).getOrElse(false)){
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

      val claimCreditsForExportsAnswers =
        if(userAnswers.get(ClaimCreditsForExportsPage).getOrElse(false)){
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

      val claimCreditsForLostDamagedAnswers =
        if(userAnswers.get(ClaimCreditsForLostDamagedPage).getOrElse(false)){
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

      val smallProducerAnswers =
          SummaryListViewModel(rows = Seq(
            SmallProducerDetailsSummary.producerList(userAnswers)
          ).flatten)

      val warehouseAnswers =
        SummaryListViewModel(rows = Seq(
          SecondaryWarehouseDetailsSummary.warehouseList(userAnswers)
        ))
      
      val amountOwed:String = "£100,000.00"
      val paymentDate = ReturnPeriod(2022,1)
      val returnDate = ReturnPeriod(2022,1)
      LocalTime.now(ZoneId.of("Europe/London")).format(DateTimeFormatter.ofPattern("h:mma")).toLowerCase


      def financialStatus (total :BigDecimal):String = {
        total match {
          case total if total > 0 => "amountToPay"
          case total if total < 0 => "creditedPay"
          case total if total == 0 => "noPayNeeded"
        }
      }

      //Warehouse TODO -> REMOVE WHEN WAREHOUSE LIST IS MADE!
      val tradingName:String = "Soft Juice Ltd"
      val line1: String = "3 Prospect St"
      val line2: String = "Reading"
      val line3: String = "Berkshire"
      val line4: String = "United Kingdom"
      val postcode: String = "CT44 0DF"
      val warhouseList:List[Warehouse] = List(Warehouse(tradingName,Address(line1, line2, line3, line4, postcode)))

            def smallProducerCheck(smallProducerList:List[SmallProducer]): Option[List[SmallProducer]] = {
              if(smallProducerList.length > 0){
                Some(smallProducerList)
              }else None
            }

            def warehouseCheck(warehouseList:List[Warehouse]): Option[List[Warehouse]] = {
              if(warehouseList.length > 0){
                Some(warehouseList)
              }else None
            }

      //Quarter

            def checkSmallProducerStatus(sdilRef: String, period: ReturnPeriod): Future[Option[Boolean]] = {
              connector.checkSmallProducerStatus(sdilRef, period)
            }

            val smallProducerStatus:Boolean = !Await.result(checkSmallProducerStatus(request.sdilEnrolment,request.returnPeriod.get),20.seconds).getOrElse(true)

            val totalThisQuarterAnswer =
              SummaryListViewModel(rows = Seq(
                AmountToPaySummary.totalThisQuarter(userAnswers, config.lowerBandCostPerLitre, config.higherBandCostPerLitre, smallProducerStatus)))

      //Balance Brought Forward

            def listItemsWithTotal(items: List[FinancialLineItem]): List[(FinancialLineItem, BigDecimal)] =
              items.distinct.foldLeft(List.empty[(FinancialLineItem, BigDecimal)]) { (acc, n) =>
                (n, acc.headOption.fold(n.amount)(_._2 + n.amount)) :: acc
              }

            def extractTotal(l: List[(FinancialLineItem, BigDecimal)]): BigDecimal =
              l.headOption.fold(BigDecimal(0))(_._2)

            val broughtForward = if(configuration.underlying.getBoolean("balanceAllEnabled")) {
              connector.balanceHistory(request.sdilEnrolment, withAssessment = false).map { x =>
                extractTotal(listItemsWithTotal(x))
              }
            }else {
                connector.balance(request.sdilEnrolment, withAssessment = false)
            }
            val balanceBroughtForward = Await.result(broughtForward ,20.seconds)

      val balanceBroughtForwardAnswer =
        SummaryListViewModel(rows = Seq(
          AmountToPaySummary.balanceBroughtForward(balanceBroughtForward)))

      val totalAnswer =
        SummaryListViewModel(rows = Seq(
          AmountToPaySummary.total(userAnswers, config.lowerBandCostPerLitre, config.higherBandCostPerLitre, smallProducerStatus,balanceBroughtForward)))

      val balance = AmountToPaySummary.balance(userAnswers, config.lowerBandCostPerLitre, config.higherBandCostPerLitre, smallProducerStatus, balanceBroughtForward)


      Ok(view(returnDate,
              subscription,
              amountOwed,
              balance,
              paymentDate,
              financialStatus = financialStatus(balance): String,
              ownBrandsAnswer,
              packagedContractPackerAnswers,
              exemptionsForSmallProducersAnswers,
              broughtIntoUkAnswers,
              broughtIntoUkSmallProducerAnswers,
              claimCreditsForExportsAnswers,
              claimCreditsForLostDamagedAnswers,
              smallProducerCheck = smallProducerCheck(request.userAnswers.smallProducerList):Option[List[SmallProducer]],
              warehouseCheck = warehouseCheck(warhouseList):Option[List[Warehouse]], //TODO CHANGE TO CHECK WAREHOUSE LIST!
              smallProducerAnswers,
              warehouseAnswers,
              totalThisQuarterAnswer,
              balanceBroughtForwardAnswer,
              totalAnswer
              ))
  }
}
