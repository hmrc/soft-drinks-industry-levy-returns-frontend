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

import com.google.inject.Inject
import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.govuk.summarylist._
import views.html.CheckYourAnswersView

class CheckYourAnswersController @Inject()(
                                            override val messagesApi: MessagesApi,
                                            identify: IdentifierAction,
                                            getData: DataRetrievalAction,
                                            requireData: DataRequiredAction,
                                            val controllerComponents: MessagesControllerComponents,
                                            view: CheckYourAnswersView
                                          ) extends FrontendBaseController with I18nSupport {

  def onPageLoad(): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>

      val returnPeriod = request.returnPeriod match {
        case Some(returnPeriod) =>
          val year = returnPeriod.year
          returnPeriod.quarter match {
            case 0 => s"${Messages("firstQuarter")} $year"
            case 1 => s"${Messages("secondQuarter")} $year"
            case 2 => s"${Messages("thirdQuarter")} $year"
            case 3 => s"${Messages("fourthQuarter")} $year"
            case _ => throw new RuntimeException("Invalid return period quarter")
          }
        case None => throw new RuntimeException("No return period returned")
      }

      val list = SummaryListViewModel(
        rows = Seq.empty
      )

     Ok(view(list, request.orgName, returnPeriod))

  }
}
