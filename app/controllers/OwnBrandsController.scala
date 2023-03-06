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

import javax.inject.Inject
import models.{BrandsPackagedAtOwnSites, CheckMode, Mode, UserAnswers}
import navigation.Navigator
import pages.{BrandsPackagedAtOwnSitesPage, OwnBrandsPage}
import play.api.Logger
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.OwnBrandsView

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

class OwnBrandsController @Inject()(
                                         override val messagesApi: MessagesApi,
                                         sessionRepository: SessionRepository,
                                         navigator: Navigator,
                                         identify: IdentifierAction,
                                         getData: DataRetrievalAction,
                                         formProvider: OwnBrandsFormProvider,
                                         val controllerComponents: MessagesControllerComponents,
                                         view: OwnBrandsView
                                 )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  val form = formProvider()
  val logger: Logger = Logger(this.getClass())

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
        value =>
          for {
            updatedAnswers <- Future.fromTry(answers.set(OwnBrandsPage, value))
            _ <- sessionRepository.set(updatedAnswers)
          } yield {
            if (value) {
              Redirect(navigator.nextPage(OwnBrandsPage, mode, updatedAnswers))
            } else {
              val answersWithLitresRemoved =
                updatedAnswers.remove(BrandsPackagedAtOwnSitesPage) match {
                  case Success(updatedAnswers) => updatedAnswers
                  case Failure(exception) => logger.error(s"Failed to remove value \n ${exception.getMessage}"); updatedAnswers
              }
              sessionRepository.set(answersWithLitresRemoved)
              Redirect(navigator.nextPage(OwnBrandsPage, mode, answersWithLitresRemoved))
            }
          }
      )
  }
}
