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
import forms.RemoveSmallProducerConfirmFormProvider
import helpers.LoggerHelper
import models.{NormalMode, UserAnswers}
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
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import utilitlies.GenericLogger
import views.html.RemoveSmallProducerConfirmView

import scala.concurrent.Future


class RemoveSmallProducerConfirmControllerSpec extends SpecBase with MockitoSugar with LoggerHelper {

  def onwardRoute: Call = Call("GET", "/foo")

  val formProvider = new RemoveSmallProducerConfirmFormProvider()
  val form: Form[Boolean] = formProvider()

  val userAnswersData: JsObject = Json.obj(
    RemoveSmallProducerConfirmPage.toString -> Json.obj(
      "producerName" -> producerName,
      "referenceNumber" -> sdilReference,
      "lowBand" -> litres,
      "highBand" -> litres
    )
  )
  val userAnswers: UserAnswers = emptyUserAnswers.copy(data = userAnswersData, smallProducerList = smallProducerList)
  val userAnswersWithTwoProducers: UserAnswers = emptyUserAnswers.copy(data = userAnswersData, smallProducerList = smallProducerListWithTwoProducers)
  val userAnswersWithOneProducer: UserAnswers = emptyUserAnswers.copy(data = userAnswersData, smallProducerList = smallProducerListOnlySuperCola)

  lazy val removeSmallProducerConfirmRoute: String = routes.RemoveSmallProducerConfirmController.onPageLoad(NormalMode, s"$sdilReferenceParty").url

  "RemoveSmallProducerConfirm Controller" - {

    "must redirect to returns sent page if return is already submitted" in {
      val application = applicationBuilder(userAnswers = Some(submittedAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, removeSmallProducerConfirmRoute)
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.ReturnSentController.onPageLoad.url
      }
    }

    "must redirect to returns sent page if return is already submitted when hitting the submit" in {
      val application = applicationBuilder(userAnswers = Some(submittedAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, removeSmallProducerConfirmRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.ReturnSentController.onPageLoad.url
      }
    }

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
      val userAnswers = emptyUserAnswers.copy(data = userAnswersData, smallProducerList = smallProducerList).set(RemoveSmallProducerConfirmPage, false).success.value
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
      val userAnswers = emptyUserAnswers.copy(data = userAnswersData, smallProducerList = smallProducerList).set(RemoveSmallProducerConfirmPage, true).success.value
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
      val userAnswers = emptyUserAnswers.copy(data = userAnswersData, smallProducerList = smallProducerListWithTwoProducers).set(RemoveSmallProducerConfirmPage, true).success.value
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
      val userAnswers = emptyUserAnswers.copy(data = userAnswersData, smallProducerList = smallProducerListWithTwoProducers).set(
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
      val userAnswers = emptyUserAnswers.copy(data = userAnswersData, smallProducerList = smallProducerList).set(RemoveSmallProducerConfirmPage, true).success.value
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

      val mockSessionRepository = mock[SessionRepository]
      when(mockSessionRepository.set(any())) thenReturn Future.successful(Right(true))

      val application =
        applicationBuilder(userAnswers = Some(failingUserAnswers))
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

      val application = applicationBuilder(userAnswers = Some(failingUserAnswers)).build()

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
        applicationBuilder(Some(emptyUserAnswers.copy(smallProducerList = smallProducerList)))
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
