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
import models.{Amounts, ReturnPeriod, UserAnswers, extractTotal, listItemsWithTotal}
import pages._
import play.api.Logger
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.{SDILSessionCache, SDILSessionKeys}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utilitlies.{GenericError, ReturnsHelper}
import viewmodels.checkAnswers._
import viewmodels.govuk.summarylist._
import views.html.CheckYourAnswersView

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

class CheckYourAnswersController @Inject()(
                                            override val messagesApi: MessagesApi,
                                            config: FrontendAppConfig,
                                            identify: IdentifierAction,
                                            getData: DataRetrievalAction,
                                            requireData: DataRequiredAction,
                                            val controllerComponents: MessagesControllerComponents,
                                            view: CheckYourAnswersView,
                                            connector: SoftDrinksIndustryLevyConnector,
                                            sessionCache: SDILSessionCache,
                                          ) (implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  val lowerBandCostPerLitre: BigDecimal = config.lowerBandCostPerLitre
  val higherBandCostPerLitre: BigDecimal = config.higherBandCostPerLitre
  val logger: Logger = Logger(this.getClass())

  def onPageLoad: Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request => constructPage(request)
  }

  def noActivityToReport: Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      val noActivityUserAnswers = ReturnsHelper.noActivityUserAnswers(request.sdilEnrolment)
      constructPage(request, Some(noActivityUserAnswers))
  }

  private def constructPage(request: DataRequest[AnyContent],
                            userAnswers: Option[UserAnswers] = None
                           )(implicit hc: HeaderCarrier, messages: Messages) = {

    val answers = userAnswers match {
      case Some(answers) => answers
      case _ => request.userAnswers
    }
    val balanceAllEnabled = config.balanceAllEnabled
    val returnPeriod = request.returnPeriod.getOrElse(GenericError.throwException("No return period found"))
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

      val totalForQuarter = calculateTotalForQuarter(answers, isSmallProducer.getOrElse(false))
      val total = totalForQuarter - balanceBroughtForward
      val isNilReturn = totalForQuarter == 0

      cacheAmounts(sdilEnrolment, Amounts(totalForQuarter, balanceBroughtForward, total))

      Ok(view(request.subscription.orgName,
        formattedReturnPeriodQuarter(returnPeriod),
        ownBrandsAnswers(answers),
        packagedContractPackerAnswers(answers),
        exemptionsForSmallProducersAnswers(answers),
        broughtIntoTheUKAnswers(answers),
        broughtIntoTheUKSmallProducersAnswers(answers),
        claimCreditsForExportsAnswers(answers),
        claimCreditsForLostOrDamagedAnswers(answers),
        AmountToPaySummary.sectionHeaderTitle(total),
        AmountToPaySummary.amountToPaySummary(totalForQuarter, balanceBroughtForward, total),
        AmountToPaySummary.amountInCredit(total),
        registeredSites(answers),
        isNilReturn
      )(request,messages))
    }) recoverWith {
      case t: Throwable =>
        logger.error(s"Exception occurred while retrieving SDIL data for $sdilEnrolment", t)
        Redirect(routes.JourneyRecoveryController.onPageLoad()).pure[Future]
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

  private def registeredSites(userAnswers: UserAnswers)(implicit messages: Messages) = {
    userAnswers.get(PackAtBusinessAddressPage) match {
      case Some(true) =>
        Some(SummaryListViewModel(rows = Seq(
          PackAtBusinessAddressSummary.row(userAnswers)
        ).flatten))
      case _ => None
    }
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

  private def calculateTotalForQuarter(userAnswers: UserAnswers, smallProducer: Boolean)(implicit messages: Messages) = {
    calculateLowBandTotalForQuarter(userAnswers, lowerBandCostPerLitre, smallProducer) +
      calculateHighBandTotalForQuarter(userAnswers, higherBandCostPerLitre, smallProducer)
  }

  private def calculateLowBandTotalForQuarter(userAnswers: UserAnswers, lowBandCostPerLitre: BigDecimal, smallProducer: Boolean)(implicit messages: Messages): BigDecimal = {
    val litresPackedAtOwnSite = userAnswers.get(BrandsPackagedAtOwnSitesPage).map(_.lowBand).getOrElse(0L)
    val litresAsContractPacker = userAnswers.get(HowManyAsAContractPackerPage).map(_.lowBand).getOrElse(0L)
    val litresBroughtIntoTheUk = userAnswers.get(HowManyBroughtIntoUkPage).map(_.lowBand).getOrElse(0L)
    val litresExported = userAnswers.get(HowManyCreditsForExportPage).map(_.lowBand).getOrElse(0L)
    val litresLostOrDamaged = userAnswers.get(HowManyCreditsForLostDamagedPage).map(_.lowBand).getOrElse(0L)

    val total = litresBroughtIntoTheUk + litresAsContractPacker
    val totalCredits = litresExported + litresLostOrDamaged

    smallProducer match {
      case true => (total - totalCredits) * lowBandCostPerLitre
      case _ => (total + litresPackedAtOwnSite - totalCredits) * lowBandCostPerLitre
    }
  }

  private def calculateHighBandTotalForQuarter(userAnswers: UserAnswers, highBandCostPerLitre: BigDecimal, smallProducer: Boolean)(implicit messages: Messages): BigDecimal = {
    val litresPackedAtOwnSite = userAnswers.get(BrandsPackagedAtOwnSitesPage).map(_.highBand).getOrElse(0L)
    val litresAsContractPacker = userAnswers.get(HowManyAsAContractPackerPage).map(_.highBand).getOrElse(0L)
    val litresBroughtIntoTheUk = userAnswers.get(HowManyBroughtIntoUkPage).map(_.highBand).getOrElse(0L)
    val litresExported = userAnswers.get(HowManyCreditsForExportPage).map(_.highBand).getOrElse(0L)
    val litresLostOrDamaged = userAnswers.get(HowManyCreditsForLostDamagedPage).map(_.highBand).getOrElse(0L)

    val total = litresBroughtIntoTheUk + litresAsContractPacker
    val totalCredits = litresExported + litresLostOrDamaged

    smallProducer match {
      case true => (total - totalCredits) * highBandCostPerLitre
      case _ => (total + litresPackedAtOwnSite - totalCredits) * highBandCostPerLitre
    }
  }

  private def cacheAmounts(sdilEnrolment: String, amounts: Amounts) = {
    sessionCache.save(sdilEnrolment, SDILSessionKeys.AMOUNTS, amounts).onComplete {
      case Success(_) => logger.info(s"Amounts saved in session cache for $sdilEnrolment")
      case Failure(error) => logger.error(s"Failed to save amounts in session cache for $sdilEnrolment Error: $error")
    }
  }
}
