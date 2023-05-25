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

import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import handlers.ErrorHandler
import navigation.Navigator
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import utilitlies.GenericLogger

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class KeepAliveController @Inject()(
                                     val controllerComponents: MessagesControllerComponents,
                                     val errorHandler: ErrorHandler,
                                     val sessionRepository: SessionRepository,
                                     val navigator: Navigator,
                                     identify: IdentifierAction,
                                     getData: DataRetrievalAction,
                                     requireData: DataRequiredAction,
                                     val genericLogger: GenericLogger
                                   )(implicit ec: ExecutionContext) extends ControllerHelper {

  def keepAlive: Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      sessionRepository.keepAlive(request.userAnswers.id).map {
        case Right(_) => Ok
        case Left(_) =>
          genericLogger.logger.error("Failed to keep the session alive due to error from mongo session repository's keepAlive")
          InternalServerError(errorHandler.internalServerErrorTemplate)
      }
  }
  Future.successful(Ok)
}
