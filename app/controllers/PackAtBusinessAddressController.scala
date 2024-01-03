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
import handlers.ErrorHandler
import models.backend.Site
import models.{ Mode, SdilReturn }
import navigation.Navigator
import pages.PackAtBusinessAddressPage
import play.api.i18n.MessagesApi
import play.api.mvc.{ Action, AnyContent, MessagesControllerComponents }
import repositories.SessionRepository
import services.{ AddressLookupService, PackingDetails }
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import utilitlies.{ AddressHelper, GenericLogger, UserTypeCheck }
import viewmodels.AddressFormattingHelper
import views.html.PackAtBusinessAddressView

import javax.inject.Inject
import scala.concurrent.{ ExecutionContext, Future }

class PackAtBusinessAddressController @Inject() (
  override val messagesApi: MessagesApi,
  val sessionRepository: SessionRepository,
  val navigator: Navigator,
  val errorHandler: ErrorHandler,
  val genericLogger: GenericLogger,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  addressLookupService: AddressLookupService,
  checkReturnSubmission: CheckingSubmissionAction,
  formProvider: PackAtBusinessAddressFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: PackAtBusinessAddressView)(implicit ec: ExecutionContext) extends ControllerHelper with AddressHelper {

  private val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData andThen checkReturnSubmission) {
    implicit request =>
      val formattedAddress: HtmlContent = AddressFormattingHelper.formatBusinessAddress(request.subscription.address, Some(request.subscription.orgName))
      val newPacker = UserTypeCheck.isNewPacker(SdilReturn.apply(request.userAnswers), request.subscription)
      val noExistingProductionSites = request.subscription.productionSites.isEmpty

      lazy val preparedForm = request.userAnswers.get(PackAtBusinessAddressPage) match {
        case None => form
        case Some(_) if newPacker && noExistingProductionSites => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, formattedAddress, mode))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData andThen checkReturnSubmission).async {
    implicit request =>
      val formattedAddress: HtmlContent = AddressFormattingHelper.formatBusinessAddress(request.subscription.address, Some(request.subscription.orgName))

      form.bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors, formattedAddress, mode))),

        value =>
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(PackAtBusinessAddressPage, value))
            onwardUrl <- if (value) {
              updateDatabaseWithoutRedirect(updatedAnswers.copy(
                packagingSiteList = updatedAnswers.packagingSiteList ++ Map(
                  generateId ->
                    Site(
                      address = request.subscription.address,
                      ref = None,
                      tradingName = Some(request.subscription.orgName),
                      closureDate = None))), PackAtBusinessAddressPage).flatMap(_ =>
                Future.successful(routes.PackagingSiteDetailsController.onPageLoad(mode).url))
            } else {
              updateDatabaseWithoutRedirect(updatedAnswers, PackAtBusinessAddressPage).flatMap(_ =>
                addressLookupService.initJourneyAndReturnOnRampUrl(PackingDetails, mode = mode))
            }
          } yield {
            Redirect(onwardUrl)
          })
  }
}