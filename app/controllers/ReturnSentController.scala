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

import config.FrontendAppConfig
import controllers.actions._
import orchestrators.ReturnsOrchestrator
import play.api.i18n.{ I18nSupport, MessagesApi }
import play.api.mvc.{ Action, AnyContent, MessagesControllerComponents }
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import util.CurrencyFormatter
import views.html.ReturnSentView

import javax.inject.Inject
import scala.concurrent.{ ExecutionContext, Future }

class ReturnSentController @Inject() (
  returnsOrchestrator: ReturnsOrchestrator,
  override val messagesApi: MessagesApi,
  config: FrontendAppConfig,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  val controllerComponents: MessagesControllerComponents,
  view: ReturnSentView)(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  def onPageLoad: Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      val returnPeriod = request.returnPeriod
      if (request.userAnswers.submitted) {
        val sdilRef = request.sdilEnrolment
        val subscription = request.subscription
        val userAnswers = request.userAnswers
        val returnPeriod = request.returnPeriod

        returnsOrchestrator.getCalculatedAmountsForReturnSent(sdilRef, userAnswers, returnPeriod).map { amounts =>
          Ok(view(
            returnPeriod,
            userAnswers,
            amounts,
            subscription,
            CurrencyFormatter.formatAmountOfMoneyWithPoundSign(amounts.total))(implicitly, implicitly, config))
        }
      } else {
        Future.successful(Redirect(routes.ReturnsController.onPageLoad(returnPeriod.year, returnPeriod.quarter, false)))
      }
  }

}
