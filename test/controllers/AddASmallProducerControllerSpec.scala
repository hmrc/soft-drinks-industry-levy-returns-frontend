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
import models.{EditMode, NormalMode, ReturnPeriod, SmallProducer, UserAnswers}
import org.jsoup.Jsoup
import models.{NormalMode, ReturnPeriod, SmallProducer, UserAnswers}
import org.jsoup.Jsoup
import org.scalatestplus.mockito.MockitoSugar
import pages.AddASmallProducerPage
import play.api.i18n.Messages
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import play.api.inject.bind

import scala.concurrent.Future

class AddASmallProducerControllerSpec extends SpecBase with MockitoSugar {

  val formProvider = new AddASmallProducerFormProvider()
  val mockSessionRepository = mock[SessionRepository]
//  lazy val form = formProvider(emptyUserAnswers, false)
//
  val producerName = "Party Drinks Group"
  val sdilReference = "XPSDIL000000116"
  val bandMax = 100000000000000L
  val litres = 20L

  val superCola = SmallProducer("Super Cola Ltd", "XCSDIL000000069", (1L, 1L))
  val sparkyJuice = SmallProducer("Sparky Juice Co", "XCSDIL000000070", (100L, 100L))

  lazy val addASmallProducerRoute = routes.AddASmallProducerController.onPageLoad(NormalMode).url

//  val mockUserAnswers = UserAnswers(
//    sdilNumber,
//    Json.obj(
//      AddASmallProducerPage.toString -> Json.obj(
//        "producerName" -> producerName,
//        "referenceNumber" -> sdilReference,
//        "lowBand" -> litres,
//        "highBand" -> litres
//      )
//    )
//  )

  "AddASmallProducer Controller onPageLoad" - {

    "must return OK with correct page title and header" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, addASmallProducerRoute)
        val result = route(application, request).value

        status(result) mustEqual OK
        val page = Jsoup.parse(contentAsString(result))

