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
import models.NormalMode
import navigation.{ FakeNavigator, Navigator }
import org.mockito.ArgumentMatchers.any

import org.mockito.MockitoSugar
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import views.html.ReturnChangeRegistrationView

import scala.concurrent.Future

class ReturnChangeRegistrationControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute: Call = Call("GET", "/foo")

  lazy val returnChangeRegistrationRoute: String = routes.ReturnChangeRegistrationController.onPageLoad().url

  "ReturnChangeRegistration Controller" - {

    "must redirect to returns sent page if return is already submitted" in {
      val application = applicationBuilder(userAnswers = Some(submittedAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, routes.ReturnChangeRegistrationController.onPageLoad().url)
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.ReturnSentController.onPageLoad.url
      }
    }

    "must redirect to returns sent page if return is already submitted when hitting the submit" in {
      val application = applicationBuilder(userAnswers = Some(submittedAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, routes.ReturnChangeRegistrationController.onPageLoad().url)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.ReturnSentController.onPageLoad.url
      }
    }

    "must return OK and the correct view for a GET with a link back to Co-packer question if new copacker" in {

      val application = applicationBuilder(userAnswers = Some(completedUserAnswers), subscription = Some(aSubscription)).build()
      val urlLink: String = routes.PackagedContractPackerController.onPageLoad(NormalMode).url
      running(application) {
        val request = FakeRequest(GET, routes.ReturnChangeRegistrationController.onPageLoad().url)
        val result = route(application, request).value
        val view = application.injector.instanceOf[ReturnChangeRegistrationView]

        status(result) mustEqual OK
        contentAsString(result) must include(routes.PackagedContractPackerController.onPageLoad(NormalMode).url)
        contentAsString(result) mustEqual view(urlLink)(request, messages(application)).toString
        urlLink mustEqual "/soft-drinks-industry-levy-returns-frontend/packaged-as-contract-packer"
      }
    }

    "must return OK and the correct view for a GET with a link back to Brought into UK question if new importer" in {
      val application = applicationBuilder(userAnswers = Some(completedUserAnswers), subscription = Some(subscriptionWithCopacker)).build()

      running(application) {
        val request = FakeRequest(GET, routes.ReturnChangeRegistrationController.onPageLoad().url)
        val urlLink: String = routes.BroughtIntoUKController.onPageLoad(NormalMode).url
        val result = route(application, request).value

        val view = application.injector.instanceOf[ReturnChangeRegistrationView]

        status(result) mustEqual OK
        contentAsString(result) must include(routes.BroughtIntoUKController.onPageLoad(NormalMode).url)
        contentAsString(result) mustEqual view(urlLink)(request, messages(application)).toString
        urlLink mustEqual "/soft-drinks-industry-levy-returns-frontend/brought-into-uk"
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
          FakeRequest(POST, returnChangeRegistrationRoute)
            .withFormUrlEncodedBody(("value", ""))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }
  }
}
