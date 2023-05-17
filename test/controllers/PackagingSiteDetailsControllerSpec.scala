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
import connectors.SoftDrinksIndustryLevyConnector
import forms.PackagingSiteDetailsFormProvider
import models.{NormalMode, UserAnswers}
import org.jsoup.Jsoup
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.PackagingSiteDetailsPage
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import viewmodels.govuk.SummaryListFluency
import views.html.PackagingSiteDetailsView

import scala.concurrent.Future

class PackagingSiteDetailsControllerSpec extends SpecBase with MockitoSugar with  SummaryListFluency{

  val formProvider = new PackagingSiteDetailsFormProvider()
  val form = formProvider()

  lazy val packagingSiteDetailsRoute: String = routes.PackagingSiteDetailsController.onPageLoad(NormalMode).url
  lazy val userAnswersWith1PackagingSite: UserAnswers = UserAnswers(sdilNumber, Json.obj(), List.empty, packagingSiteListWith1)

  "packagingSiteDetails Controller" - {

    "must redirect to returns sent page if return is already submitted" in {
      val application = applicationBuilder(userAnswers = Some(submittedAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, packagingSiteDetailsRoute)
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.ReturnSentController.onPageLoad().url
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
        page.title() must include("You added 1 packaging sites")
        page.getElementsByTag("h1").text() mustEqual "You added 1 packaging sites"
        page.getElementsByTag("h2").text() must include("Do you want to add another UK packaging site?")
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
        page.title() must include("You added 1 packaging sites")
        page.getElementsByTag("h1").text() mustEqual "You added 1 packaging sites"
        page.getElementById("value").`val`() mustEqual "true"
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val mockSessionRepository = mock[SessionRepository]
      val mockSdilConnector = mock[SoftDrinksIndustryLevyConnector]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)
      when(mockSdilConnector.retrieveSubscription(any(), any())(any())) thenReturn Future.successful(None)

      val application =
        applicationBuilder(userAnswers = Some(userAnswersWith1PackagingSite))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[SoftDrinksIndustryLevyConnector].toInstance(mockSdilConnector)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, packagingSiteDetailsRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
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
  }
}
