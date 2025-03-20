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
import forms.PackagingSiteDetailsFormProvider
import helpers.LoggerHelper
import models.{ CheckMode, NormalMode, UserAnswers }
import navigation.{ FakeNavigator, Navigator }
import org.jsoup.Jsoup
import org.mockito.ArgumentMatchers.any
import org.mockito.{ ArgumentMatchers, MockitoSugar }
import pages.PackagingSiteDetailsPage
import play.api.data.Form
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import services.{ AddressLookupService, PackingDetails }
import utilities.GenericLogger
import viewmodels.govuk.SummaryListFluency
import views.html.PackagingSiteDetailsView

import scala.concurrent.Future

class PackagingSiteDetailsControllerSpec extends SpecBase with MockitoSugar with SummaryListFluency with LoggerHelper {

  def onwardRoute: Call = Call("GET", "/foo")

  val formProvider = new PackagingSiteDetailsFormProvider()
  val form: Form[Boolean] = formProvider()

  lazy val packagingSiteDetailsRoute: String = routes.PackagingSiteDetailsController.onPageLoad(NormalMode).url
  lazy val checkPackagingSiteDetailsRoute: String = routes.PackagingSiteDetailsController.onPageLoad(CheckMode).url
  lazy val userAnswersWith1PackagingSite: UserAnswers = emptyUserAnswers.copy(packagingSiteList = packagingSiteListWith1)
  lazy val newImporterAnswer: UserAnswers = emptyUserAnswers.copy(data = Json.obj("HowManyBroughtIntoUk" -> Json.obj("lowBand" -> 10, "highBand" -> 10)), packagingSiteList = packagingSiteListWith1)

