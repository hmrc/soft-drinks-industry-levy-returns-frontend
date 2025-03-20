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
import controllers.actions._
import handlers.ErrorHandler
import models.retrieved.RetrievedSubscription
import models.{ SdilReturn, UserAnswers }
import navigation.Navigator
import orchestrators.ReturnsOrchestrator
import pages.CheckYourAnswersPage
import play.api.i18n.MessagesApi
import play.api.mvc.{ Action, AnyContent, Call, MessagesControllerComponents }
import repositories.SessionRepository
import utilities.{ GenericLogger, UserTypeCheck }
import views.html.CheckYourAnswersView

import scala.concurrent.{ ExecutionContext, Future }

class CheckYourAnswersController @Inject() (
  override val messagesApi: MessagesApi,
  val config: FrontendAppConfig,
  val sessionRepository: SessionRepository,
  val navigator: Navigator,
  val errorHandler: ErrorHandler,
  val genericLogger: GenericLogger,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  requiredUserAnswers: RequiredUserAnswers,
  checkReturnSubmission: CheckingSubmissionAction,
  val controllerComponents: MessagesControllerComponents,
  checkYourAnswersView: CheckYourAnswersView,
  returnsOrchestrator: ReturnsOrchestrator)(implicit ec: ExecutionContext) extends ControllerHelper {

  def onPageLoad: Action[AnyContent] = (identify andThen getData andThen requireData andThen checkReturnSubmission).async {
    implicit request =>
      requiredUserAnswers.requireData(CheckYourAnswersPage) {
        val sdilRef = request.sdilEnrolment
        val returnPeriod = request.returnPeriod
        val isSmallProducer = request.subscription.activity.smallProducer

        sanitiseUserAnswers(request.userAnswers, request.subscription).flatMap { userAnswers =>
          returnsOrchestrator.calculateAmounts(sdilRef, userAnswers, returnPeriod).map { amounts =>
            val submitUrl: Call = routes.CheckYourAnswersController.onSubmit
            Ok(checkYourAnswersView(
              request.subscription.orgName,
              returnPeriod,
              userAnswers,
              amounts,
              submitUrl,
              isSmallProducer)(implicitly, implicitly, config))
          }.recoverWith {
            case t: Throwable =>
              genericLogger.logger.error(s"Exception occurred while retrieving SDIL data for $sdilRef", t)
              Future.successful(Redirect(routes.JourneyRecoveryController.onPageLoad()))
          }
        }
      }
  }

  def onSubmit: Action[AnyContent] = (identify andThen getData andThen requireData andThen checkReturnSubmission).async {
    implicit request =>
      requiredUserAnswers.requireData(CheckYourAnswersPage) {
        returnsOrchestrator.completeReturnAndUpdateUserAnswers().map { _ =>
          Redirect(routes.ReturnSentController.onPageLoad)
        }
      }
  }

  private def sanitiseUserAnswers(answers: UserAnswers, subscription: RetrievedSubscription) = {
    val sanitisedPackingSitesUserAnswers = sanitisePackingSitesInAnswers(answers, subscription)
    val sanitisedAnswers = sanitiseWarehousesInAnswers(sanitisedPackingSitesUserAnswers, subscription)

    updateDatabaseWithoutRedirect(sanitisedAnswers, CheckYourAnswersPage).map { _ =>
      sanitisedAnswers
    }
  }

  private def sanitisePackingSitesInAnswers(answers: UserAnswers, subscription: RetrievedSubscription) = {
    if (!UserTypeCheck.isNewPacker(SdilReturn.apply(answers), subscription)) {
      answers.copy(packagingSiteList = Map.empty)
    } else {
      answers
    }
  }

  private def sanitiseWarehousesInAnswers(answers: UserAnswers, subscription: RetrievedSubscription) = {
    if (!UserTypeCheck.isNewImporter(SdilReturn.apply(answers), subscription)) {
      answers.copy(warehouseList = Map.empty)
    } else {
      answers
    }
  }
}
