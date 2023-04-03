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
import models.backend.{Site, UkAddress}
import models.{NormalMode, UserAnswers}
import org.jsoup.Jsoup
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.PackagingSiteDetailsPage
import play.api.i18n.Messages
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{SummaryList, SummaryListRow}
import viewmodels.checkAnswers.PackagingSiteDetailsSummary
import viewmodels.govuk.SummaryListFluency
import views.html.PackagingSiteDetailsView

import scala.concurrent.Future

class PackagingSiteDetailsControllerSpec extends SpecBase with MockitoSugar with  SummaryListFluency{

  val PackagingSite1 = Site(
    UkAddress(List("33 Rhes Priordy", "East London"), "E73 2RP"),
    None,
    Some("Wild Lemonade Group"),
    None)

  val PackagingSite2 = Site(
    UkAddress(List("30 Rhes Priordy", "East London"), "E73 2RP"),
    Some("10"),
    None,
    None)

  val formProvider = new PackagingSiteDetailsFormProvider()
  val form = formProvider()

  lazy val packagingSiteDetailsRoute = routes.PackagingSiteDetailsController.onPageLoad(NormalMode).url
  val packagingSiteListWith2 = List(PackagingSite1, PackagingSite2)
  val packagingSiteListWith1 = List(PackagingSite1)

  val userAnswersWith1PackagingSite = UserAnswers(sdilNumber, Json.obj(), List.empty, packagingSiteListWith1)
  val userAnswersWith2PackagingSites = UserAnswers(sdilNumber, Json.obj(), List.empty, packagingSiteListWith2)

  "packagingSiteDetails Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(userAnswersWith1PackagingSite)).build()

      running(application) {
        val request = FakeRequest(GET, packagingSiteDetailsRoute)

        val result = route(application, request).value

        val packagingSummaryList: List[SummaryListRow] =
          PackagingSiteDetailsSummary.row2(List())(messages(application))

        SummaryListViewModel(
          rows = packagingSummaryList
        )

        application.injector.instanceOf[PackagingSiteDetailsView]

        status(result) mustEqual OK
        val page = Jsoup.parse(contentAsString(result))

        page.title() must include("You added 1 packaging sites")
        page.getElementsByTag("h1").text() mustEqual "You added 1 packaging sites"
        page.getElementsByTag("h2").text() must include("Do you want to add another UK packaging site?")
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      UserAnswers(sdilNumber).set(PackagingSiteDetailsPage, true).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswersWith1PackagingSite)).build()

      running(application) {
        val request = FakeRequest(GET, packagingSiteDetailsRoute)
          .withFormUrlEncodedBody(("value", "true"))

        application.injector.instanceOf[PackagingSiteDetailsView]

        val result = route(application, request).value
        val packagingSiteSummaryList: List[SummaryListRow] =
          PackagingSiteDetailsSummary.row2(List())(messages(application))

        SummaryListViewModel(
          rows = packagingSiteSummaryList
        )

        status(result) mustEqual OK
        val page = Jsoup.parse(contentAsString(result))
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

        val smallProducersSummaryList: List[SummaryListRow] =
          PackagingSiteDetailsSummary.row2(List())(messages(application))

        val list: SummaryList = SummaryListViewModel(
          rows = smallProducersSummaryList
        )

        val result = route(application, request).value
        val page = Jsoup.parse(contentAsString(result))

        status(result) mustEqual BAD_REQUEST
        page.getElementsByTag("h2").text() must include("There is a problem")
        contentAsString(result) mustEqual view(boundForm, NormalMode, list)(request, messages(application)).toString
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
