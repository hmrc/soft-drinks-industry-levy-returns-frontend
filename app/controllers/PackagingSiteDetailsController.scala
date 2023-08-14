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
import forms.PackagingSiteDetailsFormProvider
import handlers.ErrorHandler
import models.backend.Site
import models.{Mode, NormalMode, SdilReturn}
import navigation.Navigator
import pages.PackagingSiteDetailsPage
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.{AddressLookupService, PackingDetails}
import utilitlies.{GenericLogger, UserTypeCheck}
import views.html.PackagingSiteDetailsView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class PackagingSiteDetailsController @Inject()(
                                                override val messagesApi: MessagesApi,
                                                val sessionRepository: SessionRepository,
                                                val navigator: Navigator,
                                                val errorHandler: ErrorHandler,
                                                val genericLogger: GenericLogger,
                                                identify: IdentifierAction,
                                                getData: DataRetrievalAction,
                                                requireData: DataRequiredAction,
                                                checkReturnSubmission: CheckingSubmissionAction,
                                                formProvider: PackagingSiteDetailsFormProvider,
                                                val controllerComponents: MessagesControllerComponents,
                                                addressLookupService: AddressLookupService,
                                                view: PackagingSiteDetailsView
                                              )(implicit ec: ExecutionContext) extends ControllerHelper {

  private val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData andThen checkReturnSubmission) {
    implicit request =>

      val preparedForm = request.userAnswers.get(PackagingSiteDetailsPage) match {
          case None => form
          case Some(value) => form.fill(value)
        }

          val siteList: Map[String, Site] = request.userAnswers.packagingSiteList

          Ok(view(preparedForm, mode, siteList))

  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData andThen checkReturnSubmission).async {
    implicit request =>
      val siteList: Map[String, Site] = request.userAnswers.packagingSiteList

      form.bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors, mode, siteList))),

        value =>
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(PackagingSiteDetailsPage, value))
            onwardUrl:String <-
              if(value){
                updateDatabaseWithoutRedirect(updatedAnswers, PackagingSiteDetailsPage).flatMap(_ =>
                addressLookupService.initJourneyAndReturnOnRampUrl(PackingDetails, mode = mode))
              } else {
                updateDatabaseWithoutRedirect(updatedAnswers, PackagingSiteDetailsPage).flatMap(_ =>
                  (Some(SdilReturn.apply(updatedAnswers)), Some(request.subscription)) match {
                    case (Some(sdilReturn), Some(subscription)) =>
                      if (UserTypeCheck.isNewImporter (sdilReturn, subscription) && mode == NormalMode) {
                        Future.successful(routes.AskSecondaryWarehouseInReturnController.onPageLoad(NormalMode).url)
                      } else {
                        Future.successful(routes.CheckYourAnswersController.onPageLoad.url)
                      }
                    case (_, Some(subscription)) =>
                      genericLogger.logger.warn(s"SDIL return not provided for ${subscription.sdilRef}")
                      Future.successful(routes.JourneyRecoveryController.onPageLoad().url)
                    case _ =>
                      genericLogger.logger.warn("SDIL return or subscription not provided for current unknown user")
                      Future.successful(routes.JourneyRecoveryController.onPageLoad().url)
                    }
                )
              }
          } yield {
            Redirect(onwardUrl)
          }
      )
  }
}
