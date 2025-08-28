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

import base.ReturnsTestData._
import base.SpecBase
import errors.SessionDatabaseInsertError
import forms.SmallProducerDetailsFormProvider
import helpers.LoggerHelper
import models.NormalMode
import navigation.{ FakeNavigator, Navigator }
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.*
import org.scalatestplus.mockito.MockitoSugar
import play.api.data.Form
import play.api.inject.bind
import play.api.mvc.{ AnyContentAsEmpty, Call }
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import util.GenericLogger
import viewmodels.govuk.SummaryListFluency
import views.html.SmallProducerDetailsView

import scala.concurrent.Future

class SmallProducerDetailsControllerSpec extends SpecBase with MockitoSugar with SummaryListFluency with LoggerHelper {

  def onwardRoute: Call = Call("GET", "/foo")

  val formProvider = new SmallProducerDetailsFormProvider()
  val form: Form[Boolean] = formProvider()

  lazy val smallProducerDetailsRoute: String = routes.SmallProducerDetailsController.onPageLoad(NormalMode).url

  "SmallProducerDetails Controller" - {

    "must redirect to returns sent page if return is already submitted" in {
      val application = applicationBuilder(userAnswers = Some(submittedAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, smallProducerDetailsRoute)
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.ReturnSentController.onPageLoad.url
      }
    }

    "must redirect to returns sent page if return is already submitted when hitting the submit" in {
      val application = applicationBuilder(userAnswers = Some(submittedAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, smallProducerDetailsRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.ReturnSentController.onPageLoad.url
      }
    }

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        implicit val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(GET, smallProducerDetailsRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[SmallProducerDetailsView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode, List())(request, messages(application)).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(Right(true))

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository))
          .build()

      running(application) {
        val request =
          FakeRequest(POST, smallProducerDetailsRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "If user selects no on the small producer details page with 0 producers, " +
      "navigation takes them back to exemption question page" in {

        val mockSessionRepository = mock[SessionRepository]

        when(mockSessionRepository.set(any())) thenReturn Future.successful(Right(true))

        val application =
          applicationBuilder(userAnswers = Some(emptyUserAnswers))
            .overrides(
              bind[SessionRepository].toInstance(mockSessionRepository))
            .build()

        running(application) {
          val request =
            FakeRequest(POST, smallProducerDetailsRoute)
              .withFormUrlEncodedBody(("value", "false"))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual "/soft-drinks-industry-levy-returns-frontend/exemptions-for-small-producers"
        }
      }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, smallProducerDetailsRoute)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[SmallProducerDetailsView]

        val result = route(application, request).value
        val smallProducersSummaryList = List()

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode, smallProducersSummaryList)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing user answers data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, smallProducerDetailsRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing user answers data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, smallProducerDetailsRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must include added small producer SDIL reference on the row" in {

      val userAnswers = emptyUserAnswers.copy(smallProducerList = List(superCola))
      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()
      val expectedView = application.injector.instanceOf[SmallProducerDetailsView]
      val producerList = List(superCola)

      running(application) {
        val request = FakeRequest(GET, smallProducerDetailsRoute)
        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual expectedView(form, NormalMode, producerList)(request, messages(application)).toString
      }
    }

    "must include all added small producer SDIL references on the page in rows" in {

      val actualUserAnswers = emptyUserAnswers.copy(smallProducerList = List(superCola, sparkyJuice))
      val application = applicationBuilder(userAnswers = Some(actualUserAnswers)).build()

      val expectedView = application.injector.instanceOf[SmallProducerDetailsView]
      val producerList = List(superCola, sparkyJuice)

      running(application) {
        val request = FakeRequest(GET, smallProducerDetailsRoute)
        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual expectedView(form, NormalMode, producerList)(request, messages(application)).toString
      }
    }

    "must fail and return an Internal Server Error if the getting(Try) of userAnswers fails" in {

      val mockSessionRepository = mock[SessionRepository]
      when(mockSessionRepository.set(ArgumentMatchers.eq(completedUserAnswers))) thenReturn Future.successful(Right(true))

      val application = applicationBuilder(userAnswers = Some(failingUserAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, smallProducerDetailsRoute)
            .withFormUrlEncodedBody(("value", "false"))

        val result = route(application, request).value

        status(result) mustEqual INTERNAL_SERVER_ERROR
        verify(mockSessionRepository, times(0)).set(completedUserAnswers)
      }
    }

    "should log an error message when internal server error is returned when getting user answers is not resolved" in {
      val mockSessionRepository = mock[SessionRepository]
      when(mockSessionRepository.set(ArgumentMatchers.eq(completedUserAnswers))) thenReturn Future.successful(Right(true))

      val application = applicationBuilder(userAnswers = Some(failingUserAnswers)).build()

      running(application) {
        withCaptureOfLoggingFrom(application.injector.instanceOf[GenericLogger].logger) { events =>
          val request = FakeRequest(POST, smallProducerDetailsRoute).withFormUrlEncodedBody(("value", "false"))
          await(route(application, request).value)
          events.collectFirst {
            case event =>
              event.getLevel.levelStr mustEqual "ERROR"
              event.getMessage mustEqual "Failed to resolve user answers while on smallProducerDetails"
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
          val request = FakeRequest(POST, smallProducerDetailsRoute).withFormUrlEncodedBody(("value", "false"))
          await(route(app, request).value)
          events.collectFirst {
            case event =>
              event.getLevel.levelStr mustEqual "ERROR"
              event.getMessage mustEqual "Failed to set value in session repository while attempting set on smallProducerDetails"
          }.getOrElse(fail("No logging captured"))
        }
      }
    }

  }
}
