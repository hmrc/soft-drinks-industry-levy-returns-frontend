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
import forms.HowManyBoughtIntoUkFormProvider
import handlers.ErrorHandler
import models.Mode
import navigation.Navigator
import pages.HowManyBroughtIntoUkPage
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import utilitlies.GenericLogger
import views.html.HowManyBoughtIntoUkView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class HowManyBroughtIntoUkController @Inject()(
                                               override val messagesApi: MessagesApi,
                                               val sessionRepository: SessionRepository,
                                               val navigator: Navigator,
                                               val errorHandler: ErrorHandler,
                                               val genericLogger: GenericLogger,
                                               identify: IdentifierAction,
                                               getData: DataRetrievalAction,
                                               requireData: DataRequiredAction,
                                               checkReturnSubmission: CheckingSubmissionAction,
                                               formProvider: HowManyBoughtIntoUkFormProvider,
                                               val controllerComponents: MessagesControllerComponents,
                                               view: HowManyBoughtIntoUkView
                                     )(implicit ec: ExecutionContext) extends ControllerHelper {

  private val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData andThen checkReturnSubmission) {
    implicit request =>

     val preparedForm = request.userAnswers.get(HowManyBroughtIntoUkPage) match {
          case None => form
          case Some(value) => form.fill(value)
     }

     Ok(view(preparedForm, mode))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData andThen checkReturnSubmission).async {
    implicit request =>

      form.bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors, mode))),

        value => {
          val updatedUserAnswers = request.userAnswers.set(
            HowManyBroughtIntoUkPage, value)

          updateDatabaseAndRedirect(updatedUserAnswers, HowManyBroughtIntoUkPage, mode)
        }
      )
  }
}
