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

import javax.inject.Inject
import models.{Mode, NormalMode, UserAnswers}
import navigation.Navigator
import pages.RemovePackagingDetailsConfirmationPage
import play.api.Logger
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import play.twirl.api.Html
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.AddressFormattingHelper
import views.html.RemovePackagingDetailsConfirmationView

import scala.concurrent.{ExecutionContext, Future}

class RemovePackagingDetailsConfirmationController @Inject()(
                                         override val messagesApi: MessagesApi,
                                         sessionRepository: SessionRepository,
                                         navigator: Navigator,
                                         identify: IdentifierAction,
                                         getData: DataRetrievalAction,
                                         requireData: DataRequiredAction,
                                         formProvider: RemovePackagingDetailsConfirmationFormProvider,
                                         val controllerComponents: MessagesControllerComponents,
                                         view: RemovePackagingDetailsConfirmationView
                                 )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  val form = formProvider()
  val logger = Logger(this.getClass)

  private def getPackagingSiteAddressBaseOnRef(ref: String, userAnswers: UserAnswers): Option[Html] = {
    userAnswers.packagingSiteList
      .get(ref)
      .map(packagingSite => AddressFormattingHelper.addressFormatting(packagingSite.address, packagingSite.tradingName))
  }

  def onPageLoad(mode: Mode, ref: String): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      getPackagingSiteAddressBaseOnRef(ref, request.userAnswers) match {
        case None =>
          logger.warn(s"user has potentially hit page and ref does not exist for packaging site $ref ${request.userAnswers.id} amount currently: ${request.userAnswers.packagingSiteList.size}")
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
          logger.warn(s"user has potentially submit page and ref does not exist for packaging site $ref ${request.userAnswers.id} amount currently: ${request.userAnswers.packagingSiteList.size}")
          Future.successful(Redirect(routes.PackagingSiteDetailsController.onPageLoad(mode)))
        case Some(packagingSiteDetails) =>
          form.bindFromRequest().fold(
            formWithErrors =>
              Future.successful(BadRequest(view(formWithErrors, mode, ref, packagingSiteDetails))),
            value =>
              for {
                updatedAnswersAfterUserAnswer <- Future.successful(removePackagingDetailsFromUserAnswers(value, request.userAnswers, ref))
                _ <- sessionRepository.set(updatedAnswersAfterUserAnswer)
              } yield Redirect(navigator.nextPage(RemovePackagingDetailsConfirmationPage, mode, updatedAnswersAfterUserAnswer))
          )
      }
  }
}