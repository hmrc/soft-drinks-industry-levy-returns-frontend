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
import orchestrators.ReturnsOrchestrator
import pages.CheckYourAnswersPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utilitlies.GenericLogger
import views.html.CheckYourAnswersView

import scala.concurrent.{ExecutionContext, Future}

class CheckYourAnswersController @Inject()(
                                            override val messagesApi: MessagesApi,
                                            val config: FrontendAppConfig,
                                            val genericLogger: GenericLogger,
                                            identify: IdentifierAction,
                                            getData: DataRetrievalAction,
                                            requireData: DataRequiredAction,
                                            requiredUserAnswers: RequiredUserAnswers,
                                            checkReturnSubmission: CheckingSubmissionAction,
                                            val controllerComponents: MessagesControllerComponents,
                                            checkYourAnswersView: CheckYourAnswersView,
                                            returnsOrchestrator: ReturnsOrchestrator
                                          ) (implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {


  def onPageLoad: Action[AnyContent] = (identify andThen getData andThen requireData andThen checkReturnSubmission).async {
    implicit request =>
      requiredUserAnswers.requireData(CheckYourAnswersPage) {
        val sdilRef = request.sdilEnrolment
        val returnPeriod = request.returnPeriod
        val userAnswers = request.userAnswers

        returnsOrchestrator.calculateAmounts(sdilRef, userAnswers, returnPeriod).map { amounts =>
          val submitUrl: Call = routes.CheckYourAnswersController.onSubmit

          Ok(checkYourAnswersView(request.subscription.orgName,
            returnPeriod,
            userAnswers,
            amounts,
            submitUrl
          )(implicitly, implicitly, config))
        }.recoverWith {
          case t: Throwable =>
            genericLogger.logger.error(s"Exception occurred while retrieving SDIL data for $sdilRef", t)
            Future.successful(Redirect(routes.JourneyRecoveryController.onPageLoad()))
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
}
