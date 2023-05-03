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
import models.Amounts
import play.api.Logger
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.{SDILSessionCache, SDILSessionKeys}
import services.ReturnService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utilitlies.ReturnsHelper.extractReturnPeriod
import views.html.ReturnSentView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}


class ReturnsController @Inject()(
                                   override val messagesApi: MessagesApi,
                                   identify: IdentifierAction,
                                   getData: DataRetrievalAction,
                                   returnService: ReturnService,
                                   requireData: DataRequiredAction,
                                   val controllerComponents: MessagesControllerComponents,
                                   view: ReturnSentView,
                                   sessionCache: SDILSessionCache
                                     )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {


  val logger: Logger = Logger(this.getClass())

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
            } else {
              logger.error(s"Pending returns for $sdilEnrolment don't contain the return for year ${returnPeriod.year} quarter ${returnPeriod.quarter}")
              Future.successful(Redirect(routes.JourneyRecoveryController.onPageLoad()))
            }
           Redirect(routes.ReturnSentController.onPageLoad())
          case _ =>
            logger.error(s"No amount found in the cache for $sdilEnrolment year ${returnPeriod.year} quarter ${returnPeriod.quarter}")
            Redirect(routes.JourneyRecoveryController.onPageLoad())
        }
      }
  }
}
