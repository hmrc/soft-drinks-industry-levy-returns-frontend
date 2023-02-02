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
import forms.AddASmallProducerFormProvider
import models.requests.DataRequest
import models.{AddASmallProducer, NormalMode, SmallProducer, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.jsoup.Jsoup
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.AddASmallProducerPage
import play.api.i18n.{Lang, Messages, MessagesApi, MessagesImpl}
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.mvc.{Call, MessagesControllerComponents}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import views.html.AddASmallProducerView

import scala.concurrent.Future

class AddASmallProducerControllerSpec extends SpecBase with MockitoSugar {

//  val superCola = SmallProducer("Super Cola Ltd", "XCSDIL000000069", (1L, 1L))
//  val sparkyJuice = SmallProducer("Sparky Juice Co", "XCSDIL000000070", (100L, 100L))

  val formProvider = new AddASmallProducerFormProvider()
  val mockSessionRepository = mock[SessionRepository]
  val form = formProvider()
  val producerName = "Party Drinks Group"
  val sdilReference = "XPSDIL000000116"
  val bandMax = 100000000000000L
  val litres = 20L

  lazy val addASmallProducerRoute = routes.AddASmallProducerController.onPageLoad(NormalMode).url

  val userAnswers = UserAnswers(
    sdilNumber,
    Json.obj(
      AddASmallProducerPage.toString -> Json.obj(
        "producerName" -> producerName,
        "referenceNumber" -> sdilReference,
        "lowBand" -> litres,
        "highBand" -> litres
      )
    )
  )

  "AddASmallProducer Controller" - {

    "must return OK with correct page title and header" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()
      //TODO - use messages instead of literals
      val expectedPageTitle = "Enter the registered small producer’s details - soft-drinks-industry-levy-returns-frontend - GOV.UK"
      val expectedH1 = "Enter the registered small producer’s details"

      running(application) {
        val request = FakeRequest(GET, addASmallProducerRoute)
        val result = route(application, request).value

        status(result) mustEqual OK
        val page = Jsoup.parse(contentAsString(result))

        page.title() must include(Messages("addASmallProducer.title"))
        page.getElementsByTag("h1").text() mustEqual Messages("addASmallProducer.heading")
      }
    }

//    "must return OK and contain correct form fields" in {
//
//      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()
//      val smallProducerNameLabel = "Small producer name (optional)" //      addASmallProducer.hint1 =
//      val sdilReferenceLabel = "Soft Drinks Industry Levy reference number" // addASmallProducer.referenceNumber =
//      val lowBandLabel = "Litres in the low band" // addASmallProducer.lowBand =
//      val highBandLabel = "Litres in the high band" // addASmallProducer.highBand =
//
//      running(application) {
//        val request = FakeRequest(GET, addASmallProducerRoute)
//        val result = route(application, request).value
//
//        status(result) mustEqual OK
//        val page = Jsoup.parse(contentAsString(result))
//
//        page.title() must include(expectedPageTitle)
//        page.getElementsByTag("h1").text() mustEqual expectedH1
//      }
//    }
//
//    "must redirect to the next page when valid data is submitted" in {
//
//      val mockSessionRepository = mock[SessionRepository]
//
//      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)
//
//      val application =
//        applicationBuilder(userAnswers = Some(emptyUserAnswers))
//          .overrides(
//            bind[SessionRepository].toInstance(mockSessionRepository)
//          )
//          .build()
//
//      running(application) {
//        val request =
//          FakeRequest(POST, addASmallProducerRoute)
//            .withFormUrlEncodedBody(("lowBand", producerName.toString), ("highBand", sdilReference.toString))
//
//        val result = route(application, request).value
//
//        status(result) mustEqual SEE_OTHER
//      }
//    }
//
//    "must return a Bad Request and errors when invalid data is submitted" in {
//
//      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()
//
//      running(application) {
//        val request =
//          FakeRequest(POST, addASmallProducerRoute)
//            .withFormUrlEncodedBody(("value", "invalid value"))
//
//        val boundForm = form.bind(Map("value" -> "invalid value"))
//
//        val view = application.injector.instanceOf[AddASmallProducerView]
//
//        val result = route(application, request).value
//
//        status(result) mustEqual BAD_REQUEST
//        contentAsString(result) mustEqual view(boundForm, NormalMode)(request, messages(application)).toString
//      }
//    }
//
//    "must redirect to Journey Recovery for a GET if no existing user answers data is found" in {
//
//      val application = applicationBuilder(userAnswers = None).build()
//
//      running(application) {
//        val request = FakeRequest(GET, addASmallProducerRoute)
//
//        val result = route(application, request).value
//
//        status(result) mustEqual SEE_OTHER
//        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
//      }
//    }
//
//    "must redirect to Journey Recovery for a POST if no existing user answers data is found" in {
//
//      val application = applicationBuilder(userAnswers = None).build()
//
//      running(application) {
//        val request =
//          FakeRequest(POST,  addASmallProducerRoute)
//            .withFormUrlEncodedBody(("value", "true"))
//
//        val result = route(application, request).value
//
//        status(result) mustEqual SEE_OTHER
//        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
//      }
//    }
//
//    "Small producer reference number must be different to reference currently submitting the returns" in {
//      val mockSessionRepository = mock[SessionRepository]
//
//      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)
//
//      val application =
//        applicationBuilder(userAnswers = Some(emptyUserAnswers))
//          .overrides(bind[SessionRepository].toInstance(mockSessionRepository)).build()
//
//      running(application) {
//        val request =
//          FakeRequest(POST, addASmallProducerRoute)
//                  .withFormUrlEncodedBody(
//                    ("producerName", "Super Cola Ltd"),
//                    ("referenceNumber", sdilNumber),
//                    ("lowBand", "12"),
//                    ("highBand", "12")
//                  )
//
//        val result = route(application, request).value
//
//        status(result) mustEqual BAD_REQUEST
//      }
//
//    }
  }
}
