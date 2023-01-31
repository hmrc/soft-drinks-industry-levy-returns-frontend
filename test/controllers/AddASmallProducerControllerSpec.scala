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
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.AddASmallProducerPage
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import views.html.AddASmallProducerView

import scala.concurrent.Future

abstract class AddASmallProducerControllerSpec extends SpecBase with MockitoSugar {

//  val superCola = SmallProducer("Super Cola Ltd", "XCSDIL000000069", (1L, 1L))
//  val sparkyJuice = SmallProducer("Sparky Juice Co", "XCSDIL000000070", (100L, 100L))
  
  val formProvider = new AddASmallProducerFormProvider()
  val mockSessionRepository = mock[SessionRepository]
  val application = applicationBuilder(userAnswers = None).build()
  val sdilConnector = application.injector.instanceOf[SoftDrinksIndustryLevyConnector]
  implicit val request: DataRequest[_]
  val form = formProvider(mockSessionRepository, sdilConnector)

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

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, addASmallProducerRoute)

        val view = application.injector.instanceOf[AddASmallProducerView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, addASmallProducerRoute)

        val view = application.injector.instanceOf[AddASmallProducerView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(AddASmallProducer(Some(producerName), sdilReference,litres,litres)), NormalMode)(request, messages(application)).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, addASmallProducerRoute)
            .withFormUrlEncodedBody(("lowBand", producerName.toString), ("highBand", sdilReference.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, addASmallProducerRoute)
            .withFormUrlEncodedBody(("value", "invalid value"))

        val boundForm = form.bind(Map("value" -> "invalid value"))

        val view = application.injector.instanceOf[AddASmallProducerView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing user answers data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, addASmallProducerRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing user answers data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST,  addASmallProducerRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

  }
}
