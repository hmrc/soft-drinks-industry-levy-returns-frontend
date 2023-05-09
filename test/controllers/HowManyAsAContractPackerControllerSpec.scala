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
import forms.HowManyAsAContractPackerFormProvider
import helpers.LoggerHelper
import models.{LitresInBands, NormalMode, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.mockito.MockitoSugar.{times, verify}
import org.scalatestplus.mockito.MockitoSugar
import pages.HowManyAsAContractPackerPage
import play.api.data.Form
import play.api.inject.bind
import play.api.libs.json.{Json, Writes}
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import queries.Settable
import repositories.SessionRepository
import utilitlies.GenericLogger
import views.html.HowManyAsAContractPackerView

import scala.concurrent.Future
import scala.util.{Failure, Try}

class HowManyAsAContractPackerControllerSpec extends SpecBase with MockitoSugar with LoggerHelper {

  def onwardRoute: Call = Call("GET", "/foo")

  val formProvider = new HowManyAsAContractPackerFormProvider()
  val form: Form[LitresInBands] = formProvider()

  val value1max: Long = 100000000000000L
  val value1: Long = value1max - 1

  val value2max: Long = 100000000000000L
  val value2: Long = value2max - 1

  lazy val howManyAsAContractPackerRoute: String = routes.HowManyAsAContractPackerController.onPageLoad(NormalMode).url

  val userAnswers: UserAnswers = UserAnswers(
    sdilNumber,
    Json.obj(
      HowManyAsAContractPackerPage.toString -> Json.obj(
        "lowBand" -> value1,
        "highBand" -> value2
      )
    )
  )

  "HowManyAsAContractPacker Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, howManyAsAContractPackerRoute)

        val view = application.injector.instanceOf[HowManyAsAContractPackerView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, howManyAsAContractPackerRoute)

        val view = application.injector.instanceOf[HowManyAsAContractPackerView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(LitresInBands(value1, value2)), NormalMode)(request, messages(application)).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(Right(true))

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, howManyAsAContractPackerRoute)
            .withFormUrlEncodedBody(("lowBand", value1.toString), ("highBand", value2.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, howManyAsAContractPackerRoute)
            .withFormUrlEncodedBody(("value", "invalid value"))

        val boundForm = form.bind(Map("value" -> "invalid value"))

        val view = application.injector.instanceOf[HowManyAsAContractPackerView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing user answers data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, howManyAsAContractPackerRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing user answers data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, howManyAsAContractPackerRoute)
            .withFormUrlEncodedBody(("lowBand", value1.toString), ("highBand", value2.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must fail and return an Internal Server Error if the getting(Try) of userAnswers fails" in {

      val mockSessionRepository = mock[SessionRepository]
      when(mockSessionRepository.set(ArgumentMatchers.eq(completedUserAnswers))) thenReturn Future.successful(Right(true))

      val userAnswers: UserAnswers = new UserAnswers("sdilId") {
        override def set[A](page: Settable[A], value: A)(implicit writes: Writes[A]): Try[UserAnswers] = Failure[UserAnswers](new Exception(""))
      }

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, howManyAsAContractPackerRoute)
            .withFormUrlEncodedBody(("lowBand", value1.toString), ("highBand", value2.toString))

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
          val request = FakeRequest(POST, howManyAsAContractPackerRoute).withFormUrlEncodedBody(("lowBand", value1.toString), ("highBand", value2.toString))
          await(route(application, request).value)
          events.collectFirst {
            case event =>
              event.getLevel.levelStr mustEqual "ERROR"
              event.getMessage mustEqual "Failed to resolve user answers while on howManyAsAContractPacker"
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
          val request = FakeRequest(POST, howManyAsAContractPackerRoute)
          .withFormUrlEncodedBody(("lowBand", value1.toString), ("highBand", value2.toString))
          await(route(app, request).value)
          events.collectFirst {
            case event =>
              event.getLevel.levelStr mustEqual "ERROR"
              event.getMessage mustEqual "Failed to set value in session repository while attempting set on howManyAsAContractPacker"
          }.getOrElse(fail("No logging captured"))
        }
      }
    }

  }
}
