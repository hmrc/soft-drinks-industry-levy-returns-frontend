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

import handlers.ErrorHandler
import models.retrieved.RetrievedSubscription
import models.{Mode, SdilReturn, UserAnswers}
import navigation.Navigator
import pages.Page
import play.api.i18n.I18nSupport
import play.api.mvc.{AnyContent, Request, Result}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utilitlies.GenericLogger

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

trait ControllerHelper extends FrontendBaseController with I18nSupport {

  val sessionRepository: SessionRepository
  val navigator: Navigator
  val errorHandler: ErrorHandler
  val genericLogger: GenericLogger
  private val internalServerErrorBaseMessage =  "Failed to set value in session repository"
  private def sessionRepo500ErrorMessage(page: Page): String = s"$internalServerErrorBaseMessage while attempting set on ${page.toString}"

  def updateDatabaseAndRedirect(updatedAnswers: Try[UserAnswers], page: Page, mode: Mode, withSdilReturn: Boolean = false,
                                subscription: Option[RetrievedSubscription] = None,
                                smallProducerMissing: Option[Boolean] = None)
                               (implicit ec: ExecutionContext, request: Request[AnyContent]): Future[Result] = {
    println(Console.YELLOW + "getting to update db & redirect 222222222222222222222 " + Console.WHITE)
    updatedAnswers match {
      case Failure(_) =>

        failure(page)
      case Success(answers) =>
        val sdilReturn: Option[SdilReturn] =  if (withSdilReturn) {
          Some(SdilReturn.apply(answers))
        } else {
          None
        }
        setAndRedirect(answers, page, mode, sdilReturn, subscription, smallProducerMissing)
    }
  }

  def setAndRedirect(updatedAnswers: UserAnswers, page: Page, mode: Mode, sdilReturn: Option[SdilReturn] = None,
                     subscription: Option[RetrievedSubscription] = None,
                     smallProducerMissing: Option[Boolean] = None)
                               (implicit ec: ExecutionContext, request: Request[AnyContent]): Future[Result] = {
    println(Console.YELLOW + "getting to set & redirect 2aaaaaaaaaaaaaa " + Console.WHITE)
    sessionRepository.set(updatedAnswers).map {
        case Right(_) =>
          Redirect(navigator.nextPage(page, mode, updatedAnswers, sdilReturn, subscription, smallProducerMissing))
        case Left(_) =>
          genericLogger.logger.error(sessionRepo500ErrorMessage(page))
          InternalServerError(errorHandler.internalServerErrorTemplate)
    }
  }

  def updateDatabaseWithoutRedirect(updatedAnswers: UserAnswers)
                              (implicit ec: ExecutionContext): Future[Status] = {

    sessionRepository.set(updatedAnswers).map {
        case Right(_) => Ok
        case Left(_) =>
          genericLogger.logger.error(internalServerErrorBaseMessage)
          InternalServerError
      }
    }

  private def failure(page: Page)(implicit request: Request[_]): Future[Result] = {
    genericLogger.logger.error(s"Failed to resolve user answers while on ${page.toString}")
    Future.successful(InternalServerError(errorHandler.internalServerErrorTemplate))
  }

}
