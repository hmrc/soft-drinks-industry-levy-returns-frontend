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
import forms.AskSecondaryWarehouseInReturnFormProvider

import javax.inject.Inject
import models.Mode
import navigation.Navigator
import pages.AskSecondaryWarehouseInReturnPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.{AddressLookupService, WarehouseDetails}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.AskSecondaryWarehouseInReturnView

import scala.concurrent.{ExecutionContext, Future}

class AskSecondaryWarehouseInReturnController @Inject()(
                                         override val messagesApi: MessagesApi,
                                         sessionRepository: SessionRepository,
                                         navigator: Navigator,
                                         identify: IdentifierAction,
                                         getData: DataRetrievalAction,
                                         requireData: DataRequiredAction,
                                         checkReturnSubmission: CheckingSubmissionAction,
                                         formProvider: AskSecondaryWarehouseInReturnFormProvider,
                                         val controllerComponents: MessagesControllerComponents,
                                         view: AskSecondaryWarehouseInReturnView,
                                         addressLookupService: AddressLookupService
                                 )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData andThen checkReturnSubmission) {
    implicit request =>

    val preparedForm = request.userAnswers.get(AskSecondaryWarehouseInReturnPage) match {
          case None => form
          case Some(value) => form.fill(value)
    }
    Ok(view(preparedForm, mode))

  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      form.bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors, mode))),
        value =>
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(AskSecondaryWarehouseInReturnPage, value))
            onwardUrl              <- if(value){
            sessionRepository.set(updatedAnswers).flatMap(_ =>
              addressLookupService.initJourneyAndReturnOnRampUrl(WarehouseDetails))
            } else {
              sessionRepository.set(updatedAnswers.copy(warehouseList = Map.empty)).flatMap(_ =>
                Future.successful(routes.CheckYourAnswersController.onPageLoad().url))
            }
          } yield {
            Redirect(onwardUrl)
          }
      )
  }
}