        page.title() must include(Messages("addASmallProducer.title"))
        page.getElementsByTag("h1").text() mustEqual Messages("addASmallProducer.heading")
      }
    }

    "must return OK and contain correct form fields" in {

      val application = applicationBuilder(Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, addASmallProducerRoute)
        val result = route(application, request).value

        status(result) mustEqual OK
        val page = Jsoup.parse(contentAsString(result))

        val labels = page.getElementsByTag("label").text()
        labels must include(Messages("addASmallProducer.hint1"))
        labels must include(Messages("addASmallProducer.referenceNumber"))
        labels must include(Messages("addASmallProducer.lowBand"))
        labels must include(Messages("addASmallProducer.highBand"))
      }
    }
  }

  "AddASmallProducer Controller onEditPageLoad" - {

    "must return OK(200) when page is loaded in edit mode for a single small producer" in {

      lazy val addASmallProducerEditRoute = routes.AddASmallProducerController.onEditPageLoad(sdilReference = superCola.sdilRef).url

      val mockSessionRepository = mock[SessionRepository]
      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(Some(UserAnswers(sdilNumber, Json.obj(), List(superCola, sparkyJuice))))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository)).build()

      running(application) {
        val request = FakeRequest(GET, addASmallProducerEditRoute)
        val result = route(application, request).value

        status(result) mustEqual OK
        val page = Jsoup.parse(contentAsString(result))

        println(page.getElementById("producerName").`val`())
        println(page.getElementById("referenceNumber").`val`())
        println(page.getElementById("lowBand").`val`())
        println(page.getElementById("highBand").`val`())
        
        page.getElementById("producerName").`val`() mustEqual(superCola.alias)
        page.getElementById("referenceNumber").`val`() mustEqual(superCola.sdilRef)
        page.getElementById("lowBand").`val`() mustEqual(superCola.litreage._1)
        page.getElementById("highBand").`val`() mustEqual(superCola.litreage._2)


      }
    }

  "AddASmallProducer Controller onSubmit" - {

    "must redirect to the next page when valid data is submitted" in {

      val mockSessionRepository = mock[SessionRepository]
      val mockSdilConnector = mock[SoftDrinksIndustryLevyConnector]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)
      when(mockSdilConnector.checkSmallProducerStatus(any(), any())(any())) thenReturn Future.successful(Some(true))

      val application =
        applicationBuilder(Some(emptyUserAnswers), Some(ReturnPeriod(2022, 3)))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[SoftDrinksIndustryLevyConnector].toInstance(mockSdilConnector)
          ).build()

      running(application) {
        val request = FakeRequest(POST, addASmallProducerRoute)
          .withFormUrlEncodedBody(
            ("producerName", "Super Cola Ltd"),
            ("referenceNumber", "XZSDIL000000234"),
            ("lowBand", "12"),
            ("highBand", "12")
          )

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
      }
    }

    "must return 400 (bad request) and invalid SDIL reference error when invalid SDIL ref is entered" in {

      val mockSessionRepository = mock[SessionRepository]
      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(Some(emptyUserAnswers))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository)).build()

      running(application) {
        val request =
          FakeRequest(POST, addASmallProducerRoute)
            .withFormUrlEncodedBody(
              ("producerName", "Super Cola Ltd"),
              ("referenceNumber", "INVALID ref"),
              ("lowBand", "12"),
              ("highBand", "12")
            )

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        val page = Jsoup.parse(contentAsString(result))
        page.body().text() must include(Messages("addASmallProducer.error.referenceNumber.invalid"))
      }
    }

    "must return 400 (bad request) and same SDIL reference error " +
      "when same SDIL ref as the logged in user is entered" in {

      val mockSessionRepository = mock[SessionRepository]
      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(Some(emptyUserAnswers))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository)).build()

      running(application) {
        val request =
          FakeRequest(POST, addASmallProducerRoute)
            .withFormUrlEncodedBody(
              ("producerName", "Super Cola Ltd"),
              ("referenceNumber", sdilNumber),
              ("lowBand", "12"),
              ("highBand", "12")
            )

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        val page = Jsoup.parse(contentAsString(result))
        page.body().text() must include(Messages("addASmallProducer.error.referenceNumber.same"))
      }
    }

    "must return 400 (bad request) and existing SDIL reference error " +
      "when a SDIL reference number matches one of the already entered small producer SDIL references" in {

      val mockSessionRepository = mock[SessionRepository]
      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(Some(UserAnswers(sdilNumber, Json.obj(), List(superCola, sparkyJuice))))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository)).build()

      running(application) {
        val request =
          FakeRequest(POST, addASmallProducerRoute)
            .withFormUrlEncodedBody(
              ("producerName", producerName),
              ("referenceNumber", superCola.sdilRef),
              ("lowBand", litres.toString),
              ("highBand", litres.toString)
            )

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        val page = Jsoup.parse(contentAsString(result))
        page.body().text() must include(Messages("addASmallProducer.error.referenceNumber.exists"))
      }
    }

    "must return 400 (bad request) and not a small producer SDIL reference error " +
      "when a SDIL reference number belongs to a producer that is not registered as a small producer" in {

      val notASmallProducerSDILReference = "XPSDIL000000478"
      val mockSessionRepository = mock[SessionRepository]
      val mockSdilConnector = mock[SoftDrinksIndustryLevyConnector]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)
      when(mockSdilConnector.checkSmallProducerStatus(any(), any())(any())) thenReturn Future.successful(Some(false))

      val application =
        applicationBuilder(
          Some(UserAnswers(sdilNumber, Json.obj(), List(superCola, sparkyJuice))),
          Some(ReturnPeriod(2022, 3)))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[SoftDrinksIndustryLevyConnector].toInstance(mockSdilConnector)
          ).build()

      running(application) {
        val request =
          FakeRequest(POST, addASmallProducerRoute)
            .withFormUrlEncodedBody(
              ("producerName", producerName),
              ("referenceNumber", notASmallProducerSDILReference),
              ("lowBand", litres.toString),
              ("highBand", litres.toString)
            )

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        val page = Jsoup.parse(contentAsString(result))
        page.body().text() must include(Messages("addASmallProducer.error.referenceNumber.notASmallProducer"))
      }
    }

    "must return 400 (bad request) when a connector returns none for check small producer status" in {

      val notASmallProducerSDILReference = "XPSDIL000000478"
      val mockSessionRepository = mock[SessionRepository]
      val mockSdilConnector = mock[SoftDrinksIndustryLevyConnector]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)
      when(mockSdilConnector.checkSmallProducerStatus(any(), any())(any())) thenReturn Future.successful(None)

      val application =
        applicationBuilder(
          Some(UserAnswers(sdilNumber, Json.obj(), List(superCola, sparkyJuice))),
          Some(ReturnPeriod(2022, 3)))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[SoftDrinksIndustryLevyConnector].toInstance(mockSdilConnector)
          ).build()

      running(application) {
        val request =
          FakeRequest(POST, addASmallProducerRoute)
            .withFormUrlEncodedBody(
              ("producerName", producerName),
              ("referenceNumber", notASmallProducerSDILReference),
              ("lowBand", litres.toString),
              ("highBand", litres.toString)
            )

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        val page = Jsoup.parse(contentAsString(result))
        page.body().text() must include(Messages("addASmallProducer.error.referenceNumber.notASmallProducer"))
      }
    }
  }






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

    "Small producer reference number must be different to reference currently submitting the returns" in {
      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository)).build()

      running(application) {
        val request =
          FakeRequest(POST, addASmallProducerRoute)
                  .withFormUrlEncodedBody(
                    ("producerName", "Super Cola Ltd"),
                    ("referenceNumber", sdilNumber),
                    ("lowBand", "12"),
                    ("highBand", "12")
                  )

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
      }

    }
  }
}
