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
import handlers.ErrorHandler
import models.Amounts
import navigation.Navigator
import pages.ReturnChangeRegistrationPage
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.{SDILSessionCache, SDILSessionKeys, SessionRepository}
import services.ReturnService
import utilitlies.GenericLogger
import utilitlies.ReturnsHelper.extractReturnPeriod

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}


class ReturnsController @Inject()(
                                   override val messagesApi: MessagesApi,
                                   val sessionRepository: SessionRepository,
                                   val navigator: Navigator,
                                   val errorHandler: ErrorHandler,
                                   val genericLogger: GenericLogger,
                                   identify: IdentifierAction,
                                   getData: DataRetrievalAction,
                                   returnService: ReturnService,
                                   requireData: DataRequiredAction,
                                   val controllerComponents: MessagesControllerComponents,
                                   sessionCache: SDILSessionCache
                                     )(implicit ec: ExecutionContext) extends ControllerHelper {

  def onPageLoad(nilReturn: Boolean): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      val sdilEnrolment = request.sdilEnrolment
      val subscription = request.subscription
      val userAnswers = request.userAnswers
      val returnPeriod = extractReturnPeriod(request)

      for {
        session <- sessionCache.fetchEntry[Amounts](sdilEnrolment,SDILSessionKeys.AMOUNTS)
        pendingReturns <- returnService.getPendingReturns(subscription.utr)
      } yield {
        session match {
          case Some(amounts) =>
            if (pendingReturns.contains(returnPeriod)) {
              returnService.returnsUpdate(subscription, returnPeriod, userAnswers, nilReturn)
              updateDatabaseWithoutRedirect(request.userAnswers.copy(submitted = true), page = ReturnChangeRegistrationPage) // page is a holder, only relevant if save to DB fails
            } else {
              genericLogger.logger.error(s"Pending returns for $sdilEnrolment don't contain the return for year ${returnPeriod.year} quarter ${returnPeriod.quarter}")
              Future.successful(Redirect(routes.JourneyRecoveryController.onPageLoad()))
            }
           Redirect(routes.ReturnSentController.onPageLoad())
          case _ =>
            genericLogger.logger.error(s"No amount found in the cache for $sdilEnrolment year ${returnPeriod.year} quarter ${returnPeriod.quarter}")
            Redirect(routes.JourneyRecoveryController.onPageLoad())
        }
      }
  }
}
