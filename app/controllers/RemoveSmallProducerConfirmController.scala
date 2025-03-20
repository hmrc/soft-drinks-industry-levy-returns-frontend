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
import forms._
import handlers.ErrorHandler
import models.Mode
import navigation.Navigator
import pages.{ RemoveSmallProducerConfirmPage, SmallProducerDetailsPage }
import play.api.i18n.MessagesApi
import play.api.mvc.{ Action, AnyContent, MessagesControllerComponents }
import repositories.SessionRepository
import utilities.GenericLogger
import views.html.RemoveSmallProducerConfirmView

import javax.inject.Inject
import scala.concurrent.{ ExecutionContext, Future }

class RemoveSmallProducerConfirmController @Inject() (
  override val messagesApi: MessagesApi,
  val sessionRepository: SessionRepository,
  val navigator: Navigator,
  val errorHandler: ErrorHandler,
  val genericLogger: GenericLogger,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  checkReturnSubmission: CheckingSubmissionAction,
  formProvider: RemoveSmallProducerConfirmFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: RemoveSmallProducerConfirmView)(implicit ec: ExecutionContext) extends ControllerHelper {

  private val form = formProvider()

  def onPageLoad(mode: Mode, sdil: String): Action[AnyContent] = (identify andThen getData andThen requireData andThen checkReturnSubmission) {
    implicit request =>

      val preparedForm = request.userAnswers.get(RemoveSmallProducerConfirmPage) match {
        case None => form
        case Some(value) => form.fill(value)
      }

      val smallProducerList = request.userAnswers.smallProducerList
      val smallProducerMissing = !smallProducerList.exists(producer => producer.sdilRef == sdil)

      if (smallProducerMissing && smallProducerList.nonEmpty) {
        Redirect(navigator.nextPage(SmallProducerDetailsPage, mode, request.userAnswers, smallProducerMissing = Some(smallProducerMissing)))
      } else {
        val smallProducerName = smallProducerList.filter(x => x.sdilRef == sdil).map(producer => producer.alias).head
        Ok(view(preparedForm, mode, sdil, smallProducerName))
      }

  }

  def onSubmit(mode: Mode, sdil: String): Action[AnyContent] = (identify andThen getData andThen requireData andThen checkReturnSubmission).async {
    implicit request =>
      val smallProducerName = request.userAnswers.smallProducerList.filter(x => x.sdilRef == sdil).map(producer => producer.alias).head
      form.bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors, mode, sdil, smallProducerName))),
        formData => {
          if (formData) {
            val updatedAnswers = request.userAnswers.set(RemoveSmallProducerConfirmPage, formData)
            val modifiedProducerList = request.userAnswers.smallProducerList.filterNot(producer => producer.sdilRef == sdil)
            val updatedAnswersFinal = updatedAnswers.get.copy(smallProducerList = modifiedProducerList)
            setAndRedirect(updatedAnswersFinal, RemoveSmallProducerConfirmPage, mode)
          } else {
            val updatedAnswers = request.userAnswers.set(RemoveSmallProducerConfirmPage, formData)
            updateDatabaseAndRedirect(updatedAnswers, RemoveSmallProducerConfirmPage, mode)
          }
        })
  }
}

