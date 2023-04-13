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
import forms.RemoveWarehouseConfirmFormProvider
import models.{Mode, Warehouse}
import navigation.Navigator
import pages.{RemoveWarehouseConfirmPage, SecondaryWarehouseDetailsPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.RemoveWarehouseConfirmView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class RemoveWarehouseConfirmController @Inject()(
                                                  override val messagesApi: MessagesApi,
                                                  sessionRepository: SessionRepository,
                                                  navigator: Navigator,
                                                  identify: IdentifierAction,
                                                  getData: DataRetrievalAction,
                                                  requireData: DataRequiredAction,
                                                  formProvider: RemoveWarehouseConfirmFormProvider,
                                                  val controllerComponents: MessagesControllerComponents,
                                                  view: RemoveWarehouseConfirmView
                                                )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  val form = formProvider()

  def onPageLoad(mode: Mode, index: String): Action[AnyContent] =
    (identify andThen getData andThen requireData) {
      implicit request =>

        val preparedForm = request.userAnswers.get(RemoveWarehouseConfirmPage) match {
          case None => form
          case Some(value) => form.fill(value)
        }

        val warehouseList = request.userAnswers.warehouseList
        val warehouseToRemove:Warehouse = warehouseList(index)
        val formattedAddress = s"${warehouseToRemove.tradingName}, ${warehouseToRemove.address.line1}, ${warehouseToRemove.address.line2}, ${warehouseToRemove.address.line3}, ${warehouseToRemove.address.line4}, ${warehouseToRemove.address.postcode}"

        Ok(view(preparedForm, mode, formattedAddress, index))
    }

  def onSubmit(mode: Mode, index: String): Action[AnyContent] =
    (identify andThen getData andThen requireData).async {
      implicit request =>

        val warehouseList = request.userAnswers.warehouseList
        val warehouseToRemove:Warehouse = warehouseList(index)
        val formattedAddress = s"${warehouseToRemove.tradingName}, ${warehouseToRemove.address.line1}, ${warehouseToRemove.address.line2}, ${warehouseToRemove.address.line3}, ${warehouseToRemove.address.line4}, ${warehouseToRemove.address.postcode}"

        form.bindFromRequest().fold(
          formWithErrors =>
            Future.successful(BadRequest(view(formWithErrors, mode, formattedAddress, index))),
          value =>
            if(value) {
              for {
                updatedAnswers <- Future.fromTry(request.userAnswers.set(RemoveWarehouseConfirmPage, value))
                modifiedWarehouseMap = warehouseList.removed(index)
                updatedAnswersFinal = updatedAnswers.copy(warehouseList = modifiedWarehouseMap)
                _ <- sessionRepository.set(updatedAnswersFinal)
              } yield {
                Redirect(navigator.nextPage(SecondaryWarehouseDetailsPage, mode, updatedAnswersFinal))
              }
            }else{
              for {
                updatedAnswers <- Future.fromTry(request.userAnswers.set(RemoveWarehouseConfirmPage, value))
                _ <- sessionRepository.set(updatedAnswers)
              } yield {
                Redirect(navigator.nextPage(SecondaryWarehouseDetailsPage, mode, updatedAnswers))
              }
            }
        )
    }
}
