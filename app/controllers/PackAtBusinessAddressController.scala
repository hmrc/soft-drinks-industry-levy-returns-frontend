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
import forms.PackAtBusinessAddressFormProvider
import javax.inject.Inject
import models.Mode
import models.backend.Site
import navigation.Navigator
import pages.PackAtBusinessAddressPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.PackAtBusinessAddressView

import scala.concurrent.{ExecutionContext, Future}


class PackAtBusinessAddressController @Inject()(
                                         override val messagesApi: MessagesApi,
                                         sessionRepository: SessionRepository,
                                         navigator: Navigator,
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
      val businessName = request.subscription.orgName
      val businessAddress = request.subscription.address
      lazy val preparedForm = request.userAnswers.get(PackAtBusinessAddressPage) match {
        case None => form
        case Some(value) => form.fill(value)
      }
      Ok(view(preparedForm, businessName, businessAddress, mode))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      val businessName = request.subscription.orgName
      val businessAddress = request.subscription.address

      val productionSite = Site(
        request.subscription.address,
        Some(request.subscription.sdilRef),
        Some(request.subscription.orgName),
        request.subscription.deregDate,
       )



        form.bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors, businessName, businessAddress, mode))),

        value =>
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(PackAtBusinessAddressPage, value))
            originaProductionSitelList = updatedAnswers.packagingSiteList
            updatedProductionSiteList = if(request.userAnswers.packagingSiteList.isEmpty){updatedAnswers.copy(packagingSiteList = productionSite :: originaProductionSitelList)} else updatedAnswers
            _              <- sessionRepository.set(updatedProductionSiteList)
          } yield Redirect(navigator.nextPage(PackAtBusinessAddressPage, mode, updatedAnswers))
      )
  }
}