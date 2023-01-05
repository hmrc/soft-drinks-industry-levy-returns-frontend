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
import forms.SecondaryWarehouseDetailsFormProvider

import javax.inject.Inject
import models.{Address, Mode, UserAnswers, Warehouse}
import navigation.Navigator
import pages.SecondaryWarehouseDetailsPage
import play.api.http.Writeable.wByteArray
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{SummaryList, SummaryListRow}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.checkAnswers.SecondaryWarehouseDetailsSummary
import views.html.SecondaryWarehouseDetailsView
import viewmodels.govuk.summarylist._

import scala.concurrent.{ExecutionContext, Future}

class SecondaryWarehouseDetailsController @Inject()(
                                         override val messagesApi: MessagesApi,
                                         sessionRepository: SessionRepository,
                                         navigator: Navigator,
                                         identify: IdentifierAction,
                                         getData: DataRetrievalAction,
                                         requireData: DataRequiredAction,
                                         formProvider: SecondaryWarehouseDetailsFormProvider,
                                         val controllerComponents: MessagesControllerComponents,
                                         view: SecondaryWarehouseDetailsView
                                 )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  val form = formProvider()

  val spList = List(Warehouse("ABC Ltd", Address("33 Rhes Priordy", "East London","Line 3","Line 4","WR53 7CX")),Warehouse("Super Cola Ltd", Address("33 Rhes Priordy", "East London","Line 3","","SA13 7CE")))

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData) {
    implicit request =>
      val warehouseSummaryList: List[SummaryListRow] = SecondaryWarehouseDetailsSummary.row2(spList)
      val list: List[Warehouse] = spList
      val preparedForm = request.userAnswers.flatMap(_.get(SecondaryWarehouseDetailsPage)) match {
        case None => form
        case Some(value) => form.fill(value)
      }
      Ok(view(preparedForm, mode,list))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData).async {
    implicit request =>
      val warehouseSummaryList: List[SummaryListRow] = SecondaryWarehouseDetailsSummary.row2(spList)
      val list: List[Warehouse] = spList
      val answers = request.userAnswers.getOrElse(UserAnswers(id = request.sdilEnrolment))
      form.bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors, mode, list))),
        value =>
          for {
            updatedAnswers <- Future.fromTry(answers.set(SecondaryWarehouseDetailsPage, value))
            _              <- sessionRepository.set(updatedAnswers)
          } yield Redirect(navigator.nextPage(SecondaryWarehouseDetailsPage, mode, updatedAnswers))
      )
  }
}
