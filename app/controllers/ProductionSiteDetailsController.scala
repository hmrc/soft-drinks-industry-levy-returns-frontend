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
import forms.productionSiteDetailsFormProvider

import javax.inject.Inject
import models.{Mode, ProductionSite, SdilReturn}
import navigation.Navigator
import pages.ProductionSiteDetailsPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.productionSiteDetailsView
import models.backend.{Site, UkAddress}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{SummaryList, SummaryListRow}
import viewmodels.checkAnswers.{SmallProducerDetailsSummary, productionSiteDetailsSummary}
import viewmodels.govuk.summarylist._

import java.time.LocalDate
import scala.concurrent.{ExecutionContext, Future}

class ProductionSiteDetailsController @Inject()(
                                         override val messagesApi: MessagesApi,
                                         sessionRepository: SessionRepository,
                                         sdilConnector: SoftDrinksIndustryLevyConnector,
                                         navigator: Navigator,
                                         identify: IdentifierAction,
                                         getData: DataRetrievalAction,
                                         requireData: DataRequiredAction,
                                         formProvider: productionSiteDetailsFormProvider,
                                         val controllerComponents: MessagesControllerComponents,
                                         view: productionSiteDetailsView
                                 )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  val form = formProvider()

  val superCola = Site(
    UkAddress(List("33 Rhes Priordy", "East London"), "E73 2RP"),
    Some("88"),
    Some("Wild Lemonade Group"),
    Some(LocalDate.of(2018, 2, 26)))

  val sparkyJuice = Site(
    UkAddress(List("30 Rhes Priordy", "East London"), "E73 2RP"),
    Some("10"),
    Some("Sparky Juice Co"),
    Some(LocalDate.of(2018, 2, 26)))

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>

      val producetionSiteList: List[Site] = request.userAnswers.productionSiteList//request.userAnswers.productionSiteList
      val preparedForm = request.userAnswers.get(ProductionSiteDetailsPage) match {
        case None => form
        case Some(value) => form.fill(value)
      }

      val producetionSiteSummaryAliasList: List[SummaryListRow] = productionSiteDetailsSummary.row2(producetionSiteList)
      val aliasList: SummaryList = SummaryListViewModel(
        rows = producetionSiteSummaryAliasList
      )

      Ok(view(preparedForm, mode, aliasList))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      val producetionSiteList: List[Site] = request.userAnswers.productionSiteList
      val producetionSiteSummaryList: List[SummaryListRow] = productionSiteDetailsSummary.row2(producetionSiteList)
      val siteList: SummaryList = SummaryListViewModel(
        rows = producetionSiteSummaryList
      )
      form.bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors, mode, siteList))),

        value =>
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(ProductionSiteDetailsPage, value))
            _              <- sessionRepository.set(updatedAnswers)
            sdilReturn = SdilReturn.apply(updatedAnswers)
            retrievedSubs <- sdilConnector.retrieveSubscription(request.sdilEnrolment, "sdil")
          } yield Redirect(navigator.nextPage(ProductionSiteDetailsPage, mode, updatedAnswers, Some(sdilReturn), retrievedSubs))
      )
  }
}
