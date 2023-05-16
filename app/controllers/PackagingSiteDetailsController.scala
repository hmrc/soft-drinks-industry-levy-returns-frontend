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
import forms.PackagingSiteDetailsFormProvider
import models.{Mode, SdilReturn}
import models.backend.Site
import navigation.Navigator
import pages.PackagingSiteDetailsPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.PackagingSiteDetailsView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class PackagingSiteDetailsController @Inject()(
                                                override val messagesApi: MessagesApi,
                                                sessionRepository: SessionRepository,
                                                sdilConnector: SoftDrinksIndustryLevyConnector,
                                                navigator: Navigator,
                                                identify: IdentifierAction,
                                                getData: DataRetrievalAction,
                                                requireData: DataRequiredAction,
                                                formProvider: PackagingSiteDetailsFormProvider,
                                                val controllerComponents: MessagesControllerComponents,
                                                view: PackagingSiteDetailsView
                                              )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>

      request.userAnswers.submitted match {
        case true => Redirect(routes.ReturnSentController.onPageLoad())
        case false =>      val preparedForm = request.userAnswers.get(PackagingSiteDetailsPage) match {
          case None => form
          case Some(value) => form.fill(value)
        }

          val siteList: Map[String, Site] = request.userAnswers.packagingSiteList

          Ok(view(preparedForm, mode, siteList))
      }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      val siteList: Map[String, Site] = request.userAnswers.packagingSiteList

      form.bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors, mode, siteList))),

        value =>
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(PackagingSiteDetailsPage, value))
            _              <- sessionRepository.set(updatedAnswers)
          } yield Redirect(navigator.nextPage(PackagingSiteDetailsPage, mode, updatedAnswers,Some(SdilReturn.apply(updatedAnswers)), Some(request.subscription)))
      )
  }
}
