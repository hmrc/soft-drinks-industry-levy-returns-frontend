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
import forms.SmallProducerDetailsFormProvider
import models.{NormalMode, SmallProducer, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.SmallProducerDetailsPage
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{SummaryList, SummaryListRow}
import viewmodels.checkAnswers.SmallProducerDetailsSummary
import viewmodels.govuk.SummaryListFluency
import views.html.SmallProducerDetailsView

import scala.concurrent.Future

class SmallProducerDetailsControllerSpec extends SpecBase with MockitoSugar with  SummaryListFluency {

  def onwardRoute = Call("GET", "/foo")

  val superCola = SmallProducer("Super Cola Ltd", "XCSDIL000000069", (1L, 1L))
  val sparkyJuice = SmallProducer("Sparky Juice Co", "XCSDIL000000070", (100L, 100L))

  val formProvider = new SmallProducerDetailsFormProvider()
  val form = formProvider()

  lazy val smallProducerDetailsRoute = routes.SmallProducerDetailsController.onPageLoad(NormalMode).url

  "SmallProducerDetails Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        implicit val request = FakeRequest(GET, smallProducerDetailsRoute)

        val result = route(application, request).value

        val smallProducersSummaryList: List[SummaryListRow] =
          SmallProducerDetailsSummary.row2(List())(messages(application))

        val list: SummaryList = SummaryListViewModel(
          rows = smallProducersSummaryList
        )

        val view = application.injector.instanceOf[SmallProducerDetailsView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode, list)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = UserAnswers(sdilNumber).set(SmallProducerDetailsPage, true).success.value
      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, smallProducerDetailsRoute)
        val view = application.injector.instanceOf[SmallProducerDetailsView]
        val result = route(application, request).value
        val smallProducersSummaryList: List[SummaryListRow] =
          SmallProducerDetailsSummary.row2(List())(messages(application))

        val list: SummaryList = SummaryListViewModel(
          rows = smallProducersSummaryList
        )

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(true), NormalMode, list)(request, messages(application)).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, smallProducerDetailsRoute)
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
          FakeRequest(POST, smallProducerDetailsRoute)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[SmallProducerDetailsView]

        val result = route(application, request).value
        val smallProducersSummaryList: List[SummaryListRow] =
          SmallProducerDetailsSummary.row2(List())(messages(application))
        val list: SummaryList = SummaryListViewModel(
          rows = smallProducersSummaryList
        )


        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode, list)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing user answers data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, smallProducerDetailsRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing user answers data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, smallProducerDetailsRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }


    "must say 0 small producers added when no small producers available" in {

      val userAnswers = UserAnswers(sdilNumber)
      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, smallProducerDetailsRoute)
        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) must include("You added 0 small producers")
      }
    }

    "must include added small producer SDIL reference on the page" in {

      val userAnswers = UserAnswers(sdilNumber,Json.obj(),List(superCola))
      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, smallProducerDetailsRoute)
        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) must include(superCola.sdilRef)
      }
    }

    "must include added small producer sdil reference on the page" in {

      val userAnswers = UserAnswers(sdilNumber, Json.obj(), List(superCola))
      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()
      val smallProducersSummaryList = SmallProducerDetailsSummary.row2(List(superCola))(messages(application))
      val summaryList = SummaryListViewModel(rows = smallProducersSummaryList)

      running(application) {
        val request = FakeRequest(GET, smallProducerDetailsRoute)
        val result = route(application, request).value
        val view = application.injector.instanceOf[SmallProducerDetailsView]

        status(result) mustEqual OK
        contentAsString(result) must include(superCola.sdilRef)
        contentAsString(result) mustEqual view(form, NormalMode, summaryList)(request, messages(application)).toString
      }
    }

  }
}
