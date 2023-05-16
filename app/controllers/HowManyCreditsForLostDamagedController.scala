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
import forms.HowManyCreditsForLostDamagedFormProvider
import models.{Mode, SdilReturn}
import navigation.Navigator
import pages.HowManyCreditsForLostDamagedPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.HowManyCreditsForLostDamagedView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class HowManyCreditsForLostDamagedController @Inject()(
                                      override val messagesApi: MessagesApi,
                                      sessionRepository: SessionRepository,
                                      navigator: Navigator,
                                      identify: IdentifierAction,
                                      getData: DataRetrievalAction,
                                      requireData: DataRequiredAction,
                                      formProvider: HowManyCreditsForLostDamagedFormProvider,
                                      val controllerComponents: MessagesControllerComponents,
                                      view: HowManyCreditsForLostDamagedView
                                     )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>

      request.userAnswers.submitted match {
        case true => Redirect(routes.ReturnSentController.onPageLoad())
        case false =>   val preparedForm = request.userAnswers.get(HowManyCreditsForLostDamagedPage) match {
            case None => form
            case Some(value) => form.fill(value)
          }

          Ok(view(preparedForm, mode))
      }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      form.bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors, mode))),
        value =>
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(HowManyCreditsForLostDamagedPage, value))
            _              <- sessionRepository.set(updatedAnswers)
          } yield {
            Redirect(navigator.nextPage(HowManyCreditsForLostDamagedPage, mode, updatedAnswers,
              Some(SdilReturn.apply(updatedAnswers)), Some(request.subscription)))
          }
      )
  }
}
