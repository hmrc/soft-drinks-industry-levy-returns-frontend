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

import connectors.SoftDrinksIndustryLevyConnector
import controllers.actions._
import models.ReturnPeriod
import pages.{BroughtIntoUKPage, BroughtIntoUkFromSmallProducersPage, ClaimCreditsForExportsPage, ClaimCreditsForLostDamagedPage, ExemptionsForSmallProducersPage, OwnBrandsPage, PackagedContractPackerPage}
import viewmodels.govuk.summarylist._

import javax.inject.Inject
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.ReturnSentView

import java.time.format.DateTimeFormatter
import config.FrontendAppConfig

import java.time.{LocalTime, ZoneId}
import scala.concurrent.Await
import scala.concurrent.duration.DurationInt
import viewmodels.checkAnswers.{BrandsPackagedAtOwnSitesSummary, BroughtIntoUKSummary, BroughtIntoUkFromSmallProducersSummary, ClaimCreditsForExportsSummary, ClaimCreditsForLostDamagedSummary, ExemptionsForSmallProducersSummary, HowManyAsAContractPackerSummary, HowManyBroughtIntoTheUKFromSmallProducersSummary, HowManyBroughtIntoUkSummary, HowManyCreditsForExportSummary, HowManyCreditsForLostDamagedSummary, OwnBrandsSummary, PackagedContractPackerSummary, SmallProducerDetailsSummary}

