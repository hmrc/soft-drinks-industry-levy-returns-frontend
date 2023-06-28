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
import controllers.actions._
import models.NormalMode
import models.requests.IdentifierRequest
import orchestrators.ReturnsOrchestrator
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import service.ReturnResult
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import javax.inject.Inject
import scala.concurrent.ExecutionContext


class ReturnsController @Inject()(returnsOrchestrator: ReturnsOrchestrator,
                                   override val messagesApi: MessagesApi,
                                  config: FrontendAppConfig,
                                   identify: IdentifierAction,
                                   val controllerComponents: MessagesControllerComponents
                                     )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  def onPageLoad(year: Int, quarter: Int, nilReturn: Boolean): Action[AnyContent] = identify.async {
    implicit request =>

      returnsOrchestrator.setupNewReturn(year, quarter, nilReturn).value.map {
        case Right(_) if nilReturn =>
          Redirect(routes.CheckYourAnswersController.onPageLoad)
        case Right(_) if request.subscription.activity.smallProducer =>
          Redirect(routes.PackagedContractPackerController.onPageLoad(NormalMode))
        case Right(_) =>
          Redirect(routes.OwnBrandsController.onPageLoad(NormalMode))
        case Left(_) => Redirect(config.sdilFrontendBaseUrl)
      }
  }
//  private def setupReturn(year: Int, quarter: Int, nilReturn: Boolean)(
//    implicit request: IdentifierRequest[AnyContent], hc: HeaderCarrier, ec: ExecutionContext): ReturnResult[Unit] = {
//   if(config.defaultReturnSetup) {
//     returnsOrchestrator.tempSetupReturnTest
//   } else {
//     returnsOrchestrator.setupNewReturn(year, quarter, nilReturn)
//}
//  }
}
