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

import controllers.actions._
import connectors.SoftDrinksIndustryLevyConnector
import models.{Address, FinancialLineItem, Mode, ReturnPeriod, SmallProducer, Warehouse}
import pages._
import viewmodels.govuk.summarylist._
import views.html.CheckYourAnswersView
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import play.api.Configuration
import scala.math.BigDecimal

import com.google.inject.Inject
import scala.concurrent.{Await, ExecutionContext, Future}
import java.util.Locale
import scala.concurrent.duration.DurationInt


class CheckYourAnswersController @Inject()(
                                            override val messagesApi: MessagesApi,
                                            config: Configuration,
                                            identify: IdentifierAction,
                                            getData: DataRetrievalAction,
                                            requireData: DataRequiredAction,
                                            val controllerComponents: MessagesControllerComponents,
                                            sdilConnector: SoftDrinksIndustryLevyConnector,
                                            view: CheckYourAnswersView
                                          )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>

      val list = SummaryListViewModel(
        rows = Seq.empty
      )

      val alias: String = Await.result(sdilConnector.retrieveSubscription(request.sdilEnrolment,"sdil").map(
        subscription => subscription.get.orgName), 10.seconds)
      println(Console.YELLOW + {request} + Console.WHITE)
      val returnDate: String = request.returnPeriod.get.quarter match {
        case 0 => "January to March " + request.returnPeriod.get.year
        case 1 => "April to June " + request.returnPeriod.get.year
        case 2 => "July to September " + request.returnPeriod.get.year
        case 3 => "October to December " + request.returnPeriod.get.year
      }
