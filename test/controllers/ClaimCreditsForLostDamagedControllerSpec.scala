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
import connectors.SoftDrinksIndustryLevyConnector
import errors.SessionDatabaseInsertError
import forms.ClaimCreditsForLostDamagedFormProvider
import helpers.LoggerHelper
import models.NormalMode
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.mockito.MockitoSugar.{times, verify}
import org.scalatestplus.mockito.MockitoSugar
import pages.ClaimCreditsForLostDamagedPage
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import utilitlies.GenericLogger
import views.html.ClaimCreditsForLostDamagedView

import scala.concurrent.Future

class ClaimCreditsForLostDamagedControllerSpec extends SpecBase with MockitoSugar with LoggerHelper {

  def onwardRoute: Call = Call("GET", "/foo")

  val formProvider = new ClaimCreditsForLostDamagedFormProvider()
  private val form = formProvider()

  lazy val claimCreditsForLostDamagedRoute: String = routes.ClaimCreditsForLostDamagedController.onPageLoad(NormalMode).url

  "ClaimCreditsForLostDamaged Controller" - {

    "must redirect to returns sent page if return is already submitted" in {
      val application = applicationBuilder(userAnswers = Some(submittedAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, claimCreditsForLostDamagedRoute)
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.ReturnSentController.onPageLoad.url
      }
    }

    "must redirect to returns sent page if return is already submitted when hitting the submit" in {
      val application = applicationBuilder(userAnswers = Some(submittedAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, claimCreditsForLostDamagedRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.ReturnSentController.onPageLoad.url
      }
    }

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, claimCreditsForLostDamagedRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[ClaimCreditsForLostDamagedView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = emptyUserAnswers.set(ClaimCreditsForLostDamagedPage, true).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, claimCreditsForLostDamagedRoute)

        val view = application.injector.instanceOf[ClaimCreditsForLostDamagedView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(true), NormalMode)(request, messages(application)).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val mockSessionRepository = mock[SessionRepository]
      val mockSdilConnector = mock[SoftDrinksIndustryLevyConnector]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(Right(true))
      when(mockSdilConnector.retrieveSubscription(any(), any())(any())) thenReturn Future.successful(None)

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[SoftDrinksIndustryLevyConnector].toInstance(mockSdilConnector)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, claimCreditsForLostDamagedRoute)
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
          FakeRequest(POST, claimCreditsForLostDamagedRoute)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[ClaimCreditsForLostDamagedView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing user answers data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, claimCreditsForLostDamagedRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing user answers data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, claimCreditsForLostDamagedRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to the next page removing litres data from user answers, when valid data is submitted" in {
      lazy val completedUserAnswersWithCreditLostDamaged = emptyUserAnswers.copy(data = Json.obj("ownBrands" -> false, "packagedContractPacker" ->
        false, "exemptionsForSmallProducers" -> false, "broughtIntoUK" -> false, "broughtIntoUkFromSmallProducers" -> false, "claimCreditsForExports" ->
          false, "claimCreditsForLostDamaged" -> true, "howManyCreditsForLostDamaged" -> Json.obj("lowBand" ->89032484,
          "highBand" -> 87291372)))
      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(Right(true))

      val application =
        applicationBuilder(userAnswers = Some(completedUserAnswersWithCreditLostDamaged))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository)
          ).build()

      running(application) {
        val request = FakeRequest(POST, claimCreditsForLostDamagedRoute).withFormUrlEncodedBody(("value", "false"))
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.CheckYourAnswersController.onPageLoad.url
      }
    }

    "must fail and return an Internal Server Error if the getting(Try) of userAnswers fails" in {

      val mockSessionRepository = mock[SessionRepository]
      val mockSdilConnector = mock[SoftDrinksIndustryLevyConnector]

      when(mockSessionRepository.set(ArgumentMatchers.eq(completedUserAnswers))) thenReturn Future.successful(Right(true))
      when(mockSdilConnector.retrieveSubscription(any(), any())(any())) thenReturn Future.successful(None)

      val application =
        applicationBuilder(userAnswers = Some(failingUserAnswers))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[SoftDrinksIndustryLevyConnector].toInstance(mockSdilConnector)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, claimCreditsForLostDamagedRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual INTERNAL_SERVER_ERROR
        verify(mockSessionRepository, times(0)).set(completedUserAnswers)
      }
    }

    "should log an error message when internal server error is returned when getting user answers is not resolved" in {
      val mockSessionRepository = mock[SessionRepository]
      val mockSdilConnector = mock[SoftDrinksIndustryLevyConnector]
      when(mockSessionRepository.set(ArgumentMatchers.eq(completedUserAnswers))) thenReturn Future.successful(Right(true))

      val application =
        applicationBuilder(userAnswers = Some(failingUserAnswers))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[SoftDrinksIndustryLevyConnector].toInstance(mockSdilConnector)
          )
          .build()

      running(application) {
        withCaptureOfLoggingFrom(application.injector.instanceOf[GenericLogger].logger) { events =>
          val request = FakeRequest(POST, claimCreditsForLostDamagedRoute).withFormUrlEncodedBody(("value", "false"))
          await(route(application, request).value)
          events.collectFirst {
            case event =>
              event.getLevel.levelStr mustEqual "ERROR"
              event.getMessage mustEqual "Failed to resolve user answers while on claimCreditsForLostDamaged"
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
          val request = FakeRequest(POST, claimCreditsForLostDamagedRoute).withFormUrlEncodedBody(("value", "false"))
          await(route(app, request).value)
          events.collectFirst {
            case event =>
              event.getLevel.levelStr mustEqual "ERROR"
              event.getMessage mustEqual "Failed to set value in session repository while attempting set on claimCreditsForLostDamaged"
          }.getOrElse(fail("No logging captured"))
        }
      }
    }
  }
}
