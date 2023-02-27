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
import pages.{BroughtIntoUKPage, BroughtIntoUkFromSmallProducersPage, OwnBrandsPage}
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
import viewmodels.checkAnswers.{BrandsPackagedAtOwnSitesSummary, BroughtIntoUKSummary, BroughtIntoUkFromSmallProducersSummary, ExemptionsForSmallProducersSummary, HowManyAsAContractPackerSummary, HowManyBroughtIntoTheUKFromSmallProducersSummary, HowManyBroughtIntoUkSummary, OwnBrandsSummary, PackagedContractPackerSummary, SmallProducerDetailsSummary}

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
      val subscription = Await.result(connector.retrieveSubscription(request.userAnswers.id,"sdil"),20.seconds).get

      val pageAnswers = request.userAnswers.get(OwnBrandsPage).map(answers =>  answers match {
        case answers if answers == true => "Yes"
        case _ => "False"
      }).getOrElse("False")

      val userAnswers = request.userAnswers

      val ownBrandsAnswer = {
        if(userAnswers.get(OwnBrandsPage).getOrElse(false) == true){
        SummaryListViewModel(rows = Seq(
        OwnBrandsSummary.row(userAnswers, checkAnswers = false),
        BrandsPackagedAtOwnSitesSummary.lowBandRow(userAnswers),
        BrandsPackagedAtOwnSitesSummary.lowBandLevyRow(userAnswers, config.lowerBandCostPerLitre),
        BrandsPackagedAtOwnSitesSummary.highBandRow(userAnswers),
        BrandsPackagedAtOwnSitesSummary.highBandLevyRow(userAnswers, config.higherBandCostPerLitre)
      ).flatten)}else{SummaryListViewModel(rows = Seq(
          OwnBrandsSummary.row(userAnswers, checkAnswers = false)).flatten)}
      }

      val packagedContractPackerAnswers =
        if(userAnswers.get(OwnBrandsPage).getOrElse(false) == true){
        SummaryListViewModel(rows = Seq(
        PackagedContractPackerSummary.row(request.userAnswers, checkAnswers = false),
        HowManyAsAContractPackerSummary.lowBandRow(userAnswers),
        HowManyAsAContractPackerSummary.lowBandLevyRow(userAnswers, config.lowerBandCostPerLitre),
        HowManyAsAContractPackerSummary.highBandRow(userAnswers),
        HowManyAsAContractPackerSummary.highBandLevyRow(userAnswers, config.higherBandCostPerLitre)
      ).flatten)}else{ SummaryListViewModel(rows = Seq(
          PackagedContractPackerSummary.row(request.userAnswers, checkAnswers = false)).flatten)}

      val exemptionsForSmallProducersAnswers =
        if(userAnswers.get(OwnBrandsPage).getOrElse(false) == true){
          SummaryListViewModel(rows = Seq(
            ExemptionsForSmallProducersSummary.row(request.userAnswers, checkAnswers = false),
            SmallProducerDetailsSummary.lowBandRow(userAnswers),
            SmallProducerDetailsSummary.lowBandLevyRow(userAnswers, config.lowerBandCostPerLitre),
            SmallProducerDetailsSummary.highBandRow(userAnswers),
            SmallProducerDetailsSummary.highBandLevyRow(userAnswers, config.higherBandCostPerLitre)
          ).flatten)}else{ SummaryListViewModel(rows = Seq(
          PackagedContractPackerSummary.row(request.userAnswers, checkAnswers = false)).flatten)}

      val broughtIntoUkAnswers =
        if(userAnswers.get(BroughtIntoUKPage).getOrElse(false) == true){
          SummaryListViewModel(rows = Seq(
            BroughtIntoUKSummary.row(request.userAnswers, checkAnswers = false),
            HowManyBroughtIntoUkSummary.lowBandRow(userAnswers),
            HowManyBroughtIntoUkSummary.lowBandLevyRow(userAnswers, config.lowerBandCostPerLitre),
            HowManyBroughtIntoUkSummary.highBandRow(userAnswers),
            HowManyBroughtIntoUkSummary.highBandLevyRow(userAnswers, config.higherBandCostPerLitre)
          ).flatten)}else{ SummaryListViewModel(rows = Seq(
          PackagedContractPackerSummary.row(request.userAnswers, checkAnswers = false)).flatten)}

      val broughtIntoUkSmallProducerAnswers =
        if(userAnswers.get(BroughtIntoUkFromSmallProducersPage).getOrElse(false) == true){
          SummaryListViewModel(rows = Seq(
            BroughtIntoUkFromSmallProducersSummary.row(request.userAnswers, checkAnswers = false),
            HowManyBroughtIntoTheUKFromSmallProducersSummary.lowBandRow(userAnswers),
            HowManyBroughtIntoTheUKFromSmallProducersSummary.lowBandLevyRow(userAnswers, config.lowerBandCostPerLitre),
            HowManyBroughtIntoTheUKFromSmallProducersSummary.highBandRow(userAnswers),
            HowManyBroughtIntoTheUKFromSmallProducersSummary.highBandLevyRow(userAnswers, config.higherBandCostPerLitre)
          ).flatten)}else{ SummaryListViewModel(rows = Seq(
          PackagedContractPackerSummary.row(request.userAnswers, checkAnswers = false)).flatten)}

      println("Own Brands = "+ pageAnswers)
      val amountOwed:String = "£100,000.00"
      val balance = 0
      val paymentDate = ReturnPeriod(2022,1)
      val returnDate = ReturnPeriod(2022,1)
      LocalTime.now(ZoneId.of("Europe/London")).format(DateTimeFormatter.ofPattern("h:mma")).toLowerCase

      Ok(view(returnDate,
              subscription,
              amountOwed,
              balance,
              paymentDate,
              pageAnswers,
              ownBrandsAnswer,
              packagedContractPackerAnswers,
              exemptionsForSmallProducersAnswers,
              broughtIntoUkAnswers,
              broughtIntoUkSmallProducerAnswers))
  }
}