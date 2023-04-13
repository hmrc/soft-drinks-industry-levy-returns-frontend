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
import models.{Amounts, UserAnswers}
import play.api.Logger
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SDILSessionCache
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utilitlies.ReturnsHelper.{extractReturnPeriod, extractTotal, listItemsWithTotal}
import utilitlies.{CacheHelper, LevyCalculator, ReturnsHelper, TotalForQuarter}
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
                                            sdilConnector: SoftDrinksIndustryLevyConnector,
                                            sessionCache: SDILSessionCache,
                                            levyCalculator: LevyCalculator,
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

    val balanceAllEnabled = config.balanceAllEnabled
    val sdilEnrolment = request.sdilEnrolment
    val returnPeriod = extractReturnPeriod(request)

    val answers = userAnswers match {
      case Some(answers) => answers
      case _ => request.userAnswers
    }

    println("&&&&&&&&HHHHHHHHHHH")
    println(answers)

    val rowCalculations = levyCalculator.calculateLevyForAnswers(answers)
    val cacheHelper = new CacheHelper(sessionCache)

    (for {
      _ <- cacheHelper.cacheRowAmounts(sdilEnrolment, rowCalculations)
      isSmallProducer <- sdilConnector.checkSmallProducerStatus(sdilEnrolment, returnPeriod)
      balanceBroughtForward <-
        if (balanceAllEnabled) {
          sdilConnector.balanceHistory(sdilEnrolment, withAssessment = false).map { financialItem =>
            extractTotal(listItemsWithTotal(financialItem))
          }
        } else {
          sdilConnector.balance(sdilEnrolment, withAssessment = false)
        }
    } yield {

      val totalForQuarter = TotalForQuarter.calculateTotal(answers, isSmallProducer.getOrElse(false))(config)
      val total = totalForQuarter - balanceBroughtForward
      val isNilReturn = totalForQuarter == 0
      val amounts = Amounts(totalForQuarter, balanceBroughtForward, total)
      cacheHelper.cacheAmounts(sdilEnrolment, amounts)

      Ok(view(request.subscription.orgName,
        returnPeriod,
        answers,
        amounts,
        isNilReturn
      )(request,messages, config))
    }) recoverWith {
      case t: Throwable =>
        logger.error(s"Exception occurred while retrieving SDIL data for $sdilEnrolment", t)
        Redirect(routes.JourneyRecoveryController.onPageLoad()).pure[Future]
    }
  }
}
