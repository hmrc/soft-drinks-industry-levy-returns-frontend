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
import forms.RemoveWarehouseConfirmFormProvider
import models.backend.UkAddress
import models.{NormalMode, UserAnswers, Warehouse}
import navigation.{FakeNavigator, Navigator}
import org.jsoup.Jsoup
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.RemoveWarehouseConfirmPage
import play.api.i18n.Messages
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.twirl.api.Html
import repositories.SessionRepository
import views.html.RemoveWarehouseConfirmView

import scala.concurrent.Future

class RemoveWarehouseConfirmControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute = Call("GET", "/foo")

  val formProvider = new RemoveWarehouseConfirmFormProvider()
  val form = formProvider()
  val testIndex = "1"
  val testUkAddress = Html("Wild Lemonade Group<br>33, Rhes, Priordy, East London, E73 2RP")
  val warehouseMap = Map(("1",Warehouse(
    tradingName = Some("Wild Lemonade Group"),
    address = UkAddress(List("33","Rhes", "Priordy","East London"), "E73 2RP" ),
  )))

  val userAnswersData = Json.obj(
    RemoveWarehouseConfirmPage.toString -> Json.obj(
      "tradingName" -> "Wild Lemonade Group",
      "address" -> UkAddress(List("33","Rhes","Priordy","East London"), "E73 2RP")
    )
  )

  val userAnswers = UserAnswers(sdilNumber, userAnswersData, List.empty, Map.empty, warehouseMap)

  lazy val removePackingSiteRoute = routes.RemoveWarehouseConfirmController.onPageLoad(s"$testIndex").url

  "RemovePackingSite Controller" - {

    "must redirect to returns sent page if return is already submitted" in {
      val application = applicationBuilder(userAnswers = Some(submittedAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, removePackingSiteRoute)
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.ReturnSentController.onPageLoad().url
      }
    }

    "must redirect to returns sent page if return is already submitted when hitting the submit" in {
      val ref: String = "foo"
      val application = applicationBuilder(userAnswers = Some(submittedAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, removePackingSiteRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.ReturnSentController.onPageLoad().url
      }
    }

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, removePackingSiteRoute)
        val result = route(application, request).value
        val view = application.injector.instanceOf[RemoveWarehouseConfirmView]

        status(result) mustEqual OK
        val page = Jsoup.parse(contentAsString(result))
        page.title() must include(Messages("removeWarehouseConfirm.title"))
        page.getElementsByTag("h1").text() must include(Messages("removeWarehouseConfirm.title"))
        page.getElementById("warehouseToRemove").text() mustBe testUkAddress.toString().replace("<br>", " ")
        contentAsString(result) mustEqual view(form, NormalMode , testUkAddress, testIndex)(request, messages(application)).toString
      }
    }

    "must redirect to the next page when valid data is submitted (true)" in {

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(UserAnswers(sdilNumber, Json.obj(), List.empty, Map.empty, warehouseMap)))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, removePackingSiteRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must redirect to the next page when valid data is submitted (false)" in {

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(UserAnswers(sdilNumber, Json.obj(), List.empty, Map.empty, warehouseMap)))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, removePackingSiteRoute)
            .withFormUrlEncodedBody(("value", "false"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(UserAnswers(sdilNumber, Json.obj(), List.empty, Map.empty, warehouseMap))).build()

      running(application) {
        val request =
          FakeRequest(POST, removePackingSiteRoute)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[RemoveWarehouseConfirmView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode, testUkAddress, testIndex)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, removePackingSiteRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, removePackingSiteRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
