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

import connectors.SoftDrinksIndustryLevyConnector
import controllers.actions._
import forms.ClaimCreditsForLostDamagedFormProvider

import javax.inject.Inject
import models.{Mode, SdilReturn}
import navigation.Navigator
import pages.{ClaimCreditsForExportsPage, ClaimCreditsForLostDamagedPage, HowManyCreditsForExportPage, HowManyCreditsForLostDamagedPage}
import play.api.Logger
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.ClaimCreditsForLostDamagedView

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

class ClaimCreditsForLostDamagedController @Inject()(
                                         override val messagesApi: MessagesApi,
                                         sessionRepository: SessionRepository,
                                         sdilConnector: SoftDrinksIndustryLevyConnector,
                                         navigator: Navigator,
                                         identify: IdentifierAction,
                                         getData: DataRetrievalAction,
                                         requireData: DataRequiredAction,
                                         formProvider: ClaimCreditsForLostDamagedFormProvider,
                                         val controllerComponents: MessagesControllerComponents,
                                         view: ClaimCreditsForLostDamagedView
                                 )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  val form = formProvider()
  val logger: Logger = Logger(this.getClass())

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>

      val preparedForm = request.userAnswers.get(ClaimCreditsForLostDamagedPage) match {
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
          (for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(ClaimCreditsForLostDamagedPage, value))
            _ <- sessionRepository.set(updatedAnswers)
          } yield updatedAnswers).flatMap { updatedAnswers =>
            val sdilReturn = SdilReturn.apply(updatedAnswers)
            if (value) {
              Future.successful(Redirect(navigator.nextPage(
                ClaimCreditsForLostDamagedPage, mode, updatedAnswers,
                Some(sdilReturn), Some(request.subscription))))
            } else {
              Future.fromTry(updatedAnswers.remove(HowManyCreditsForLostDamagedPage)).flatMap {
                updatedAnswers =>
                  sessionRepository.set(updatedAnswers).map {
                    _ =>
                      Redirect(navigator.nextPage(
                        ClaimCreditsForLostDamagedPage, mode, updatedAnswers,
                        Some(sdilReturn), Some(request.subscription)))
                  }
              }
            }
          }
      )
  }
}