class ReturnSentController @Inject()(
                                       override val messagesApi: MessagesApi,
                                       config:FrontendAppConfig,
                                       identify: IdentifierAction,
                                       getData: DataRetrievalAction,
                                       requireData: DataRequiredAction,
                                       connector: SoftDrinksIndustryLevyConnector,
                                       val controllerComponents: MessagesControllerComponents,
                                       view: ReturnSentView
                                     ) extends FrontendBaseController with I18nSupport {

  def onPageLoad: Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      val subscription = Await.result(connector.retrieveSubscription(request.userAnswers.id,"sdil"),4.seconds).get

      val userAnswers = request.userAnswers

      val ownBrandsAnswer = {
        if(userAnswers.get(OwnBrandsPage).getOrElse(false) == true){
        SummaryListViewModel(rows = Seq(
        OwnBrandsSummary.row(userAnswers, checkAnswers = false),
        BrandsPackagedAtOwnSitesSummary.lowBandRow(userAnswers, checkAnswers = false),
        BrandsPackagedAtOwnSitesSummary.lowBandLevyRow(userAnswers, config.lowerBandCostPerLitre),
        BrandsPackagedAtOwnSitesSummary.highBandRow(userAnswers, checkAnswers = false),
        BrandsPackagedAtOwnSitesSummary.highBandLevyRow(userAnswers, config.higherBandCostPerLitre)
      ).flatten)}else{SummaryListViewModel(rows = Seq(
          OwnBrandsSummary.row(userAnswers, checkAnswers = false)).flatten)}
      }

      val packagedContractPackerAnswers =
        if(userAnswers.get(PackagedContractPackerPage).getOrElse(false) == true){
        SummaryListViewModel(rows = Seq(
        PackagedContractPackerSummary.row(userAnswers, checkAnswers = false),
        HowManyAsAContractPackerSummary.lowBandRow(userAnswers, checkAnswers = false),
        HowManyAsAContractPackerSummary.lowBandLevyRow(userAnswers, config.lowerBandCostPerLitre),
        HowManyAsAContractPackerSummary.highBandRow(userAnswers, checkAnswers = false),
        HowManyAsAContractPackerSummary.highBandLevyRow(userAnswers, config.higherBandCostPerLitre)
      ).flatten)}else{ SummaryListViewModel(rows = Seq(
          PackagedContractPackerSummary.row(request.userAnswers, checkAnswers = false)).flatten)}

      val exemptionsForSmallProducersAnswers =
        if(userAnswers.get(ExemptionsForSmallProducersPage).getOrElse(false) == true){
          SummaryListViewModel(rows = Seq(
            ExemptionsForSmallProducersSummary.row(userAnswers, checkAnswers = false),
            SmallProducerDetailsSummary.lowBandRow(userAnswers, checkAnswers = false),
            SmallProducerDetailsSummary.lowBandLevyRow(userAnswers, config.lowerBandCostPerLitre),
            SmallProducerDetailsSummary.highBandRow(userAnswers, checkAnswers = false),
            SmallProducerDetailsSummary.highBandLevyRow(userAnswers, config.higherBandCostPerLitre)
          ).flatten)}else{ SummaryListViewModel(rows = Seq(
          ExemptionsForSmallProducersSummary.row(userAnswers, checkAnswers = false)).flatten)}

      val broughtIntoUkAnswers =
        if(userAnswers.get(BroughtIntoUKPage).getOrElse(false) == true){
          SummaryListViewModel(rows = Seq(
            BroughtIntoUKSummary.row(userAnswers, checkAnswers = false),
            HowManyBroughtIntoUkSummary.lowBandRow(userAnswers, checkAnswers = false),
            HowManyBroughtIntoUkSummary.lowBandLevyRow(userAnswers, config.lowerBandCostPerLitre),
            HowManyBroughtIntoUkSummary.highBandRow(userAnswers, checkAnswers = false),
            HowManyBroughtIntoUkSummary.highBandLevyRow(userAnswers, config.higherBandCostPerLitre)
          ).flatten)}else{ SummaryListViewModel(rows = Seq(
          BroughtIntoUKSummary.row(userAnswers, checkAnswers = false)).flatten)}

      val broughtIntoUkSmallProducerAnswers =
        if(userAnswers.get(BroughtIntoUkFromSmallProducersPage).getOrElse(false) == true){
          SummaryListViewModel(rows = Seq(
            BroughtIntoUkFromSmallProducersSummary.row(userAnswers, checkAnswers = false),
            HowManyBroughtIntoTheUKFromSmallProducersSummary.lowBandRow(userAnswers, checkAnswers = false),
            HowManyBroughtIntoTheUKFromSmallProducersSummary.lowBandLevyRow(userAnswers, config.lowerBandCostPerLitre),
            HowManyBroughtIntoTheUKFromSmallProducersSummary.highBandRow(userAnswers, checkAnswers = false),
            HowManyBroughtIntoTheUKFromSmallProducersSummary.highBandLevyRow(userAnswers, config.higherBandCostPerLitre)
          ).flatten)}else{ SummaryListViewModel(rows = Seq(
          BroughtIntoUkFromSmallProducersSummary.row(userAnswers, checkAnswers = false)).flatten)}

      val claimCreditsForExportsAnswers =
        if(userAnswers.get(ClaimCreditsForExportsPage).getOrElse(false) == true){
          SummaryListViewModel(rows = Seq(
            ClaimCreditsForExportsSummary.row(userAnswers, checkAnswers = false),
            HowManyCreditsForExportSummary.lowBandRow(userAnswers, checkAnswers = false),
            HowManyCreditsForExportSummary.lowBandLevyRow(userAnswers, config.lowerBandCostPerLitre),
            HowManyCreditsForExportSummary.highBandRow(userAnswers, checkAnswers = false),
            HowManyCreditsForExportSummary.highBandLevyRow(userAnswers, config.higherBandCostPerLitre)
          ).flatten)}else{ SummaryListViewModel(rows = Seq(
          ClaimCreditsForExportsSummary.row(userAnswers, checkAnswers = false)).flatten)}

      val claimCreditsForLostDamagedAnswers =
        if(userAnswers.get(ClaimCreditsForLostDamagedPage).getOrElse(false) == true){
          SummaryListViewModel(rows = Seq(
            ClaimCreditsForLostDamagedSummary.row(userAnswers, checkAnswers = false),
            HowManyCreditsForLostDamagedSummary.lowBandRow(userAnswers, checkAnswers = false),
            HowManyCreditsForLostDamagedSummary.lowBandLevyRow(userAnswers, config.lowerBandCostPerLitre),
            HowManyCreditsForLostDamagedSummary.highBandRow(userAnswers, checkAnswers = false),
            HowManyCreditsForLostDamagedSummary.highBandLevyRow(userAnswers, config.higherBandCostPerLitre)
          ).flatten)}else{ SummaryListViewModel(rows = Seq(
          ClaimCreditsForLostDamagedSummary.row(userAnswers, checkAnswers = false)).flatten)}



      val amountOwed:String = "£100,000.00"
      val balance:BigDecimal = 0
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
              ))
  }
}
