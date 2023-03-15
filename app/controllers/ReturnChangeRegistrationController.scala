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
import models.retrieved.RetrievedSubscription
import models.{NormalMode, SdilReturn, UserAnswers}
import navigation.Navigator
import pages.{AskSecondaryWarehouseInReturnPage, PackAtBusinessAddressPage, PackagedContractPackerPage}

import javax.inject.Inject
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.ReturnChangeRegistrationView

import scala.concurrent.Await
import scala.concurrent.duration.DurationInt


class ReturnChangeRegistrationController @Inject()(
                                       override val messagesApi: MessagesApi,
                                       identify: IdentifierAction,
                                       getData: DataRetrievalAction,
                                       requireData: DataRequiredAction,
                                       sdilConnector: SoftDrinksIndustryLevyConnector,
                                       navigator: Navigator,
                                       val controllerComponents: MessagesControllerComponents,
                                       view: ReturnChangeRegistrationView
                                     ) extends FrontendBaseController with I18nSupport {

  def onPageLoad: Action[AnyContent] = (identify andThen getData) {
    implicit request =>
      Ok(view())
  }

  def onSubmit(): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      val answers = request.userAnswers
      val sdilReturn = SdilReturn.apply(answers)
      val subscription = Await.result(sdilConnector.retrieveSubscription(request.sdilEnrolment, "sdil"),4.seconds)
      val isNewImporter = (sdilReturn.totalImported._1 > 0L && sdilReturn.totalImported._2 > 0L) && !subscription.get.activity.importer
      if(!isNewImporter) {
        Redirect(routes.PackAtBusinessAddressController.onPageLoad(NormalMode)) //TODO CHECK IF USER IS NEW PACKER
      }else {
        Redirect(routes.AskSecondaryWarehouseInReturnController.onPageLoad(NormalMode))
      }
  }
}
