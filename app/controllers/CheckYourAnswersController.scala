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

import com.google.inject.Inject
import config.FrontendAppConfig
import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import models.UserAnswers
import pages.{AddASmallProducerPage, BrandsPackagedAtOwnSitesPage, BroughtIntoUKPage, BroughtIntoUkFromSmallProducersPage, ClaimCreditsForExportsPage, ClaimCreditsForLostDamagedPage, ExemptionsForSmallProducersPage, HowManyAsAContractPackerPage, HowManyBroughtIntoTheUKFromSmallProducersPage, HowManyBroughtIntoUkPage, HowManyCreditsForExportPage, HowManyCreditsForLostDamagedPage, OwnBrandsPage, PackagedContractPackerPage, QuestionPage, SmallProducerDetailsPage}
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.checkAnswers.{BrandsPackagedAtOwnSitesSummary, BroughtIntoUKSummary, BroughtIntoUkFromSmallProducersSummary, ClaimCreditsForExportsSummary, ClaimCreditsForLostDamagedSummary, ExemptionsForSmallProducersSummary, HowManyAsAContractPackerSummary, HowManyBroughtIntoTheUKFromSmallProducersSummary, HowManyBroughtIntoUkSummary, HowManyCreditsForExportSummary, HowManyCreditsForLostDamagedSummary, OwnBrandsSummary, PackagedContractPackerSummary, SmallProducerDetailsSummary}
import viewmodels.govuk.summarylist._
import views.html.CheckYourAnswersView

import scala.concurrent.Future
import scala.util.{Failure, Success}

