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
import forms.SmallProducerDetailsFormProvider
import viewmodels.checkAnswers.{AddASmallProducerSummary, SmallProducerDetailsSummary}
import views.html.SmallProducerDetailsView
import models.{Mode, SmallProducer}
import navigation.Navigator
import pages.SmallProducerDetailsPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{SummaryList, SummaryListRow}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.govuk.summarylist._

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class SmallProducerDetailsController @Inject()(
                                                override val messagesApi: MessagesApi,
                                                sessionRepository: SessionRepository,
                                                navigator: Navigator,
                                                identify: IdentifierAction,
                                                getData: DataRetrievalAction,
                                                requireData: DataRequiredAction,
                                                formProvider: SmallProducerDetailsFormProvider,
                                                val controllerComponents: MessagesControllerComponents,
                                                view: SmallProducerDetailsView
                                              )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>

      val smallProducerList:List[SmallProducer] = request.userAnswers.smallProducerList
      val preparedForm = request.userAnswers.get(SmallProducerDetailsPage) match {
        case None => form
        case Some(value) => form.fill(value)
      }

      val smallProducersSummaryList: List[SummaryListRow] = SmallProducerDetailsSummary.row2(smallProducerList)
      val list: SummaryList = SummaryListViewModel(
        rows = smallProducersSummaryList
      )

      Ok(view(preparedForm, mode, list))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      val spList = request.userAnswers.smallProducerList
      val smallProducersSummaryList: List[SummaryListRow] = SmallProducerDetailsSummary.row2(spList)
      val list: SummaryList = SummaryListViewModel(
        rows = smallProducersSummaryList
      )
      form.bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors, mode, list))),
        value =>
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(SmallProducerDetailsPage, value))
            _ <- sessionRepository.set(updatedAnswers)
          } yield Redirect(navigator.nextPage(SmallProducerDetailsPage, mode, updatedAnswers))
      )
  }
}