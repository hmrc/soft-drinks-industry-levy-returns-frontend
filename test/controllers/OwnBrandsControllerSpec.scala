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
import forms.OwnBrandsFormProvider
import helpers.LoggerHelper
import models.{NormalMode, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{times, verify, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.OwnBrandsPage
import play.api.inject.bind
import play.api.libs.json.Writes
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import queries.Settable
import repositories.SessionRepository
import utilitlies.GenericLogger
import views.html.OwnBrandsView

import scala.concurrent.Future
import scala.util.{Failure, Try}

class OwnBrandsControllerSpec extends SpecBase with MockitoSugar with LoggerHelper {

  def onwardRoute: Call = Call("GET", "/foo")

  val formProvider = new OwnBrandsFormProvider()
  private val form = formProvider()

  lazy val ownBrandsRoute: String = routes.OwnBrandsController.onPageLoad(NormalMode).url

  "OwnBrands Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, ownBrandsRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[OwnBrandsView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = UserAnswers(sdilNumber).set(OwnBrandsPage, true).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, ownBrandsRoute)

        val view = application.injector.instanceOf[OwnBrandsView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(true), NormalMode)(request, messages(application)).toString
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
          FakeRequest(POST, ownBrandsRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, ownBrandsRoute)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[OwnBrandsView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode)(request, messages(application)).toString
      }
    }

    "must redirect to the next page removing litre data from user answers, when valid data is submitted" in {

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(Right(true))

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository)
          ).build()

      running(application) {
        val request = FakeRequest(POST, ownBrandsRoute).withFormUrlEncodedBody(("value", "false"))
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.PackagedContractPackerController.onPageLoad(NormalMode).url
      }
    }

    "must fail and return an Internal Server Error if the getting(Try) of userAnswers fails" in {

      val mockSessionRepository = mock[SessionRepository]
      when(mockSessionRepository.set(ArgumentMatchers.eq(completedUserAnswers))) thenReturn Future.successful(Right(true))

      val failedTryUserAnswers: UserAnswers = new UserAnswers("sdilId") {
        override def set[A](page: Settable[A], value: A)(implicit writes: Writes[A]): Try[UserAnswers] = Failure[UserAnswers](new Exception(""))
      }

      val application = applicationBuilder(userAnswers = Some(failedTryUserAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, ownBrandsRoute)
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
          val request = FakeRequest(POST, ownBrandsRoute).withFormUrlEncodedBody(("value", "false"))
          val result = await(route(application, request).value)
          events.collectFirst {
            case event =>
              event.getLevel.levelStr mustEqual ("ERROR")
              event.getMessage mustEqual ("Failed to resolve user answers while on ownBrands")
          }.getOrElse(fail("No logging captured"))
        }
      }
    }

    "should log an error message when internal server error is returned when user answers are not set in session repository" in {
        val mockSessionRepository = mock[SessionRepository]
        when(mockSessionRepository.set(ArgumentMatchers.eq(completedUserAnswers))) thenReturn Future.successful(Left(SessionDatabaseInsertError))

        val app =
          applicationBuilder(Some(completedUserAnswers))
            .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
            .build()

        running(app) {
          withCaptureOfLoggingFrom(application.injector.instanceOf[GenericLogger].logger) { events =>
            val request = FakeRequest(POST, ownBrandsRoute).withFormUrlEncodedBody(("value", "false"))
            await(route(app, request).value)
            events.collectFirst {
              case event =>
                event.getLevel.levelStr mustEqual ("ERROR")
                event.getMessage mustEqual ("Failed to set value in session repository while attempting set on ownBrands")
            }.getOrElse(fail("No logging captured"))
          }
        }
    }

  }
}
