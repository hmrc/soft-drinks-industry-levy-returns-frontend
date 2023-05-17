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
import forms._

import javax.inject.Inject
import models.Mode
import navigation.Navigator
import pages.{RemoveSmallProducerConfirmPage, SmallProducerDetailsPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.RemoveSmallProducerConfirmView

import scala.concurrent.{ExecutionContext, Future}


class RemoveSmallProducerConfirmController @Inject()(
                                                     override val messagesApi: MessagesApi,
                                                     sessionRepository: SessionRepository,
                                                     navigator: Navigator,
                                                     identify: IdentifierAction,
                                                     getData: DataRetrievalAction,
                                                     requireData: DataRequiredAction,
                                                     checkReturnSubmission: CheckingSubmissionAction,
                                                     formProvider: RemoveSmallProducerConfirmFormProvider,
                                                     val controllerComponents: MessagesControllerComponents,
                                                     view: RemoveSmallProducerConfirmView
                                 )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  val form = formProvider()

  def onPageLoad(mode: Mode, sdil: String): Action[AnyContent] = (identify andThen getData andThen requireData andThen checkReturnSubmission) {
    implicit request =>

      val preparedForm = request.userAnswers.get(RemoveSmallProducerConfirmPage) match {
        case None => form
        case Some(value) => form.fill(value)
      }

      val smallProducerList = request.userAnswers.smallProducerList
      val smallProducerMissing = smallProducerList.filter(producer => producer.sdilRef == sdil).isEmpty

      if(smallProducerMissing && !smallProducerList.isEmpty){
        Redirect(navigator.nextPage(SmallProducerDetailsPage, mode, request.userAnswers, smallProducerMissing = Some(smallProducerMissing)))
      }else{
        val smallProducerName = smallProducerList.filter(x => x.sdilRef == sdil).map(producer => producer.alias).head
        Ok(view(preparedForm, mode, sdil, smallProducerName))
      }

  }

  def onSubmit(mode: Mode, sdil: String): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      val smallProducerName = request.userAnswers.smallProducerList.filter(x => x.sdilRef == sdil).map(producer => producer.alias).head
      form.bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors, mode, sdil, smallProducerName))),
        formData =>{
          if(formData){
            for {
              updatedAnswers <- Future.fromTry(request.userAnswers.set(RemoveSmallProducerConfirmPage, formData))
              modifiedProducerList = request.userAnswers.smallProducerList.filterNot(producer => producer.sdilRef == sdil)
              updatedAnswersFinal = updatedAnswers.copy(smallProducerList = modifiedProducerList)
              _ <- sessionRepository.set(updatedAnswersFinal)
            } yield {
              Redirect(navigator.nextPage(RemoveSmallProducerConfirmPage, mode, updatedAnswersFinal))
            }
          } else {
            for {
              updatedAnswers <- Future.fromTry(request.userAnswers.set(RemoveSmallProducerConfirmPage, formData))
              _ <- sessionRepository.set(updatedAnswers)
            } yield {
              Redirect(navigator.nextPage(RemoveSmallProducerConfirmPage, mode, updatedAnswers))
            }
          }
          }
      )
  }
}

