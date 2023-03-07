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
import forms.productionSiteDetailsFormProvider
import models.backend.{Site, UkAddress}
import models.{NormalMode, ProductionSite, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.ProductionSiteDetailsPage
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{SummaryList, SummaryListRow}
import viewmodels.checkAnswers.productionSiteDetailsSummary
import viewmodels.govuk.SummaryListFluency
import views.html.productionSiteDetailsView

import java.time.LocalDate
import scala.concurrent.Future

class productionSiteDetailsControllerSpec extends SpecBase with MockitoSugar with  SummaryListFluency{

  def onwardRoute = Call("GET", "/foo")

  val superCola = Site(
    UkAddress(List("33 Rhes Priordy", "East London"), "E73 2RP"),
    Some("88"),
    Some("Wild Lemonade Group"),
    Some(LocalDate.of(2018, 2, 26)))

  val sparkyJuice = Site(
    UkAddress(List("30 Rhes Priordy", "East London"), "E73 2RP"),
    Some("10"),
    Some("Sparky Juice Co"),
    Some(LocalDate.of(2018, 2, 26)))

  val formProvider = new productionSiteDetailsFormProvider()
  val form = formProvider()

  lazy val productionSiteDetailsRoute = routes.ProductionSiteDetailsController.onPageLoad(NormalMode).url

  "productionSiteDetails Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, productionSiteDetailsRoute)

        val result = route(application, request).value

        val smallProducersSummaryList: List[SummaryListRow] =
          productionSiteDetailsSummary.row2(List())(messages(application))

        val list: SummaryList = SummaryListViewModel(
          rows = smallProducersSummaryList
        )

        val view = application.injector.instanceOf[productionSiteDetailsView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode, list)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = UserAnswers(sdilNumber).set(ProductionSiteDetailsPage, true).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, productionSiteDetailsRoute)

        val view = application.injector.instanceOf[productionSiteDetailsView]

        val result = route(application, request).value

        val smallProducersSummaryList: List[SummaryListRow] =
          productionSiteDetailsSummary.row2(List())(messages(application))

        val list: SummaryList = SummaryListViewModel(
          rows = smallProducersSummaryList
        )

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(true), NormalMode, list)(request, messages(application)).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val mockSessionRepository = mock[SessionRepository]
      val mockSdilConnector = mock[SoftDrinksIndustryLevyConnector]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)
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
          FakeRequest(POST, productionSiteDetailsRoute)
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
          FakeRequest(POST, productionSiteDetailsRoute)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[productionSiteDetailsView]

        val smallProducersSummaryList: List[SummaryListRow] =
          productionSiteDetailsSummary.row2(List())(messages(application))

        val list: SummaryList = SummaryListViewModel(
          rows = smallProducersSummaryList
        )

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode, list)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, productionSiteDetailsRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, productionSiteDetailsRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
