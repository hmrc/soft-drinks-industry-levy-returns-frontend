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

import controllers.actions.{ CheckingSubmissionAction, DataRequiredAction, DataRetrievalAction, IdentifierAction }
import forms.RemoveWarehouseConfirmFormProvider
import handlers.ErrorHandler
import models.Mode
import models.backend.Site
import navigation.Navigator
import pages.RemoveWarehouseConfirmPage
import play.api.i18n.MessagesApi
import play.api.mvc.{ Action, AnyContent, MessagesControllerComponents }
import repositories.SessionRepository
import util.GenericLogger
import viewmodels.AddressFormattingHelper
import views.html.RemoveWarehouseConfirmView

import javax.inject.Inject
import scala.concurrent.{ ExecutionContext, Future }

class RemoveWarehouseConfirmController @Inject() (
  override val messagesApi: MessagesApi,
  val sessionRepository: SessionRepository,
  val navigator: Navigator,
  val errorHandler: ErrorHandler,
  val genericLogger: GenericLogger,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  checkReturnSubmission: CheckingSubmissionAction,
  formProvider: RemoveWarehouseConfirmFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: RemoveWarehouseConfirmView)(implicit ec: ExecutionContext) extends ControllerHelper {

  private val form = formProvider()

  def onPageLoad(mode: Mode, index: String): Action[AnyContent] =
    (identify andThen getData andThen requireData andThen checkReturnSubmission) {
      implicit request =>

        request.userAnswers.warehouseList.get(index) match {
          case Some(warehouse) =>
            val formattedAddress = AddressFormattingHelper.addressFormatting(warehouse.address, warehouse.tradingName)
            Ok(view(form, mode, formattedAddress, index))
          case _ =>
            genericLogger.logger.warn(s"Warehouse index $index doesn't exist ${request.userAnswers.id} warehouse list length:" +
              s"${request.userAnswers.warehouseList.size}")
            Redirect(routes.SecondaryWarehouseDetailsController.onPageLoad(mode))
        }
    }

  def onSubmit(mode: Mode, ref: String): Action[AnyContent] =
    (identify andThen getData andThen requireData andThen checkReturnSubmission).async {
      implicit request =>
        val warehouseList: Map[String, Site] = request.userAnswers.warehouseList
        val warehouseToRemove: Site = warehouseList(ref)
        val formattedAddress = AddressFormattingHelper.addressFormatting(warehouseToRemove.address, warehouseToRemove.tradingName)

        form.bindFromRequest().fold(
          formWithErrors =>
            Future.successful(BadRequest(view(formWithErrors, mode, formattedAddress, ref))),
          value =>
            if (value) {
              val updatedAnswers = request.userAnswers
              val modifiedWarehouseMap = warehouseList.removed(ref)
              val updatedAnswersFinal = updatedAnswers.copy(warehouseList = modifiedWarehouseMap)
              setAndRedirect(updatedAnswersFinal, RemoveWarehouseConfirmPage, mode)
            } else {
              val updatedAnswers = request.userAnswers.set(RemoveWarehouseConfirmPage, value)
              updateDatabaseAndRedirect(updatedAnswers, RemoveWarehouseConfirmPage, mode)
            })
    }
}
