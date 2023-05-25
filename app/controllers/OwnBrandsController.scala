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
import errors.NoPendingReturnForGivenPeriod
import forms.OwnBrandsFormProvider
import handlers.ErrorHandler
import models.requests.OptionalDataRequest
import models.{CheckMode, Mode, NormalMode, UserAnswers}
import navigation.Navigator
import orchestrators.ReturnsOrchestrator
import pages.{BrandsPackagedAtOwnSitesPage, OwnBrandsPage}
import play.api.data.Form
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import repositories.SessionRepository
import uk.gov.hmrc.http.HeaderCarrier
import utilitlies.GenericLogger
import views.html.OwnBrandsView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class OwnBrandsController @Inject()(returnsOrchestrator: ReturnsOrchestrator,
                                     override val messagesApi: MessagesApi,
                                     val sessionRepository: SessionRepository,
                                     val navigator: Navigator,
                                     val errorHandler: ErrorHandler,
                                     val genericLogger: GenericLogger,
                                     identify: IdentifierAction,
                                     getData: DataRetrievalAction,
//                                     requireData: DataRequiredAction,
//                                     checkReturnSubmission: CheckingSubmissionAction,
                                     formProvider: OwnBrandsFormProvider,
                                     val controllerComponents: MessagesControllerComponents,
                                     view: OwnBrandsView,
                                     config: FrontendAppConfig
                                   )(implicit ec: ExecutionContext) extends ControllerHelper {

  private val form = formProvider()
 //Todo use this function once ATs route to the ReturnsController from dashboard and use required data and checkSubmission actions
//  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData andThen checkReturnSubmission) {
//    implicit request =>
//      val preparedForm = request.userAnswers.get(OwnBrandsPage) match {
//        case None => form
//        case Some(value) => form.fill(value)
//      }
//      Ok(view(preparedForm, mode))
//  }


  //ToDo remove this function once ATs route to the ReturnsController from dashboard and use required data and checkSubmission actions

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData).async {
    implicit request =>
      val preparedForm = request.userAnswers.fold[Form[Boolean]](form) { ua =>
        ua.get(OwnBrandsPage) match {
          case Some(value) if mode == CheckMode => form.fill(value)
          case _ => form
        }
      }
      request.returnPeriod match {
        case Some(_) if mode == NormalMode =>
          request.userAnswers.getOrElse(UserAnswers(id = request.sdilEnrolment)).submitted match {
            case true => Future.successful(Redirect(routes.ReturnSentController.onPageLoad))
            case false => Future.successful(Ok(view(preparedForm, mode)))
          }
        case _ => handleTempLandingPage(form)
      }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData).async {
    implicit request =>
      val answers = request.userAnswers.getOrElse(UserAnswers(id = request.sdilEnrolment))
      form.bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors, mode))),
        value => {
          val updatedUserAnswers = answers.setAndRemoveLitresIfReq(OwnBrandsPage, BrandsPackagedAtOwnSitesPage, value)
          updateDatabaseAndRedirect(updatedUserAnswers, OwnBrandsPage, mode)
        }
      )
  }

  //ToDo remove this function once ATs route to the ReturnsController from dashboard and use required data and checkSubmission actions
  private def handleTempLandingPage(form: Form[Boolean])
                                   (implicit request: OptionalDataRequest[AnyContent], hc: HeaderCarrier, ec: ExecutionContext): Future[Result] = {
    returnsOrchestrator.tempSetupReturn.value.map{
      case Right(_) => Ok(view(form, NormalMode))
      case Left(NoPendingReturnForGivenPeriod) => Redirect(config.sdilFrontendBaseUrl)
      case Left(_) => InternalServerError(errorHandler.internalServerErrorTemplate)
    }
  }
}
