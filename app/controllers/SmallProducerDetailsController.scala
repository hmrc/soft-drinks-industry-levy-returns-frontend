/*
 * Copyright 2022 HM Revenue & Customs
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

import javax.inject.Inject
import models.{Mode, SmallProducer, UserAnswers}
import navigation.Navigator
import pages.SmallProducerDetailsPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{SummaryList, SummaryListRow}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.checkAnswers.SmallProducerDetailsSummary
import views.html.SmallProducerDetailsView
import viewmodels.govuk.summarylist._


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

  val spList = List(SmallProducer("ABC Ltd", "SDIL123456", (1000L, 1000L)),
    SmallProducer("XYZ Ltd", "SDIL123789", (1000L, 1000L)))

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData) {
    implicit request =>
      val smallProducersSummaryList: List[SummaryListRow] = SmallProducerDetailsSummary.row2(spList)
      val list: SummaryList = SummaryListViewModel(
        rows = smallProducersSummaryList
      )
      val preparedForm = request.userAnswers.flatMap(_.get(SmallProducerDetailsPage)) match {
        case None => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, mode, list))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData).async {
    implicit request =>
    val answers = request.userAnswers.getOrElse(UserAnswers(id = request.sdilEnrolment))
      val smallProducersSummaryList: List[SummaryListRow] = SmallProducerDetailsSummary.row2(spList)
      val list: SummaryList = SummaryListViewModel(
        rows = smallProducersSummaryList
      )
      form.bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors, mode, list))),

        value =>
          for {
            updatedAnswers <- Future.fromTry(answers.set(SmallProducerDetailsPage, value))
            _              <- sessionRepository.set(updatedAnswers)
          } yield Redirect(navigator.nextPage(SmallProducerDetailsPage, mode, updatedAnswers))
      )
  }
}
