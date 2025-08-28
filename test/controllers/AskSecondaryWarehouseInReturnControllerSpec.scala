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

import base.ReturnsTestData.*
import base.SpecBase
import forms.AskSecondaryWarehouseInReturnFormProvider
import models.NormalMode
import navigation.{ FakeNavigator, Navigator }
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.*
import org.scalatestplus.mockito.MockitoSugar
import pages.AskSecondaryWarehouseInReturnPage
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import services.{ AddressLookupService, WarehouseDetails }
import views.html.AskSecondaryWarehouseInReturnView

import scala.concurrent.Future

class AskSecondaryWarehouseInReturnControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute = Call("GET", "/foo")

  val formProvider = new AskSecondaryWarehouseInReturnFormProvider()
  val form = formProvider()

  lazy val askSecondaryWarehouseInReturnRoute = routes.AskSecondaryWarehouseInReturnController.onPageLoad(NormalMode).url

  "AskSecondaryWarehouseInReturn Controller" - {

    "must redirect to returns sent page if return is already submitted" in {
      val application = applicationBuilder(userAnswers = Some(submittedAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, askSecondaryWarehouseInReturnRoute)
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.ReturnSentController.onPageLoad.url
      }
    }

    "must redirect to returns sent page if return is already submitted when hitting the submit" in {
      val application = applicationBuilder(userAnswers = Some(submittedAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, askSecondaryWarehouseInReturnRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.ReturnSentController.onPageLoad.url
      }
    }

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, askSecondaryWarehouseInReturnRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[AskSecondaryWarehouseInReturnView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = emptyUserAnswers.set(AskSecondaryWarehouseInReturnPage, true).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, askSecondaryWarehouseInReturnRoute)

        val view = application.injector.instanceOf[AskSecondaryWarehouseInReturnView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(true), NormalMode)(request, messages(application)).toString
      }
    }

    "must redirect to alf on ramp when valid data is submitted (true)" in {

      val mockSessionRepository = mock[SessionRepository]
      val mockAddressLookupService = mock[AddressLookupService]
      val onwardUrlForALF = "foobarwizz"

      when(mockSessionRepository.set(any())) thenReturn Future.successful(Right(true))
      when(mockAddressLookupService.initJourneyAndReturnOnRampUrl(
        ArgumentMatchers.eq(WarehouseDetails), ArgumentMatchers.any(), ArgumentMatchers.any())(
          ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
        .thenReturn(Future.successful(onwardUrlForALF))

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[AddressLookupService].toInstance(mockAddressLookupService))
          .build()

      running(application) {
        val request =
          FakeRequest(POST, askSecondaryWarehouseInReturnRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardUrlForALF

        verify(mockAddressLookupService, times(1)).initJourneyAndReturnOnRampUrl(
          ArgumentMatchers.eq(WarehouseDetails), ArgumentMatchers.any(), ArgumentMatchers.any())(
            ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any())
      }
    }
    "must redirect to alf on ramp when valid data is submitted (false), verifying ALF is not called" in {

      val mockSessionRepository = mock[SessionRepository]
      val mockAddressLookupService = mock[AddressLookupService]
      val onwardUrlForALF = "foobarwizz"

      when(mockSessionRepository.set(any())) thenReturn Future.successful(Right(true))
      when(mockAddressLookupService.initJourneyAndReturnOnRampUrl(
        ArgumentMatchers.eq(WarehouseDetails), ArgumentMatchers.any(), ArgumentMatchers.any())(
          ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
        .thenReturn(Future.successful(onwardUrlForALF))

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[AddressLookupService].toInstance(mockAddressLookupService))
          .build()

      running(application) {
        val request =
          FakeRequest(POST, askSecondaryWarehouseInReturnRoute)
            .withFormUrlEncodedBody(("value", "false"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.CheckYourAnswersController.onPageLoad.url

        verify(mockAddressLookupService, times(0)).initJourneyAndReturnOnRampUrl(
          ArgumentMatchers.eq(WarehouseDetails), ArgumentMatchers.any(), ArgumentMatchers.any())(
            ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any())
      }
    }
    "must return error if ALF on ramp call returns error" in {
      val mockSessionRepository = mock[SessionRepository]
      val mockAddressLookupService = mock[AddressLookupService]
      when(mockSessionRepository.set(any())) thenReturn Future.successful(Right(true))

      when(mockAddressLookupService.initJourneyAndReturnOnRampUrl(
        ArgumentMatchers.eq(WarehouseDetails), ArgumentMatchers.any(), ArgumentMatchers.any())(
          ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
        .thenReturn(Future.failed(new Exception("uh oh spaghetio")))

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[AddressLookupService].toInstance(mockAddressLookupService))
          .build()

      running(application) {
        val request =
          FakeRequest(POST, askSecondaryWarehouseInReturnRoute)
            .withFormUrlEncodedBody(("value", "true"))

        intercept[Exception](await(route(application, request).value))

        verify(mockAddressLookupService, times(1)).initJourneyAndReturnOnRampUrl(
          ArgumentMatchers.eq(WarehouseDetails), ArgumentMatchers.any(), ArgumentMatchers.any())(
            ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any())
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, askSecondaryWarehouseInReturnRoute)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[AskSecondaryWarehouseInReturnView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode)(request, messages(application)).toString
      }
    }
  }
}
