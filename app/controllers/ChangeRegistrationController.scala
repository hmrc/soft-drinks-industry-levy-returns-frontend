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

import config.FrontendAppConfig
import connectors.SoftDrinksIndustryLevyConnector
import controllers.actions._
import models.{NormalMode, SdilReturn}
import navigation.Navigator
import javax.inject.Inject
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.ChangeRegistrationView

class ChangeRegistrationController @Inject()(
                                       override val messagesApi: MessagesApi,
                                       identify: IdentifierAction,
                                       getData: DataRetrievalAction,
                                       requireData: DataRequiredAction,
                                       sdilConnector: SoftDrinksIndustryLevyConnector,
                                       navigator: Navigator,
                                       val controllerComponents: MessagesControllerComponents,
                                       view: ChangeRegistrationView
                                     )(implicit config: FrontendAppConfig) extends FrontendBaseController with I18nSupport {

  def onPageLoad: Action[AnyContent] = (identify andThen getData) {
    implicit request =>
      val linkCall = if (request.subscription.productionSites.isEmpty) {
        routes.PackagedContractPackerController.onPageLoad(NormalMode)
      } else routes.BroughtIntoUKController.onPageLoad(NormalMode)
      Ok(view(linkCall))
  }

  def onSubmit(): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      val answers = request.userAnswers
      val sdilReturn = SdilReturn.apply(answers)
      val subscription = request.subscription
      val isNewPacker = (sdilReturn.totalPacked._1 > 0L || sdilReturn.totalPacked._2 > 0L) && !subscription.activity.contractPacker // TODO to be refactored when we have a common helper
      if(isNewPacker) {
        Redirect(routes.PackAtBusinessAddressController.onPageLoad(NormalMode))
      }else {
        Redirect(routes.AskSecondaryWarehouseInReturnController.onPageLoad(NormalMode))
      }
  }
}
