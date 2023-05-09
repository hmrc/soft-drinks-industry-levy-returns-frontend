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

import base.SpecBase
import errors.SessionDatabaseInsertError
import forms.RemoveSmallProducerConfirmFormProvider
import helpers.LoggerHelper
import models.{NormalMode, SmallProducer, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.jsoup.Jsoup
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.mockito.MockitoSugar.{times, verify}
import org.scalatestplus.mockito.MockitoSugar
import pages.RemoveSmallProducerConfirmPage
import play.api.data.Form
import play.api.i18n.Messages
import play.api.inject.bind
import play.api.libs.json.{JsObject, Json, Writes}
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import queries.Settable
import repositories.SessionRepository
import utilitlies.GenericLogger
import views.html.RemoveSmallProducerConfirmView

import scala.concurrent.Future
import scala.util.{Failure, Try}


class RemoveSmallProducerConfirmControllerSpec extends SpecBase with MockitoSugar with LoggerHelper {

  def onwardRoute: Call = Call("GET", "/foo")

  val formProvider = new RemoveSmallProducerConfirmFormProvider()
  val form: Form[Boolean] = formProvider()
  val producerName = "Super Cola Plc"
  val sdilReference = "XCSDIL000000069"
  val producerNameParty = "Soft Juice"
  val sdilReferenceParty = "XMSDIL000000113"
  val bandMax: Long = 100000000000000L
  val litres: Long = bandMax - 1
  val smallProducerList = List(SmallProducer(producerNameParty, sdilReferenceParty, (litres, litres)))
  val smallProducerListWithTwoProducers = List(
    SmallProducer(producerNameParty, sdilReferenceParty, (litres, litres)),
    SmallProducer(producerName, sdilReference, (litres, litres))
  )
  val smallProducerListOnlySuperCola = List(SmallProducer(producerName, sdilReference, (litres, litres)))
  val userAnswersData: JsObject = Json.obj(
    RemoveSmallProducerConfirmPage.toString -> Json.obj(
      "producerName" -> producerName,
      "referenceNumber" -> sdilReference,
      "lowBand" -> litres,
      "highBand" -> litres
    )
  )
  val userAnswers: UserAnswers = UserAnswers(sdilNumber, userAnswersData, smallProducerList)
  val userAnswersWithTwoProducers: UserAnswers = UserAnswers(sdilNumber, userAnswersData, smallProducerListWithTwoProducers)
  val userAnswersWithOneProducer: UserAnswers = UserAnswers(sdilNumber, userAnswersData, smallProducerListOnlySuperCola)

  lazy val removeSmallProducerConfirmRoute: String = routes.RemoveSmallProducerConfirmController.onPageLoad(s"$sdilReferenceParty").url

  "RemoveSmallProducerConfirm Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, removeSmallProducerConfirmRoute)
        val view = application.injector.instanceOf[RemoveSmallProducerConfirmView]
        val result = route(application, request).value

        status(result) mustEqual OK
        val page = Jsoup.parse(contentAsString(result))
        page.title() must include(Messages("removeSmallProducerConfirm.title"))
        page.getElementsByTag("h1").text() mustEqual Messages("removeSmallProducerConfirm.heading")
        contentAsString(result) mustEqual view(form, NormalMode, sdilReferenceParty, producerNameParty)(request, messages(application)).toString
      }
    }

    "must return OK and the correct view for a GET when yes or no was already answered on previous visits to the page" in {
      val userAnswers = UserAnswers(sdilReference, userAnswersData, smallProducerList).set(RemoveSmallProducerConfirmPage, false).success.value
      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, removeSmallProducerConfirmRoute)
        val result = route(application, request).value

        status(result) mustEqual OK
        val page = Jsoup.parse(contentAsString(result))
        page.title() must include(Messages("removeSmallProducerConfirm.title"))
        page.getElementsByTag("h1").text() mustEqual Messages("removeSmallProducerConfirm.heading")
      }
    }

    "must redirect to small producer details page when more than one producer present and the small producer is not in" +
      "the SmallProducerList (i.e. user clicked back button/browser back after confirming remove" in {

      val application = applicationBuilder(userAnswers = Some(userAnswersWithOneProducer)).build()

      running(application) {
        val request = FakeRequest(GET, removeSmallProducerConfirmRoute)
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
      }
    }

    "must redirect to the next page when valid data is submitted" in {
      val userAnswers = UserAnswers(sdilReference, userAnswersData, smallProducerList).set(RemoveSmallProducerConfirmPage, true).success.value
      val mockSessionRepository = mock[SessionRepository]
      when(mockSessionRepository.set(any())) thenReturn Future.successful(Right(true))
      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request = FakeRequest(POST, removeSmallProducerConfirmRoute).withFormUrlEncodedBody(("value", "true"))
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return to small producer details page when no is selected and there are two producers on the list" in {
      val userAnswers = UserAnswers(sdilReference, userAnswersData, smallProducerListWithTwoProducers).set(RemoveSmallProducerConfirmPage, true).success.value
      val mockSessionRepository = mock[SessionRepository]
      when(mockSessionRepository.set(any())) thenReturn Future.successful(Right(true))

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request = FakeRequest(POST, removeSmallProducerConfirmRoute).withFormUrlEncodedBody(("value", "false"))
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual Call("GET", "/soft-drinks-industry-levy-returns-frontend/small-producer-details").url
      }
    }

    "must return to small producer details page when yes is selected and it is not the last producer being removed" in {
      val userAnswers = UserAnswers(sdilReference, userAnswersData, smallProducerListWithTwoProducers).set(
        RemoveSmallProducerConfirmPage, true).success.value
      val mockSessionRepository = mock[SessionRepository]
      when(mockSessionRepository.set(any())) thenReturn Future.successful(Right(true))
      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request = FakeRequest(POST, removeSmallProducerConfirmRoute).withFormUrlEncodedBody(("value", "true"))
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual Call("GET", "/soft-drinks-industry-levy-returns-frontend/small-producer-details").url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {
      val userAnswers = UserAnswers(sdilReference, userAnswersData, smallProducerList).set(RemoveSmallProducerConfirmPage, true).success.value
      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(POST, removeSmallProducerConfirmRoute).withFormUrlEncodedBody(("value", ""))
        val boundForm = form.bind(Map("value" -> ""))
        val view = application.injector.instanceOf[RemoveSmallProducerConfirmView]
        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        val page = Jsoup.parse(contentAsString(result))
        contentAsString(result) mustEqual view(boundForm, NormalMode, sdilReferenceParty, producerNameParty)(request, messages(application)).toString
        page.getElementsByTag("a").text() must include(Messages("removeSmallProducerConfirm.error.required"))
      }
    }

    "must redirect to Journey Recovery for a GET if no existing user answers data is found" in {
      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, removeSmallProducerConfirmRoute)
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing user answers data is found" in {
      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(POST, removeSmallProducerConfirmRoute).withFormUrlEncodedBody(("value", "true"))
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must fail and return an Internal Server Error if the getting(Try) of userAnswers fails" in {

      val userAnswers: UserAnswers = new UserAnswers("sdilId",Json.obj(),smallProducerList) {
        override def set[A](page: Settable[A], value: A)(implicit writes: Writes[A]): Try[UserAnswers] = Failure[UserAnswers](new Exception(""))
      }

      val mockSessionRepository = mock[SessionRepository]
      when(mockSessionRepository.set(any())) thenReturn Future.successful(Right(true))

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, removeSmallProducerConfirmRoute)
            .withFormUrlEncodedBody(("value", "false"))

        val result = route(application, request).value

        status(result) mustEqual INTERNAL_SERVER_ERROR
        verify(mockSessionRepository, times(0)).set(completedUserAnswers)
      }
    }

    "should log an error message when internal server error is returned when getting user answers is not resolved" in {
      val mockSessionRepository = mock[SessionRepository]
      when(mockSessionRepository.set(ArgumentMatchers.eq(completedUserAnswers))) thenReturn Future.successful(Right(true))

      val userAnswers: UserAnswers = new UserAnswers("sdilId") {
        override def set[A](page: Settable[A], value: A)(implicit writes: Writes[A]): Try[UserAnswers] = Failure[UserAnswers](new Exception(""))
      }

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        withCaptureOfLoggingFrom(application.injector.instanceOf[GenericLogger].logger) { events =>
          val request = FakeRequest(POST, removeSmallProducerConfirmRoute).withFormUrlEncodedBody(("value", "false"))
          await(route(application, request).value)
          events.collectFirst {
            case event =>
              event.getLevel.levelStr mustEqual "ERROR"
              event.getMessage mustEqual "Failed to resolve user answers while on removeSmallProducerConfirm"
          }.getOrElse(fail("No logging captured"))
        }
      }
    }

    "should log an error message when internal server error is returned when user answers are not set in session repository" in {
      val mockSessionRepository = mock[SessionRepository]
      when(mockSessionRepository.set(any())) thenReturn Future.successful(Left(SessionDatabaseInsertError))

      val app =
        applicationBuilder(Some(completedUserAnswers))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .build()

      running(app) {
        withCaptureOfLoggingFrom(application.injector.instanceOf[GenericLogger].logger) { events =>
          val request = FakeRequest(POST, removeSmallProducerConfirmRoute).withFormUrlEncodedBody(("value", "false"))
          await(route(app, request).value)
          events.collectFirst {
            case event =>
              event.getLevel.levelStr mustEqual "ERROR"
              event.getMessage mustEqual "Failed to set value in session repository while attempting set on removeSmallProducerConfirm"
          }.getOrElse(fail("No logging captured"))
        }
      }
    }

  }
}
