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
import forms.PackAtBusinessAddressFormProvider

import javax.inject.Inject
import models.Mode
import models.backend.UkAddress
import navigation.Navigator
import pages.PackAtBusinessAddressPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.PackAtBusinessAddressView

import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.{Failure, Success}

class PackAtBusinessAddressController @Inject()(
                                         override val messagesApi: MessagesApi,
                                         sessionRepository: SessionRepository,
                                         navigator: Navigator,
                                         connector: SoftDrinksIndustryLevyConnector,
                                         identify: IdentifierAction,
                                         getData: DataRetrievalAction,
                                         requireData: DataRequiredAction,
                                         formProvider: PackAtBusinessAddressFormProvider,
                                         val controllerComponents: MessagesControllerComponents,
                                         view: PackAtBusinessAddressView
                                 )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      val subscription = Await.result(connector.retrieveSubscription(request.userAnswers.id,"sdil"),1.seconds)

      val businessName = subscription.get.orgName
      val businessAddress = subscription.get.address
      val preparedForm = request.userAnswers.get(PackAtBusinessAddressPage) match {
        case None => form
        case Some(value) => form.fill(value)
      }
      Ok(view(preparedForm, businessName, businessAddress, mode))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      val subscription = Await.result(connector.retrieveSubscription(request.userAnswers.id, "sdil"), 1.seconds)

      val businessName = subscription.get.orgName
      val businessAddress = subscription.get.address

      form.bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors, businessName: String, businessAddress: UkAddress, mode))),

        value =>
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(PackAtBusinessAddressPage, value))
            _              <- sessionRepository.set(updatedAnswers)
          } yield Redirect(navigator.nextPage(PackAtBusinessAddressPage, mode, updatedAnswers))
      )
  }
  private def getSubscription(sdilRef: String, identifier: String) (implicit hc: HeaderCarrier) {
    val subscription = Await.result(connector.retrieveSubscription(sdilRef,"sdil"),1.seconds)
  }
}