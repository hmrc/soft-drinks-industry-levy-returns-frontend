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
import models.{NormalMode, SdilReturn}
import navigation.Navigator
import pages.ReturnChangeRegistrationPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utilitlies.UserTypeCheck
import views.html.ReturnChangeRegistrationView

import javax.inject.Inject


class ReturnChangeRegistrationController @Inject()(
                                       override val messagesApi: MessagesApi,
                                       identify: IdentifierAction,
                                       getData: DataRetrievalAction,
                                       requireData: DataRequiredAction,
                                       checkReturnSubmission: CheckingSubmissionAction,
                                       navigator: Navigator,
                                       val controllerComponents: MessagesControllerComponents,
                                       view: ReturnChangeRegistrationView
                                     ) extends FrontendBaseController with I18nSupport {

  def onPageLoad(): Action[AnyContent] = (identify andThen getData andThen requireData andThen checkReturnSubmission) {
    implicit request =>
      val sdilReturn = SdilReturn.apply(request.userAnswers)
      var urlLink: String = ""
      if (UserTypeCheck.isNewImporter(sdilReturn, request.subscription) && !UserTypeCheck.isNewPacker(sdilReturn, request.subscription) ) {
        urlLink = routes.BroughtIntoUKController.onPageLoad(NormalMode).url
      } else {
        urlLink = routes.PackagedContractPackerController.onPageLoad(NormalMode).url
      }
      Ok(view(urlLink))
  }

  def onSubmit(): Action[AnyContent] = (identify andThen getData andThen requireData andThen checkReturnSubmission) {
    implicit request =>
      Redirect(navigator.nextPage(ReturnChangeRegistrationPage, NormalMode, request.userAnswers,
        Some(SdilReturn.apply(request.userAnswers)),Some(request.subscription) ))
  }
}
