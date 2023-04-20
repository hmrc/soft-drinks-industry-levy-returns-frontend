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
import models.{Mode, NormalMode, UserAnswers, Warehouse}
import navigation.Navigator
import pages.RemoveWarehouseConfirmPage
import play.api.Logger
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import play.twirl.api.Html
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
  val logger: Logger = Logger(this.getClass())

  def onPageLoad(mode: Mode, index: String): Action[AnyContent] =
    (identify andThen getData andThen requireData) {
      implicit request =>
        request.userAnswers.warehouseList.get(index) match {
           case Some(warehouse) =>
             val formattedAddress = s"${warehouse.tradingName}," +
             s" ${warehouse.address.line1}," +
             s" ${warehouse.address.line2}," +
             s" ${warehouse.address.line3}," +
             s" ${warehouse.address.line4}," +
             s" ${warehouse.address.postcode}"
             Ok(view(form, mode, formattedAddress, index))
           case _ => logger.warn(s"Warhouse index $index doesn't exist ${request.userAnswers.id} warehouse list length: ${request.userAnswers.warehouseList.size}")

            Redirect(routes.SecondaryWarehouseDetailsController.onPageLoad(mode))
         }



    }

  def onSubmit(mode: Mode, ref: String): Action[AnyContent] =
    (identify andThen getData andThen requireData).async {
      implicit request =>
        val warehouseList: Map[String, Warehouse] = request.userAnswers.warehouseList
        val warehouseToRemove:Warehouse = warehouseList(ref)
        val formattedAddress = s"${warehouseToRemove.tradingName}, ${warehouseToRemove.address.line1}, ${warehouseToRemove.address.line2}, ${warehouseToRemove.address.line3}, ${warehouseToRemove.address.line4}, ${warehouseToRemove.address.postcode}"

        form.bindFromRequest().fold(
          formWithErrors =>
            Future.successful(BadRequest(view(formWithErrors, mode, formattedAddress,ref))),
          value =>
            if(value) {
              for {
                updatedAnswers <- Future.fromTry(request.userAnswers.set(RemoveWarehouseConfirmPage, value))
                modifiedWarehouseMap = warehouseList.removed(ref)
                updatedAnswersFinal = updatedAnswers.copy(warehouseList = modifiedWarehouseMap)
                _ <- sessionRepository.set(updatedAnswersFinal)
              } yield {
                Redirect(navigator.nextPage(RemoveWarehouseConfirmPage, mode, updatedAnswersFinal))
              }
            } else{
              for {
                updatedAnswers <- Future.fromTry(request.userAnswers.set(RemoveWarehouseConfirmPage, value))
                _ <- sessionRepository.set(updatedAnswers)
              } yield {
                Redirect(navigator.nextPage(RemoveWarehouseConfirmPage, mode, updatedAnswers))
              }
            }
        )
    }
}