class CheckYourAnswersController @Inject()(
                                            override val messagesApi: MessagesApi,
                                            config: FrontendAppConfig,
                                            identify: IdentifierAction,
                                            getData: DataRetrievalAction,
                                            requireData: DataRequiredAction,
                                            val controllerComponents: MessagesControllerComponents,
                                            view: CheckYourAnswersView
                                          ) extends FrontendBaseController with I18nSupport {

  def onPageLoad(): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>

      val userAnswers = cleanUpUserAnswers(request.userAnswers)
      val lowerBandCostPerLitre = config.lowerBandCostPerLitre
      val higherBandCostPerLitre = config.higherBandCostPerLitre

      val returnPeriod = request.returnPeriod match {
        case Some(returnPeriod) =>
          val year = returnPeriod.year
          returnPeriod.quarter match {
            case 0 => s"${Messages("firstQuarter")} $year"
            case 1 => s"${Messages("secondQuarter")} $year"
            case 2 => s"${Messages("thirdQuarter")} $year"
            case 3 => s"${Messages("fourthQuarter")} $year"
          }
        case None => throw new RuntimeException("No return period returned")
      }

      //println(Console.YELLOW + userAnswers + Console.WHITE)

      // we want to update user answers to not contain the brands litres if this answer has been changed to no
      val ownBrandsAnswers = SummaryListViewModel(rows = Seq(
          OwnBrandsSummary.row(request.userAnswers),
          BrandsPackagedAtOwnSitesSummary.lowBandRow(userAnswers),
          BrandsPackagedAtOwnSitesSummary.lowBandLevyRow(userAnswers, lowerBandCostPerLitre),
          BrandsPackagedAtOwnSitesSummary.highBandRow(userAnswers),
          BrandsPackagedAtOwnSitesSummary.highBandLevyRow(userAnswers, higherBandCostPerLitre)
        ).flatten)

      val packagedContractPackerAnswers = SummaryListViewModel(rows = Seq(
        PackagedContractPackerSummary.row(userAnswers),
        HowManyAsAContractPackerSummary.lowBandRow(userAnswers),
        HowManyAsAContractPackerSummary.lowBandLevyRow(userAnswers, lowerBandCostPerLitre),
        HowManyAsAContractPackerSummary.highBandRow(userAnswers),
        HowManyAsAContractPackerSummary.highBandLevyRow(userAnswers, higherBandCostPerLitre)
      ).flatten)

      val exemptionsForSmallProducersAnswers =
        if(userAnswers.get(ExemptionsForSmallProducersPage).getOrElse(false)){
          SummaryListViewModel(rows = Seq(
            ExemptionsForSmallProducersSummary.row(userAnswers),
            SmallProducerDetailsSummary.lowBandRow(userAnswers),
            SmallProducerDetailsSummary.lowBandLevyRow(userAnswers, lowerBandCostPerLitre),
            SmallProducerDetailsSummary.highBandRow(userAnswers),
            SmallProducerDetailsSummary.highBandLevyRow(userAnswers, higherBandCostPerLitre)
          ).flatten)
        } else {
          SummaryListViewModel(rows = Seq(ExemptionsForSmallProducersSummary.row(userAnswers)).flatten)
        }

      val broughtIntoTheUKAnswers = SummaryListViewModel(rows = Seq(
        BroughtIntoUKSummary.row(userAnswers),
        HowManyBroughtIntoUkSummary.lowBandRow(userAnswers),
        HowManyBroughtIntoUkSummary.lowBandLevyRow(userAnswers, lowerBandCostPerLitre),
        HowManyBroughtIntoUkSummary.highBandRow(userAnswers),
        HowManyBroughtIntoUkSummary.highBandLevyRow(userAnswers, higherBandCostPerLitre)
      ).flatten)

      val broughtIntoTheUKSmallProducersAnswers = SummaryListViewModel(rows = Seq(
        BroughtIntoUkFromSmallProducersSummary.row(userAnswers),
        HowManyBroughtIntoTheUKFromSmallProducersSummary.lowBandRow(userAnswers),
        HowManyBroughtIntoTheUKFromSmallProducersSummary.lowBandLevyRow(userAnswers, lowerBandCostPerLitre),
        HowManyBroughtIntoTheUKFromSmallProducersSummary.highBandRow(userAnswers),
        HowManyBroughtIntoTheUKFromSmallProducersSummary.highBandLevyRow(userAnswers, higherBandCostPerLitre)
      ).flatten)

      val claimCreditsForExportsAnswers = SummaryListViewModel(rows = Seq(
        ClaimCreditsForExportsSummary.row(userAnswers),
        HowManyCreditsForExportSummary.lowBandRow(userAnswers),
        HowManyCreditsForExportSummary.lowBandLevyRow(userAnswers, lowerBandCostPerLitre),
        HowManyCreditsForExportSummary.highBandRow(userAnswers),
        HowManyCreditsForExportSummary.highBandLevyRow(userAnswers, higherBandCostPerLitre)
      ).flatten)

      val claimCreditsForLostOrDamagedAnswers = SummaryListViewModel(rows = Seq(
        ClaimCreditsForLostDamagedSummary.row(userAnswers),
        HowManyCreditsForLostDamagedSummary.lowBandRow(userAnswers),
        HowManyCreditsForLostDamagedSummary.lowBandLevyRow(userAnswers, lowerBandCostPerLitre),
        HowManyCreditsForLostDamagedSummary.highBandRow(userAnswers),
        HowManyCreditsForLostDamagedSummary.highBandLevyRow(userAnswers, higherBandCostPerLitre)
      ).flatten)

      Ok(view(request.orgName, returnPeriod, ownBrandsAnswers,
        packagedContractPackerAnswers,
        exemptionsForSmallProducersAnswers,
        broughtIntoTheUKAnswers,
        broughtIntoTheUKSmallProducersAnswers,
        claimCreditsForExportsAnswers,
        claimCreditsForLostOrDamagedAnswers
      ))
  }

  def cleanUpUserAnswers(userAnswers: UserAnswers): UserAnswers = {
    if (!userAnswers.get(OwnBrandsPage).getOrElse(false)) {
      removePageData(userAnswers, BrandsPackagedAtOwnSitesPage)
    } else {
      userAnswers
    }
  }


  def removePageData(userAnswers: UserAnswers, page: QuestionPage[_]) = {
    userAnswers.remove(page) match {
      case Success(updatedAnswers) => updatedAnswers
      case Failure(exception) => println(s"Failed to remove value \n ${exception.getMessage}")
        userAnswers
    }
  }


}