  "packagingSiteDetails Controller" - {

    "must redirect to returns sent page if return is already submitted" in {
      val application = applicationBuilder(userAnswers = Some(submittedAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, packagingSiteDetailsRoute)
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.ReturnSentController.onPageLoad.url
      }
    }

    "must redirect to returns sent page if return is already submitted when hitting the submit" in {
      val application = applicationBuilder(userAnswers = Some(submittedAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, packagingSiteDetailsRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.ReturnSentController.onPageLoad.url
      }
    }

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(userAnswersWith1PackagingSite)).build()

      running(application) {
        val request = FakeRequest(GET, packagingSiteDetailsRoute)

        val packagingSiteList = userAnswersWith1PackagingSite.packagingSiteList

        val result = route(application, request).value
        val view = application.injector.instanceOf[PackagingSiteDetailsView]

        status(result) mustEqual OK
        val page = Jsoup.parse(contentAsString(result))

        contentAsString(result) mustEqual view(form, NormalMode, packagingSiteList)(request, messages(application)).toString
        page.title() must include("You added 1 packaging site")
        page.getElementsByTag("h1").text() mustEqual "You added 1 packaging site"
        page.getElementsByClass("govuk-fieldset__legend--m").text() must include("Do you want to add another UK packaging site?")
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val updatedUserAnswers = userAnswersWith1PackagingSite.set(PackagingSiteDetailsPage, true).success.value

      val application = applicationBuilder(userAnswers = Some(updatedUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, packagingSiteDetailsRoute)
          .withFormUrlEncodedBody(("value", "true"))

        val packagingSiteList = userAnswersWith1PackagingSite.packagingSiteList

        val result = route(application, request).value
        val view = application.injector.instanceOf[PackagingSiteDetailsView]

        status(result) mustEqual OK
        val page = Jsoup.parse(contentAsString(result))

        contentAsString(result) mustEqual view(form.fill(true), NormalMode, packagingSiteList)(request, messages(application)).toString
        page.title() must include("You added 1 packaging site")
        page.getElementsByTag("h1").text() mustEqual "You added 1 packaging site"
        page.getElementById("value").`val`() mustEqual "true"
      }
    }

    "must redirect to the next page when valid data is submitted (true with subscription)" in {
      val mockSessionRepository = mock[SessionRepository]
      val mockAddressLookupService = mock[AddressLookupService]
      val onwardUrlForALF = "foobarwizz"

      when(mockSessionRepository.set(any())) thenReturn Future.successful(Right(true))
      when(mockAddressLookupService.initJourneyAndReturnOnRampUrl(
        ArgumentMatchers.eq(PackingDetails), ArgumentMatchers.any(), ArgumentMatchers.any())(
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
          FakeRequest(POST, packagingSiteDetailsRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardUrlForALF

        verify(mockAddressLookupService, times(1)).initJourneyAndReturnOnRampUrl(
          ArgumentMatchers.eq(PackingDetails), ArgumentMatchers.any(), ArgumentMatchers.any())(
            ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any())
      }
    }

    "must redirect to check your answers when user does not match new importer when the data is submitted" in {

      val mockSessionRepository = mock[SessionRepository]
      val mockSdilConnector = mock[SoftDrinksIndustryLevyConnector]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(Right(true))
      when(mockSdilConnector.retrieveSubscription(any(), any())(any())) thenReturn Future.successful(None)

      val application =
        applicationBuilder(userAnswers = Some(userAnswersWith1PackagingSite))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[SoftDrinksIndustryLevyConnector].toInstance(mockSdilConnector))
          .build()

      running(application) {
        val request =
          FakeRequest(POST, packagingSiteDetailsRoute)
            .withFormUrlEncodedBody(("value", "false"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.CheckYourAnswersController.onPageLoad.url
      }
    }

    "must redirect to check your answers when user does not match new importer when the data is submitted in check mode" in {

      val mockSessionRepository = mock[SessionRepository]
      val mockSdilConnector = mock[SoftDrinksIndustryLevyConnector]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(Right(true))
      when(mockSdilConnector.retrieveSubscription(any(), any())(any())) thenReturn Future.successful(Some(aSubscription))

      val application =
        applicationBuilder(userAnswers = Some(newImporterAnswer))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[SoftDrinksIndustryLevyConnector].toInstance(mockSdilConnector))
          .build()

      running(application) {
        val request =
          FakeRequest(POST, checkPackagingSiteDetailsRoute)
            .withFormUrlEncodedBody(("value", "false"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.CheckYourAnswersController.onPageLoad.url
      }
    }

    "must redirect to ask secondary warehouse when user does match new importer when the data is submitted" in {

      val mockSessionRepository = mock[SessionRepository]
      val mockSdilConnector = mock[SoftDrinksIndustryLevyConnector]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(Right(true))
      when(mockSdilConnector.retrieveSubscription(any(), any())(any())) thenReturn Future.successful(Some(aSubscription))

      val application =
        applicationBuilder(userAnswers = Some(newImporterAnswer))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[SoftDrinksIndustryLevyConnector].toInstance(mockSdilConnector))
          .build()

      running(application) {
        val request =
          FakeRequest(POST, packagingSiteDetailsRoute)
            .withFormUrlEncodedBody(("value", "false"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.AskSecondaryWarehouseInReturnController.onPageLoad(NormalMode).url
      }
    }

    "must redirect to journey recovery when user doesn't have a user answers but has a subscription when the data is submitted" in {

      val mockSessionRepository = mock[SessionRepository]
      val mockSdilConnector = mock[SoftDrinksIndustryLevyConnector]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(Right(true))
      when(mockSdilConnector.retrieveSubscription(any(), any())(any())) thenReturn Future.successful(Some(aSubscription))

      val application =
        applicationBuilder(userAnswers = None)
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[SoftDrinksIndustryLevyConnector].toInstance(mockSdilConnector))
          .build()

      running(application) {
        val request =
          FakeRequest(POST, packagingSiteDetailsRoute)
            .withFormUrlEncodedBody(("value", "false"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to journey recovery when user doesn't have a subscription or user answers when the data is submitted" in {

      val mockSessionRepository = mock[SessionRepository]
      val mockSdilConnector = mock[SoftDrinksIndustryLevyConnector]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(Right(true))
      when(mockSdilConnector.retrieveSubscription(any(), any())(any())) thenReturn Future.successful(None)

      val application =
        applicationBuilder(userAnswers = None)
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[SoftDrinksIndustryLevyConnector].toInstance(mockSdilConnector))
          .build()

      running(application) {
        val request =
          FakeRequest(POST, packagingSiteDetailsRoute)
            .withFormUrlEncodedBody(("value", "false"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, packagingSiteDetailsRoute)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[PackagingSiteDetailsView]

        val result = route(application, request).value
        val page = Jsoup.parse(contentAsString(result))

        status(result) mustEqual BAD_REQUEST
        page.getElementsByTag("h2").text() must include("There is a problem")
        contentAsString(result) mustEqual view(boundForm, NormalMode, Map.empty)(request, messages(application)).toString
        page.getElementById("value-error").text() must include("Select yes if you need to add another packaging site")
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, packagingSiteDetailsRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, packagingSiteDetailsRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
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
          val request = FakeRequest(POST, packagingSiteDetailsRoute).withFormUrlEncodedBody(("value", "false"))
          await(route(app, request).value)
          events.collectFirst {
            case event =>
              event.getLevel.levelStr mustEqual "ERROR"
              event.getMessage mustEqual "Failed to set value in session repository while attempting set on packagingSiteDetails"
          }.getOrElse(fail("No logging captured"))
        }
      }
    }

  }
}
