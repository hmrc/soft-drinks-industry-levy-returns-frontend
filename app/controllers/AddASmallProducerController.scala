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
import models.requests.DataRequest
import controllers.actions._
import forms.AddASmallProducerFormProvider

import javax.inject.Inject
import models.{AddASmallProducer, BlankMode, Mode, NormalMode, SmallProducer}
import navigation.Navigator
import pages.{AddASmallProducerPage, SmallProducerDetailsPage}
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.AddASmallProducerView

import scala.concurrent.{ExecutionContext, Future}

class AddASmallProducerController @Inject()(
                                      override val messagesApi: MessagesApi,
                                      sessionRepository: SessionRepository,
                                      sdilConnector: SoftDrinksIndustryLevyConnector,
                                      navigator: Navigator,
                                      identify: IdentifierAction,
                                      getData: DataRetrievalAction,
                                      requireData: DataRequiredAction,
                                      formProvider: AddASmallProducerFormProvider,
                                      val controllerComponents: MessagesControllerComponents,
                                      view: AddASmallProducerView
                                     )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {




  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request: DataRequest[AnyContent] =>
      val form: Form[AddASmallProducer] = formProvider(sessionRepository,sdilConnector)
       mode match {
        case BlankMode => Ok(view(form, NormalMode))
        case _ =>
            val preparedForm = request.userAnswers.get(AddASmallProducerPage) match {
              case None => form
              case Some(value) => form.fill(value)
            }
            Ok(view(preparedForm, mode))


      }


  }

  def onEditPageLoad(mode: Mode, sdil: String): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request: DataRequest[AnyContent] =>
      val form: Form[AddASmallProducer] = formProvider(sessionRepository,sdilConnector, Some(mode), Some(sdil))
      val preparedForm = {
              val sp = request.userAnswers.smallProducerList.filter(_.sdilRef == sdil).headOption
              val v: AddASmallProducer = sp.fold(sys.error(" no elemet present"))(value =>
                AddASmallProducer(Some(value.alias), value.sdilRef, value.litreage._1, value.litreage._2))
              form.fill(v)
            }
            Ok(view(preparedForm, mode, Some(sdil)))
  }

  def onEditPageSubmit(mode: Mode, sdil: String): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      val form = formProvider(sessionRepository,sdilConnector, Some(mode), Some(sdil))
      form.bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors, mode, Some(sdil)))),
        value => {
          val smallProducer: SmallProducer =
            SmallProducer(value.producerName.getOrElse(""),
              value.referenceNumber,
              (value.lowBand, value.highBand))
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(AddASmallProducerPage, value))
            updatedList = request.userAnswers.smallProducerList.filterNot(producer => producer.sdilRef == sdil)
            updatedAnswersFinal = {updatedAnswers.copy(smallProducerList = smallProducer :: updatedList)}
            _              <- sessionRepository.set(updatedAnswersFinal)
          } yield {
            Redirect(navigator.nextPage(AddASmallProducerPage, mode, updatedAnswersFinal))
          }
        }

      )

  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      val form = formProvider(sessionRepository,sdilConnector)

      form.bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors, mode))),
        value => {
          val smallProducer: SmallProducer =
            SmallProducer(value.producerName.getOrElse(""),
              value.referenceNumber,
              (value.lowBand, value.highBand))
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(AddASmallProducerPage, value))
            updatedAnswersFinal = updatedAnswers.copy(smallProducerList = smallProducer :: updatedAnswers.smallProducerList)
            _              <- sessionRepository.set(updatedAnswersFinal)
          } yield {
            Redirect(navigator.nextPage(AddASmallProducerPage, mode, updatedAnswersFinal))

          }
        }

      )
  }
}
