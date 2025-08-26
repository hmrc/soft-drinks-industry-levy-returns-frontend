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
import connectors.SoftDrinksIndustryLevyConnector
import errors.SessionDatabaseInsertError
import forms.PackAtBusinessAddressFormProvider
import helpers.LoggerHelper
import models.NormalMode
import models.backend.UkAddress
import models.retrieved.RetrievedSubscription
import navigation.{FakeNavigator, Navigator}
import org.jsoup.Jsoup
import org.mockito.ArgumentMatchers.{any, anyString, eq as matching}
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.*
import org.scalatest.matchers.should.Matchers.should
import org.scalatestplus.mockito.MockitoSugar
import pages.PackAtBusinessAddressPage
import play.api.data.Form
import play.api.i18n.Messages
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import services.{AddressLookupService, PackingDetails}
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import util.GenericLogger
import views.html.PackAtBusinessAddressView

import scala.concurrent.Future

class PackAtBusinessAddressControllerSpec extends SpecBase with MockitoSugar with LoggerHelper {

  def onwardRoute: Call = Call("GET", "/foo")

  val formProvider = new PackAtBusinessAddressFormProvider()
  val form: Form[Boolean] = formProvider()
  val mockSessionRepository: SessionRepository = mock[SessionRepository]
  val mockSdilConnector: SoftDrinksIndustryLevyConnector = mock[SoftDrinksIndustryLevyConnector]
  var usersRetrievedSubscription: RetrievedSubscription = aSubscription
  val businessName: String = usersRetrievedSubscription.orgName
  val businessAddress: UkAddress = usersRetrievedSubscription.address
  val formattedAddress = "Super Lemonade Plc<br/>63 Clifton Roundabout<br/>Worcester<br/>WR53 7CX"

  lazy val packAtBusinessAddressRoute: String = routes.PackAtBusinessAddressController.onPageLoad(NormalMode).url

  "PackAtBusinessAddress Controller" - {

    "must redirect to returns sent page if return is already submitted" in {
      val application = applicationBuilder(userAnswers = Some(submittedAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, packAtBusinessAddressRoute)
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.ReturnSentController.onPageLoad.url
      }
    }

    "must redirect to returns sent page if return is already submitted when hitting the submit" in {
      val application = applicationBuilder(userAnswers = Some(submittedAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, packAtBusinessAddressRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.ReturnSentController.onPageLoad.url
      }
    }

    "must return OK and the User Company name and address for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, packAtBusinessAddressRoute)
        when(mockSdilConnector.retrieveSubscription(matching("XCSDIL000000002"), anyString())(any())).thenReturn {
          Future.successful(Some(aSubscription))
        }
        val result = route(application, request).value
        val page = Jsoup.parse(contentAsString(result))
        status(result) mustEqual OK

        page.body().`val`() contains businessName
        page.body().`val`() contains businessAddress

      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = emptyUserAnswers.set(PackAtBusinessAddressPage, true).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, packAtBusinessAddressRoute)

        val view = application.injector.instanceOf[PackAtBusinessAddressView]
        when(mockSdilConnector.retrieveSubscription(matching("XCSDIL000000002"), anyString())(any())).thenReturn {
          Future.successful(Some(aSubscription))
        }
        val result = route(application, request).value
        val page = Jsoup.parse(contentAsString(result))

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(true), HtmlContent(formattedAddress), NormalMode)(request, messages(application)).toString
        page.title() must include(Messages("packAtBusinessAddress.title"))
        page.getElementsByTag("h1").text() mustEqual Messages("packAtBusinessAddress.title")
        //noinspection ComparingUnrelatedTypes
        page.getElementsContainingText(usersRetrievedSubscription.orgName).toString should not be empty
      }
    }

    "must redirect to the next page when valid data is submitted (false)" in {
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
          FakeRequest(POST, packAtBusinessAddressRoute)
            .withFormUrlEncodedBody(("value", "false"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardUrlForALF

        verify(mockAddressLookupService, times(1)).initJourneyAndReturnOnRampUrl(
          ArgumentMatchers.eq(PackingDetails), ArgumentMatchers.any(), ArgumentMatchers.any())(
            ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any())
      }
    }

    "must return a Bad Request, continue to have the correct information on the page, and answer required error " +
      "when invalid data is submitted" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          val request =
            FakeRequest(POST, packAtBusinessAddressRoute)
              .withFormUrlEncodedBody(("value", ""))

          val boundForm = form.bind(Map("value" -> ""))

          val view = application.injector.instanceOf[PackAtBusinessAddressView]

          val result = route(application, request).value
          val page = Jsoup.parse(contentAsString(result))

          status(result) mustEqual BAD_REQUEST
          contentAsString(result) mustEqual view(boundForm, HtmlContent(formattedAddress), NormalMode)(request, messages(application)).toString

          //noinspection ComparingUnrelatedTypes
          page.getElementsContainingText(usersRetrievedSubscription.orgName).toString should not be empty
          //noinspection ComparingUnrelatedTypes
          page.getElementsContainingText(usersRetrievedSubscription.address.toString).`val`() should not be empty
          page.getElementsByTag("a").text() must include(Messages("packAtBusinessAddress.error.required"))

        }
      }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, packAtBusinessAddressRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, packAtBusinessAddressRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to the next page removing litre data from user answers, when valid data is submitted (true)" in {

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(Right(true))

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)).build()

      running(application) {
        val request = FakeRequest(POST, packAtBusinessAddressRoute).withFormUrlEncodedBody(("value", "true"))
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.PackagingSiteDetailsController.onPageLoad(NormalMode).url
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
          val request = FakeRequest(POST, packAtBusinessAddressRoute).withFormUrlEncodedBody(("value", "true"))
          await(route(app, request).value)
          events.collectFirst {
            case event =>
              event.getLevel.levelStr mustEqual "ERROR"
              event.getMessage mustEqual "Failed to set value in session repository while attempting set on packAtBusinessAddress"
          }.getOrElse(fail("No logging captured"))
        }
      }
    }

  }
}
