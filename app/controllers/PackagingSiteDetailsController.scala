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
import forms.packagingSiteDetailsFormProvider
import javax.inject.Inject
import models.{Mode, SdilReturn}
import navigation.Navigator
import pages.PackagingSiteDetailsPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.PackagingSiteDetailsView
import models.backend.Site
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{SummaryList, SummaryListRow}
import viewmodels.checkAnswers.packagingSiteDetailsSummary
import viewmodels.govuk.summarylist._
import scala.concurrent.{ExecutionContext, Future}

class PackagingSiteDetailsController @Inject()(
                                                override val messagesApi: MessagesApi,
                                                sessionRepository: SessionRepository,
                                                sdilConnector: SoftDrinksIndustryLevyConnector,
                                                navigator: Navigator,
                                                identify: IdentifierAction,
                                                getData: DataRetrievalAction,
                                                requireData: DataRequiredAction,
                                                formProvider: packagingSiteDetailsFormProvider,
                                                val controllerComponents: MessagesControllerComponents,
                                                view: PackagingSiteDetailsView
                                 )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>

      val packagingSiteList: List[Site] = request.userAnswers.packagingSiteList
      val preparedForm = request.userAnswers.get(PackagingSiteDetailsPage) match {
        case None => form
        case Some(value) => form.fill(value)
      }

      val packagingSiteSummaryAliasList: List[SummaryListRow] = packagingSiteDetailsSummary.row2(packagingSiteList)
      val aliasList: SummaryList = SummaryListViewModel(
        rows = packagingSiteSummaryAliasList
      )

      Ok(view(preparedForm, mode, aliasList))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      val packagingSiteList: List[Site] = request.userAnswers.packagingSiteList
      val packagingSiteSummaryList: List[SummaryListRow] = packagingSiteDetailsSummary.row2(packagingSiteList)
      val siteList: SummaryList = SummaryListViewModel(
        rows = packagingSiteSummaryList
      )
      form.bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors, mode, siteList))),

        value =>
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(PackagingSiteDetailsPage, value))
            _              <- sessionRepository.set(updatedAnswers)
            sdilReturn = SdilReturn.apply(updatedAnswers)
            retrievedSubs <- sdilConnector.retrieveSubscription(request.sdilEnrolment, "sdil")
          } yield Redirect(navigator.nextPage(PackagingSiteDetailsPage, mode, updatedAnswers, Some(sdilReturn), retrievedSubs))
      )
  }
}
