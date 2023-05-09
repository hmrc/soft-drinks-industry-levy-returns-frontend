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
import forms.RemovePackagingDetailsConfirmationFormProvider
import handlers.ErrorHandler
import models.{Mode, UserAnswers}
import navigation.Navigator
import pages.RemovePackagingDetailsConfirmationPage
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import play.twirl.api.Html
import repositories.SessionRepository
import utilitlies.GenericLogger
import viewmodels.AddressFormattingHelper
import views.html.RemovePackagingDetailsConfirmationView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class RemovePackagingDetailsConfirmationController @Inject()(
                                         override val messagesApi: MessagesApi,
                                         val sessionRepository: SessionRepository,
                                         val navigator: Navigator,
                                         val errorHandler: ErrorHandler,
                                         val genericLogger: GenericLogger,
                                         identify: IdentifierAction,
                                         getData: DataRetrievalAction,
                                         requireData: DataRequiredAction,
                                         formProvider: RemovePackagingDetailsConfirmationFormProvider,
                                         val controllerComponents: MessagesControllerComponents,
                                         view: RemovePackagingDetailsConfirmationView
                                 )(implicit ec: ExecutionContext) extends ControllerHelper {

  private val form = formProvider()

  private def getPackagingSiteAddressBaseOnRef(ref: String, userAnswers: UserAnswers): Option[Html] = {
    userAnswers.packagingSiteList
      .get(ref)
      .map(packagingSite => AddressFormattingHelper.addressFormatting(packagingSite.address, packagingSite.tradingName))
  }

  def onPageLoad(mode: Mode, ref: String): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      getPackagingSiteAddressBaseOnRef(ref, request.userAnswers) match {
        case None =>
          genericLogger.logger.warn(s"user has potentially hit page and ref does not exist for packaging site" +
            s"$ref ${request.userAnswers.id} amount currently: ${request.userAnswers.packagingSiteList.size}")
          Redirect(routes.PackagingSiteDetailsController.onPageLoad(mode))
        case Some(packagingSiteDetails) => Ok(view(form, mode, ref, packagingSiteDetails))
      }
  }

  def onSubmit(mode: Mode, ref: String): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      def removePackagingDetailsFromUserAnswers(userSelection: Boolean, userAnswers: UserAnswers, refOfSite: String): UserAnswers = {
        if (userSelection) {
          userAnswers.copy(packagingSiteList = userAnswers.packagingSiteList.filterNot(_._1 == refOfSite))
        } else {
          userAnswers
        }
      }

      getPackagingSiteAddressBaseOnRef(ref, request.userAnswers) match {
        case None =>
          genericLogger.logger.warn(s"user has potentially submit page and ref does not exist for packaging site" +
            s"$ref ${request.userAnswers.id} amount currently: ${request.userAnswers.packagingSiteList.size}")
          Future.successful(Redirect(routes.PackagingSiteDetailsController.onPageLoad(mode)))
        case Some(packagingSiteDetails) =>
          form.bindFromRequest().fold(
            formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode, ref, packagingSiteDetails))),

            value => {
              val updatedAnswersAfterUserAnswer = removePackagingDetailsFromUserAnswers(value, request.userAnswers, ref)
              setAndRedirect(updatedAnswersAfterUserAnswer, RemovePackagingDetailsConfirmationPage, mode)
            }
         )
      }
  }
}
