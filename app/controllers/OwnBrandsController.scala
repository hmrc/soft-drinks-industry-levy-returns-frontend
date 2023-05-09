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
import forms.OwnBrandsFormProvider
import handlers.ErrorHandler
import models.{Mode, UserAnswers}
import navigation.Navigator
import pages.{BrandsPackagedAtOwnSitesPage, OwnBrandsPage}
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import utilitlies.GenericLogger
import views.html.OwnBrandsView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class OwnBrandsController @Inject()(
                                     override val messagesApi: MessagesApi,
                                     val sessionRepository: SessionRepository,
                                     val navigator: Navigator,
                                     val errorHandler: ErrorHandler,
                                     val genericLogger: GenericLogger,
                                     identify: IdentifierAction,
                                     getData: DataRetrievalAction,
                                     formProvider: OwnBrandsFormProvider,
                                     val controllerComponents: MessagesControllerComponents,
                                     view: OwnBrandsView
                                   )(implicit ec: ExecutionContext) extends ControllerHelper {

  private val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData) {
    implicit request =>

      val preparedForm = request.userAnswers.flatMap(_.get(OwnBrandsPage)) match {
        case None => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, mode))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData).async {
    implicit request =>
      val answers = request.userAnswers.getOrElse(UserAnswers(id = request.sdilEnrolment))
      form.bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors, mode))),
        value => {
          val updatedUserAnswers = answers.setAndRemoveLitresIfReq(OwnBrandsPage, BrandsPackagedAtOwnSitesPage, value)
          updateDatabaseAndRedirect(updatedUserAnswers, OwnBrandsPage, mode)
        }
      )
  }
}
