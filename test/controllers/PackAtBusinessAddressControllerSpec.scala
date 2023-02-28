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
import forms.PackAtBusinessAddressFormProvider
import models.{NormalMode, UserAnswers}
import org.mockito.ArgumentMatchers.{any, anyString, eq => matching}
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.PackAtBusinessAddressPage
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import views.html.{HowManyAsAContractPackerView, PackAtBusinessAddressView}
import models.{NormalMode, ReturnPeriod, SmallProducer, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.fluentlenium.core.filter.matcher.ContainsWordMatcher
import org.jsoup.Jsoup
import play.api.i18n.Messages
import play.api.mvc.Call

import scala.concurrent.Future

class PackAtBusinessAddressControllerSpec extends SpecBase with MockitoSugar {
  def onwardRoute = Call("GET", "#")
  val formProvider = new PackAtBusinessAddressFormProvider()
  val form = formProvider()
  val mockSessionRepository = mock[SessionRepository]
  val mockSdilConnector = mock[SoftDrinksIndustryLevyConnector]
  var usersRetrievedSubscription = aSubscription
  val businessName = usersRetrievedSubscription.orgName
  val businessAddress = usersRetrievedSubscription.address


  lazy val packAtBusinessAddressRoute = routes.PackAtBusinessAddressController.onPageLoad(NormalMode).url

  "PackAtBusinessAddress Controller" - {

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

      val userAnswers = UserAnswers(sdilNumber).set(PackAtBusinessAddressPage, true).success.value

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
        contentAsString(result) mustEqual view(form.fill(true), businessName, businessAddress, NormalMode)(request, messages(application)).toString
        page.title() must include(Messages("packAtBusinessAddress.title"))
        page.getElementsByTag("h1").text() mustEqual Messages("packAtBusinessAddress.title")
        page.getElementsContainingText(usersRetrievedSubscription.orgName).toString == true
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
//            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, packAtBusinessAddressRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

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
        contentAsString(result) mustEqual view(boundForm, businessName, businessAddress, NormalMode)(request, messages(application)).toString

        page.getElementsContainingText(usersRetrievedSubscription.orgName).toString == true
        page.getElementsContainingText(usersRetrievedSubscription.address.toString).`val`() == true
        page.getElementsByTag("a").text() must include(Messages("packAtBusinessAddress.required"))

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
  }
}
