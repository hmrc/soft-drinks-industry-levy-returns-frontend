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

import cats.implicits.catsSyntaxApplicativeId
import com.google.inject.Inject
import config.FrontendAppConfig
import connectors.SoftDrinksIndustryLevyConnector
import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import models.requests.DataRequest
import models.{ReturnPeriod, UserAnswers, extractTotal, listItemsWithTotal}
import pages.ExemptionsForSmallProducersPage
import play.api.Logger
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.mvc.{AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.checkAnswers._
import viewmodels.govuk.summarylist._
import views.html.CheckYourAnswersView

import scala.concurrent.{ExecutionContext, Future}

class CheckYourAnswersController @Inject()(
                                            override val messagesApi: MessagesApi,
                                            config: FrontendAppConfig,
                                            identify: IdentifierAction,
                                            getData: DataRetrievalAction,
                                            requireData: DataRequiredAction,
                                            val controllerComponents: MessagesControllerComponents,
                                            view: CheckYourAnswersView,
                                            connector: SoftDrinksIndustryLevyConnector,
                                          ) (implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  val lowerBandCostPerLitre = config.lowerBandCostPerLitre
  val higherBandCostPerLitre = config.higherBandCostPerLitre
  val logger: Logger = Logger(this.getClass())

  def onPageLoad() = (identify andThen getData andThen requireData).async {
    implicit request =>

      val balanceAllEnabled = config.balanceAllEnabled
      val userAnswers = request.userAnswers
      val returnPeriod = currentReturnPeriod(request)
      val sdilEnrolment = request.sdilEnrolment

      (for {
        isSmallProducer <- connector.checkSmallProducerStatus(sdilEnrolment, returnPeriod)
        balanceBroughtForward <-
          if (balanceAllEnabled) {
            connector.balanceHistory(sdilEnrolment, withAssessment = false).map { financialItem =>
              extractTotal(listItemsWithTotal(financialItem))
            }
          } else connector.balance(sdilEnrolment, withAssessment = false)
      } yield {

        val amountToPaySection = amountToPaySummary(userAnswers, isSmallProducer, balanceBroughtForward)
        Ok(view(request.subscription.orgName,
          formattedReturnPeriodQuarter(returnPeriod),
          ownBrandsAnswers(userAnswers),
          packagedContractPackerAnswers(userAnswers),
          exemptionsForSmallProducersAnswers(userAnswers),
          broughtIntoTheUKAnswers(userAnswers),
          broughtIntoTheUKSmallProducersAnswers(userAnswers),
          claimCreditsForExportsAnswers(userAnswers),
          claimCreditsForLostOrDamagedAnswers(userAnswers),
          amountToPaySection._1,
          amountToPaySection._2,
          amountToPaySection._3,
          Some(registeredSites(userAnswers))
        ))
      }) recoverWith {
        case t: Throwable =>
          logger.error(s"Exception occurred while retrieving SDIL data for $sdilEnrolment", t)
          Redirect(routes.JourneyRecoveryController.onPageLoad()).pure[Future]
      }
  }

  private def currentReturnPeriod(request: DataRequest[AnyContent]) = {
    request.returnPeriod match {
      case Some(returnPeriod) => returnPeriod
      case None => throw new RuntimeException("No return period returned")
    }
  }

  private def formattedReturnPeriodQuarter(returnPeriod: ReturnPeriod)(implicit messages: Messages) = {
    returnPeriod.quarter match {
      case 0 => s"${Messages("firstQuarter")} ${returnPeriod.year}"
      case 1 => s"${Messages("secondQuarter")} ${returnPeriod.year}"
      case 2 => s"${Messages("thirdQuarter")} ${returnPeriod.year}"
      case 3 => s"${Messages("fourthQuarter")} ${returnPeriod.year}"
    }
  }

  private def amountToPaySummary(userAnswers: UserAnswers,
                                 isSmallProducer: Option[Boolean],
                                 balanceBroughtForward: BigDecimal)(implicit messages: Messages) = {

      AmountToPaySummary.amountToPayRow(
        userAnswers,
        lowerBandCostPerLitre,
        higherBandCostPerLitre,
        isSmallProducer.getOrElse(false),
        balanceBroughtForward)
  }

  private def registeredSites(userAnswers: UserAnswers)(implicit messages: Messages) = {
    SummaryListViewModel(rows = Seq(
      PackAtBusinessAddressSummary.row(userAnswers)
    ).flatten)
  }

  private def claimCreditsForLostOrDamagedAnswers(userAnswers: UserAnswers)(implicit messages: Messages) = {
    SummaryListViewModel(rows = Seq(
      ClaimCreditsForLostDamagedSummary.row(userAnswers),
      HowManyCreditsForLostDamagedSummary.lowBandRow(userAnswers),
      HowManyCreditsForLostDamagedSummary.lowBandLevyRow(userAnswers, lowerBandCostPerLitre),
      HowManyCreditsForLostDamagedSummary.highBandRow(userAnswers),
      HowManyCreditsForLostDamagedSummary.highBandLevyRow(userAnswers, higherBandCostPerLitre)
    ).flatten)
  }

  private def claimCreditsForExportsAnswers(userAnswers: UserAnswers)(implicit messages: Messages) = {
    SummaryListViewModel(rows = Seq(
      ClaimCreditsForExportsSummary.row(userAnswers),
      HowManyCreditsForExportSummary.lowBandRow(userAnswers),
      HowManyCreditsForExportSummary.lowBandLevyRow(userAnswers, lowerBandCostPerLitre),
      HowManyCreditsForExportSummary.highBandRow(userAnswers),
      HowManyCreditsForExportSummary.highBandLevyRow(userAnswers, higherBandCostPerLitre)
    ).flatten)
  }

  private def broughtIntoTheUKSmallProducersAnswers(userAnswers: UserAnswers)(implicit messages: Messages) = {
    SummaryListViewModel(rows = Seq(
      BroughtIntoUkFromSmallProducersSummary.row(userAnswers),
      HowManyBroughtIntoTheUKFromSmallProducersSummary.lowBandRow(userAnswers),
      HowManyBroughtIntoTheUKFromSmallProducersSummary.lowBandLevyRow(userAnswers, lowerBandCostPerLitre),
      HowManyBroughtIntoTheUKFromSmallProducersSummary.highBandRow(userAnswers),
      HowManyBroughtIntoTheUKFromSmallProducersSummary.highBandLevyRow(userAnswers, higherBandCostPerLitre)
    ).flatten)
  }

  private def broughtIntoTheUKAnswers(userAnswers: UserAnswers)(implicit messages: Messages) = {
    SummaryListViewModel(rows = Seq(
      BroughtIntoUKSummary.row(userAnswers),
      HowManyBroughtIntoUkSummary.lowBandRow(userAnswers),
      HowManyBroughtIntoUkSummary.lowBandLevyRow(userAnswers, lowerBandCostPerLitre),
      HowManyBroughtIntoUkSummary.highBandRow(userAnswers),
      HowManyBroughtIntoUkSummary.highBandLevyRow(userAnswers, higherBandCostPerLitre)
    ).flatten)
  }

  private def exemptionsForSmallProducersAnswers(userAnswers: UserAnswers)(implicit messages: Messages) = {
    if (userAnswers.get(ExemptionsForSmallProducersPage).getOrElse(false)) {
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
  }

  private def packagedContractPackerAnswers(userAnswers: UserAnswers)(implicit messages: Messages) = {
    SummaryListViewModel(rows = Seq(
      PackagedContractPackerSummary.row(userAnswers),
      HowManyAsAContractPackerSummary.lowBandRow(userAnswers),
      HowManyAsAContractPackerSummary.lowBandLevyRow(userAnswers, lowerBandCostPerLitre),
      HowManyAsAContractPackerSummary.highBandRow(userAnswers),
      HowManyAsAContractPackerSummary.highBandLevyRow(userAnswers, higherBandCostPerLitre)
    ).flatten)
  }

  private def ownBrandsAnswers(userAnswers: UserAnswers)(implicit messages: Messages) = {
    SummaryListViewModel(rows = Seq(
      OwnBrandsSummary.row(userAnswers),
      BrandsPackagedAtOwnSitesSummary.lowBandRow(userAnswers),
      BrandsPackagedAtOwnSitesSummary.lowBandLevyRow(userAnswers, lowerBandCostPerLitre),
      BrandsPackagedAtOwnSitesSummary.highBandRow(userAnswers),
      BrandsPackagedAtOwnSitesSummary.highBandLevyRow(userAnswers, higherBandCostPerLitre)
    ).flatten)
  }
}
