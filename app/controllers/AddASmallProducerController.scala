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
import forms.AddASmallProducerFormProvider
import models.errors.{AlreadyExists, NotASmallProducer, SDILReferenceErrors}
import models.requests.DataRequest
import models.{AddASmallProducer, BlankMode, Mode, NormalMode, ReturnPeriod, SmallProducer, UserAnswers}
import navigation.Navigator
import pages.AddASmallProducerPage
import play.api.data.{Form, FormError}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.AddASmallProducerView

import javax.inject.Inject
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

        userAnswers.submitted match {
          case true => Redirect(routes.ReturnSentController.onPageLoad())
          case false =>
            val form: Form[AddASmallProducer] = formProvider(userAnswers)
            mode match {
              case BlankMode =>
                Ok(view(form, NormalMode))
              case _ =>
                Ok(view(form, mode))
            }
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

          val smallProducerList = request.userAnswers.smallProducerList
          val smallProducerOpt = smallProducerList.find(smallProducer => smallProducer.sdilRef == value.referenceNumber)

          smallProducerOpt match {
            case Some(_) =>
              val preparedForm = form.fill(value)
              Future.successful(
                BadRequest(view(preparedForm.withError(FormError("referenceNumber", "addASmallProducer.error.referenceNumber.exists")), mode))
              )
            case _ =>
              val preparedForm = form.fill(value)
              sdilConnector.checkSmallProducerStatus(value.referenceNumber, request.returnPeriod.get).flatMap {
                case Some(false) =>
                  Future.successful(
                    BadRequest(view(preparedForm.withError(FormError("referenceNumber", "addASmallProducer.error.referenceNumber.notASmallProducer")), mode))
                  )
                case _ => updateDatabase(value, userAnswers).map(updatedAnswersFinal =>
                  Redirect(navigator.nextPage(AddASmallProducerPage, mode, updatedAnswersFinal))
                )
              }
          }
        }
      )
  }

  def onEditPageLoad(mode: Mode, sdilReference: String): Action[AnyContent] =
    (identify andThen getData andThen requireData) {
      implicit request: DataRequest[AnyContent] =>

        val userAnswers = request.userAnswers
        val form = formProvider(userAnswers)
        val targetSmallProducer = userAnswers.smallProducerList.find(producer => producer.sdilRef == sdilReference)

        targetSmallProducer match {
          case Some(producer) =>
            val addASmallProducer = AddASmallProducer(Some(producer.alias), producer.sdilRef, producer.litreage._1,
              producer.litreage._2)
            val preparedForm = form.fill(addASmallProducer)
            Ok(view(preparedForm, mode, Some(sdilReference)))
          case _ =>
            throw new RuntimeException("No such small producer exists")
        }
    }

  def onEditPageSubmit(mode: Mode, sdilReference: String): Action[AnyContent] =
    (identify andThen getData andThen requireData).async {
      implicit request =>

        val userAnswers = request.userAnswers
        val returnPeriod = request.returnPeriod
        val form = formProvider(userAnswers)

        form.bindFromRequest().fold(
          formWithErrors =>
            Future.successful(BadRequest(view(formWithErrors, mode, Some(sdilReference)))),
          formData => {
            val smallProducerList = request.userAnswers.smallProducerList
            isValidSDILRef(sdilReference, formData.referenceNumber, smallProducerList, returnPeriod).flatMap({
              case Left(AlreadyExists) =>
                Future.successful(
                  BadRequest(view(form.withError(FormError("referenceNumber", "addASmallProducer.error.referenceNumber.exists")), mode, Some(sdilReference)))
                )
              case Left(NotASmallProducer) =>
                Future.successful(
                  BadRequest(view(form.withError(FormError("referenceNumber", "addASmallProducer.error.referenceNumber.notASmallProducer")),
                    mode, Some(sdilReference)))
                )
              case Right(_) =>
                updateSmallProducerList(formData, userAnswers, sdilReference).map(updatedAnswersFinal =>
                  Redirect(navigator.nextPage(AddASmallProducerPage, mode, updatedAnswersFinal)))
            })
          }
        )
    }

  private def updateDatabase(addSmallProducer: AddASmallProducer, userAnswers: UserAnswers): Future[UserAnswers] = {
    val smallProducer = SmallProducer(addSmallProducer.producerName.getOrElse(""), addSmallProducer.referenceNumber,
      (addSmallProducer.lowBand, addSmallProducer.highBand))
    for {
      updatedAnswers <- Future.fromTry(userAnswers.set(AddASmallProducerPage, addSmallProducer))
      updatedAnswersFinal = updatedAnswers.copy(smallProducerList = smallProducer :: updatedAnswers.smallProducerList)
      _ <- sessionRepository.set(updatedAnswersFinal)
    } yield {
      updatedAnswersFinal
    }
  }

  private def isValidSDILRef(currentSDILRef: String, addASmallProducerSDILRef: String,
                             smallProducerList: Seq[SmallProducer], returnPeriod: Option[ReturnPeriod])
                            (implicit hc: HeaderCarrier): Future[Either[SDILReferenceErrors, Unit]] = {

    if (currentSDILRef == addASmallProducerSDILRef) {
      Future.successful(Right(()))
    } else if (smallProducerList.map(_.sdilRef).contains(addASmallProducerSDILRef)) {
      Future.successful(Left(AlreadyExists))
    } else {
      sdilConnector.checkSmallProducerStatus(addASmallProducerSDILRef, returnPeriod.get).map {
        case Some(false) => Left(NotASmallProducer)
        case _ => Right(())
      }
    }
  }

  private def updateSmallProducerList(formData: AddASmallProducer, userAnswers: UserAnswers, sdilUnderEdit: String): Future[UserAnswers] = {

    val smallProducer = SmallProducer(
      formData.producerName.getOrElse(""),
      formData.referenceNumber,
      (formData.lowBand, formData.highBand))

    for {
      updatedAnswers <- Future.fromTry(userAnswers.set(AddASmallProducerPage, formData))
      newListWithOldSPRemoved = updatedAnswers.smallProducerList.filterNot(_.sdilRef == sdilUnderEdit)
      updatedAnswersFinal = updatedAnswers.copy(smallProducerList = smallProducer :: newListWithOldSPRemoved)
      _ <- sessionRepository.set(updatedAnswersFinal)
    } yield {
      updatedAnswersFinal
    }
  }
}