//
//      //Warehouse
//      val tradingName:String = "Soft Juice Ltd"
//      val line1: String = "3 Prospect St"
//      val line2: String = "Reading"
//      val line3: String = "Berkshire"
//      val line4: String = "United Kingdom"
//      val postcode: String = "CT44 0DF"
//      val warhouseList:List[Warehouse] = List(Warehouse(tradingName,Address(line1, line2, line3, line4, postcode)))
//
////================================================================TODO Calculation START
//      val currencyFormatter =
//        java.text.NumberFormat.getCurrencyInstance(Locale.UK)
//
//      val costLower = BigDecimal(config.underlying.getString("band-costs.lowBand"))
//      val costHigher = BigDecimal(config.underlying.getString("band-costs.highBand"))
//
//      def checkSmallProducerStatus(sdilRef: String, period: ReturnPeriod): Future[Option[Boolean]] = {
//        sdilConnector.checkSmallProducerStatus(sdilRef, period)
//      }
//
//      def formatAmountOfMoneyWithPoundSign(d: BigDecimal): String = {
//        currencyFormatter.format(d)
//      }
//
//      // Page 2 HowManyAsAContractPacker
//      val howManyAsAContractPackerAnswers = request.userAnswers.get(HowManyAsAContractPackerPage)
//      val howManyAsAContractPackerLowBand = if(request.userAnswers.get(PackagedContractPackerPage) == Some(true)){
//        howManyAsAContractPackerAnswers.map(answer => answer.lowBand).getOrElse(0L)}else 0L
//      val howManyAsAContractPackerLowBandCost = costLower * howManyAsAContractPackerLowBand * 1
//      val howManyAsAContractPackerHighBand = if(request.userAnswers.get(PackagedContractPackerPage) == Some(true)){
//        howManyAsAContractPackerAnswers.map(answer => answer.highBand).getOrElse(0L)}else 0L
//      val howManyAsAContractPackerHighBandCost = costHigher * howManyAsAContractPackerHighBand * 1
//      val howManyAsAContractPackerTotal = howManyAsAContractPackerLowBandCost + howManyAsAContractPackerHighBandCost
//
//
//      val smallProducerDetailsAnswers = request.userAnswers.smallProducerList
//      val smallProducerDetailsLowBand = smallProducerDetailsAnswers.map(answer => answer.litreage._1).sum
//      val smallProducerDetailsLowBandCost = smallProducerDetailsAnswers.map(answer => answer.litreage._1 * costLower * 0).sum
//      val smallProducerDetailsHighBand = smallProducerDetailsAnswers.map(answer => answer.litreage._2).sum
//      val smallProducerDetailsHighBandCost = smallProducerDetailsAnswers.map(answer => answer.litreage._2 * costLower * 0).sum
//      val smallProducerDetailsTotal = smallProducerDetailsLowBandCost + smallProducerDetailsHighBandCost
//
//      // Page 3 add-small-producer
//      val howManyBroughtIntoUkAnswers = request.userAnswers.get(HowManyBroughtIntoUkPage)
//      val howManyBroughtIntoUkLowBand = if(request.userAnswers.get(BroughtIntoUKPage) == Some(true)){
//        howManyBroughtIntoUkAnswers.map(answer => answer.lowBand).getOrElse(0L)} else 0L
//      val howManyBroughtIntoUkLowBandCost = (costLower * howManyBroughtIntoUkLowBand * 1)
//      val howManyBroughtIntoUkHighBand = if(request.userAnswers.get(BroughtIntoUKPage) == Some(true)){
//        howManyBroughtIntoUkAnswers.map(answer => answer.highBand).getOrElse(0L)}else 0L
//      val howManyBroughtIntoUkHighBandCost = (costHigher * howManyBroughtIntoUkHighBand * 1)
//      val howManyBroughtIntoUkTotal = howManyBroughtIntoUkLowBandCost + howManyBroughtIntoUkHighBandCost
//
//      val howManyBroughtIntoTheUKFromSmallProducersAnswers = request.userAnswers.get(HowManyBroughtIntoTheUKFromSmallProducersPage)
//      val howManyBroughtIntoTheUKFromSmallProducersLowBand = if(request.userAnswers.get(BroughtIntoUkFromSmallProducersPage) == Some(true)){
//        howManyBroughtIntoTheUKFromSmallProducersAnswers.map(answer => answer.lowBand).getOrElse(0L)}else 0L
//      val howManyBroughtIntoTheUKFromSmallProducersLowBandCost = (costLower * howManyBroughtIntoTheUKFromSmallProducersLowBand * 1)
//      val howManyBroughtIntoTheUKFromSmallProducersHighBand = if(request.userAnswers.get(BroughtIntoUkFromSmallProducersPage) == Some(true)){
//        howManyBroughtIntoTheUKFromSmallProducersAnswers.map(answer => answer.highBand).getOrElse(0L)}else 0L
//      val howManyBroughtIntoTheUKFromSmallProducersHighBandCost = (costHigher * howManyBroughtIntoTheUKFromSmallProducersHighBand * 1)
//      val howManyBroughtIntoTheUKFromSmallProducersTotal = howManyBroughtIntoTheUKFromSmallProducersLowBandCost + howManyBroughtIntoTheUKFromSmallProducersHighBandCost
//
//      val howManyCreditsForExportAnswers = request.userAnswers.get(HowManyCreditsForExportPage)
//      val howManyCreditsForExportLowBand = if(request.userAnswers.get(ClaimCreditsForExportsPage) == Some(true)){
//        howManyCreditsForExportAnswers.map(answer => answer.lowBand).getOrElse(0L)}else 0L
//      val howManyCreditsForExportLowBandCost = (costLower * howManyCreditsForExportLowBand * -1)
//      val howManyCreditsForExportHighBand = if(request.userAnswers.get(ClaimCreditsForExportsPage) == Some(true)){
//        howManyCreditsForExportAnswers.map(answer => answer.highBand).getOrElse(0L)}else 0L
//      val howManyCreditsForExportHighBandCost = (costLower * howManyCreditsForExportHighBand * -1)
//      val howManyCreditsForExportTotal = howManyCreditsForExportLowBandCost + costHigher * howManyCreditsForExportHighBand * -1
//
//      val howManyCreditsForLostDamagedAnswers = request.userAnswers.get(HowManyCreditsForLostDamagedPage)
//      val howManyCreditsForLostDamagedLowBand = if(request.userAnswers.get(ClaimCreditsForLostDamagedPage) == Some(true)){
//        howManyCreditsForLostDamagedAnswers.map(answer => answer.lowBand).getOrElse(0L)}else 0L
//      val howManyCreditsForLostDamagedLowBandCost = (costLower * howManyCreditsForLostDamagedLowBand * -1)
//      val howManyCreditsForLostDamagedHighBand = if(request.userAnswers.get(ClaimCreditsForLostDamagedPage) == Some(true)){
//        howManyCreditsForLostDamagedAnswers.map(answer => answer.highBand).getOrElse(0L)}else 0L
//      val howManyCreditsForLostDamagedTotal = costLower * howManyCreditsForLostDamagedLowBand * -1 + costHigher * howManyCreditsForLostDamagedHighBand * -1
//
//      val lowBandAnswerList:List[Long]= List(
//        howManyAsAContractPackerLowBand,
//        smallProducerDetailsLowBand,
//        howManyBroughtIntoUkLowBand,
//        howManyBroughtIntoTheUKFromSmallProducersLowBand,
//        howManyCreditsForExportLowBand,
//        howManyCreditsForLostDamagedLowBand)
//
//      val highBandAnswerList:List[Long] = List(
//        howManyAsAContractPackerHighBand,
//        smallProducerDetailsHighBand,
//        howManyBroughtIntoUkHighBand,
//        howManyBroughtIntoTheUKFromSmallProducersHighBand,
//        howManyCreditsForExportHighBand,
//        howManyCreditsForLostDamagedHighBand)
//
//      val lowBandAnswerListCost:List[String] = List(
//        howManyAsAContractPackerLowBandCost,
//        smallProducerDetailsLowBandCost,
//        howManyBroughtIntoUkLowBandCost,
//        howManyBroughtIntoTheUKFromSmallProducersLowBandCost,
//        howManyCreditsForExportLowBandCost,
//        howManyCreditsForLostDamagedLowBandCost).map(answers => formatAmountOfMoneyWithPoundSign(answers))
//
//      val highBandAnswerListCost:List[String] = List(
//        howManyAsAContractPackerHighBandCost,
//        smallProducerDetailsHighBandCost,
//        howManyBroughtIntoUkHighBandCost,
//        howManyBroughtIntoTheUKFromSmallProducersHighBandCost,
//        howManyCreditsForExportHighBandCost,
//        howManyCreditsForLostDamagedLowBandCost).map(answers => formatAmountOfMoneyWithPoundSign(answers))
//
//      val smallProducerAnswerListTotal:List[BigDecimal] = List(
//        howManyAsAContractPackerTotal,
//        smallProducerDetailsTotal,
//        howManyBroughtIntoUkTotal,
//        howManyBroughtIntoTheUKFromSmallProducersTotal,
//        howManyCreditsForExportTotal,
//        howManyCreditsForLostDamagedTotal
//      )
//
//
//      def calculateSubtotal(
//                             costLower:BigDecimal,
//                             costHigher:BigDecimal,
//                             smallProducerAnswerListTotal:List[BigDecimal]
//                           ): BigDecimal = {
//        if(!Await.result(checkSmallProducerStatus(request.sdilEnrolment,request.returnPeriod.get),20.seconds).getOrElse(true)){
//            smallProducerAnswerListTotal.sum
//          }else{
//          val ownBranduserAnswers = request.userAnswers.get(BrandsPackagedAtOwnSitesPage)
//          val lowBand = ownBranduserAnswers.map(answer => answer.lowBand).getOrElse(0L)
//          val highBand = ownBranduserAnswers.map(answer => answer.highBand).getOrElse(0L)
//          val ownBrandTotal = costLower * lowBand * 1 + costHigher * highBand * 1
//          val largeProducerListTotal =   ownBrandTotal + smallProducerAnswerListTotal.sum
//          largeProducerListTotal
//          }
//        }
//
//      def listItemsWithTotal(items: List[FinancialLineItem]): List[(FinancialLineItem, BigDecimal)] =
//        items.distinct.foldLeft(List.empty[(FinancialLineItem, BigDecimal)]) { (acc, n) =>
//          (n, acc.headOption.fold(n.amount)(_._2 + n.amount)) :: acc
//        }
//
//      def extractTotal(l: List[(FinancialLineItem, BigDecimal)]): BigDecimal =
//        l.headOption.fold(BigDecimal(0))(_._2)
//
//      val broughtForward = if(config.underlying.getBoolean("balanceAllEnabled")) {
//         sdilConnector.balanceHistory(request.sdilEnrolment, withAssessment = false).map { x =>
//          extractTotal(listItemsWithTotal(x))
//        }
//      }else {
//          sdilConnector.balance(request.sdilEnrolment, withAssessment = false)
//      }
//
//
//      val total:BigDecimal = calculateSubtotal(costLower,
//                                               costHigher,
//                                               smallProducerAnswerListTotal
//                                              ) - Await.result(broughtForward,20.seconds)
//
//      def smallProducerCheck(smallProducerList:List[SmallProducer]): Option[List[SmallProducer]] = {
//        if(smallProducerList.length > 0){
//          Some(smallProducerList)
//        }else None
//      }
//
//      def warehouseCheck(warehouseList:List[Warehouse]): Option[List[Warehouse]] = {
//        if(warehouseList.length > 0){
//          Some(warehouseList)
//        }else None
//      }
//
//      def financialStatus (total:BigDecimal):String = {
//        total match {
//          case total if total > 0 => "amountToPay"
//          case total if total < 0 => "creditedPay"
//          case total if total == 0 => "noPayNeeded"
//        }
//      }

      Ok(view(
              mode = mode,
              list = list,
              alias = alias:String,
              returnDate = returnDate:String//,
//              quarter = formatAmountOfMoneyWithPoundSign(calculateSubtotal(
//                costLower,
//                costHigher,
//                smallProducerAnswerListTotal)):String,
//              balanceBroughtForward = formatAmountOfMoneyWithPoundSign(Await.result(broughtForward ,20.seconds)):String,
//              total = formatAmountOfMoneyWithPoundSign(total): String,
//              financialStatus = financialStatus(total): String,
//              smallProducerCheck = smallProducerCheck(request.userAnswers.smallProducerList):Option[List[SmallProducer]],
//              warehouseCheck = warehouseCheck(warhouseList):Option[List[Warehouse]],//TODO COLLECT WAREHOUSE LIST
//              lowBandAnswerList = lowBandAnswerList:List[Long],
//              highBandAnswerList = highBandAnswerList:List[Long],
//              lowBandAnswerListCost = lowBandAnswerListCost:List[String],
//              highBandAnswerListCost = highBandAnswerListCost:List[String]
              ))
  }
}
