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
import models.{AddASmallProducer, BlankMode, Mode, NormalMode, ReturnPeriod, SmallProducer, UserAnswers}
import navigation.Navigator
import pages.AddASmallProducerPage
import play.api.data.{Form, FormError}
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
      val userAnswers = request.userAnswers
      val form: Form[AddASmallProducer] = formProvider(userAnswers)
       mode match {
        case BlankMode => Ok(view(form, NormalMode))
        case _ =>
            val preparedForm = userAnswers.get(AddASmallProducerPage) match {
              case None => form
              case Some(value) => form.fill(value)
            }
            Ok(view(preparedForm, mode))
      }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      val userAnswers = request.userAnswers
      val form: Form[AddASmallProducer] = formProvider(userAnswers)

      form.bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors, mode))),
        value => {
          sdilConnector.checkSmallProducerStatus(value.referenceNumber, request.returnPeriod.get).flatMap {
            case Some(false) =>
              Future.successful(
                BadRequest(view(form.withError(FormError("referenceNumber", "addASmallProducer.error.referenceNumber.notASmallProducer")), mode))
              )
            case _ => updateDatabase(value, userAnswers).map(updatedAnswersFinal =>
              Redirect(navigator.nextPage(AddASmallProducerPage, mode, updatedAnswersFinal))
            )
          }
        }
      )
  }

  private def updateDatabase(addSmallProducer: AddASmallProducer, userAnswers: UserAnswers): Future[UserAnswers] = {
    val smallProducer = SmallProducer(addSmallProducer.producerName.getOrElse(""), addSmallProducer.referenceNumber, (addSmallProducer.lowBand, addSmallProducer.highBand))
    for {
      updatedAnswers <- Future.fromTry(userAnswers.set(AddASmallProducerPage, addSmallProducer))
      updatedAnswersFinal = updatedAnswers.copy(smallProducerList = smallProducer :: updatedAnswers.smallProducerList)
      _ <- sessionRepository.set(updatedAnswersFinal)
    } yield {
      updatedAnswersFinal
    }
  }

  def onEditPageLoad(mode: Mode, sdilReference: String): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request: DataRequest[AnyContent] =>

      val userAnswers = request.userAnswers
      val form = formProvider(userAnswers)
      val targetSmallProducer = userAnswers.smallProducerList.find(producer => producer.sdilRef == sdilReference)

      targetSmallProducer match {
        case Some(producer) =>
          val addASmallProducer = AddASmallProducer(Some(producer.alias), producer.sdilRef, producer.litreage._1, producer.litreage._2)
          val preparedForm = form.fill(addASmallProducer)
          Ok(view(preparedForm, mode, Some(sdilReference)))
        case _ => ???
      }





  }

  def onEditPageSubmit(mode: Mode, sdil: String): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>

      val userAnswers = request.userAnswers
      val form = formProvider(userAnswers)

      form.bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors, mode, Some(sdil)))),
        addSmallProducer => {
          val smallProducer: SmallProducer =
            SmallProducer(addSmallProducer.producerName.getOrElse(""),
              addSmallProducer.referenceNumber,
              (addSmallProducer.lowBand, addSmallProducer.highBand))
          for {
            updatedAnswers <- Future.fromTry(userAnswers.set(AddASmallProducerPage, addSmallProducer))
            updatedList = userAnswers.smallProducerList.filterNot(producer => producer.sdilRef == sdil)
            updatedAnswersFinal = {updatedAnswers.copy(smallProducerList = smallProducer :: updatedList)}
            _              <- sessionRepository.set(updatedAnswersFinal)
          } yield {
            Redirect(navigator.nextPage(AddASmallProducerPage, mode, updatedAnswersFinal))
          }
        }

      )
  }



}
