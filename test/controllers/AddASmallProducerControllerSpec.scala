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
import models.{BlankMode, NormalMode, ReturnPeriod, SmallProducer, UserAnswers}
import org.jsoup.Jsoup
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.AddASmallProducerPage
import play.api.i18n.Messages
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository

import scala.concurrent.Future

class AddASmallProducerControllerSpec extends SpecBase with MockitoSugar {

  val formProvider = new AddASmallProducerFormProvider()
  val mockSessionRepository = mock[SessionRepository]

  val producerName = "Party Drinks Group"
  val sdilReference = "XPSDIL000000116"
  val notASmallProducerSDILReference = "XPSDIL000000478"
  val bandMax = 100000000000000L
  val litres = 20L
  val userAnswersWithTwoSmallProducers = UserAnswers(sdilReference, Json.obj(), List(superCola, sparkyJuice))

  lazy val addASmallProducerRoute = routes.AddASmallProducerController.onPageLoad(NormalMode).url
  lazy val addASmallProducerEditSubmitRoute = routes.AddASmallProducerController.onEditPageSubmit(superCola.sdilRef).url

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

    "must return OK when loaded in blank mode" in {
      lazy val addASmallProducerRoute = routes.AddASmallProducerController.onPageLoad(BlankMode).url

      val application = applicationBuilder(Some(userAnswersWithTwoSmallProducers)).build()

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

    "must redirect to Journey Recovery for a GET if no existing user answers data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, addASmallProducerRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
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
        page.getElementById("producerName").`val`() mustEqual (superCola.alias)
        page.getElementById("referenceNumber").`val`() mustEqual (superCola.sdilRef)
        page.getElementById("lowBand").`val`() mustEqual (superCola.litreage._1.toString)
        page.getElementById("highBand").`val`() mustEqual (superCola.litreage._2.toString)
      }
    }

    "must return 500 when small producer the user being edited does not exist on the list of small producers" in {

      lazy val addASmallProducerEditRoute = routes.AddASmallProducerController.onEditPageLoad(sdilReference = sparkyJuice.sdilRef).url

      val mockSessionRepository = mock[SessionRepository]
      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(Some(UserAnswers(sdilNumber, Json.obj(), List(superCola))))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository)).build()

      val res = running(application) {
        val request = FakeRequest(GET, addASmallProducerEditRoute)
        route(application, request).value
      }

      intercept[RuntimeException](
        res mustBe an[RuntimeException]
      )
    }
  }

  "AddASmallProducer Controller onEditSubmit" - {

    "must return Redirect(303) when SDIL reference has not been changed" in {

      val mockSessionRepository = mock[SessionRepository]
      val mockSdilConnector = mock[SoftDrinksIndustryLevyConnector]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)
      when(mockSdilConnector.checkSmallProducerStatus(any(), any())(any())) thenReturn Future.successful(Some(true))

      val sessionData =
        Json.obj(
          AddASmallProducerPage.toString -> Json.obj(
            "producerName" -> superCola.alias,
            "referenceNumber" -> superCola.sdilRef,
            "lowBand" -> superCola.litreage._1,
            "highBand" -> superCola.litreage._2
          )
        )

      val application =
        applicationBuilder(Some(UserAnswers(sdilNumber, sessionData, List(superCola, sparkyJuice))), Some(ReturnPeriod(year = 2022, quarter = 3)))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[SoftDrinksIndustryLevyConnector].toInstance(mockSdilConnector)
          ).build()

      running(application) {
        val request = FakeRequest(POST, addASmallProducerEditSubmitRoute)
          .withFormUrlEncodedBody(
            ("producerName", superCola.alias),
            ("referenceNumber", superCola.sdilRef),
            ("lowBand", superCola.litreage._1.toString),
            ("highBand", superCola.litreage._2.toString)
          )

        val result = route(application, request).value

        contentAsString(result) mustNot include(Messages("addASmallProducer.error.referenceNumber.exists"))
        status(result) mustEqual SEE_OTHER
      }
    }

    "must return Redirect(303) when SDIL reference has been changed and update the existing producer details" in {

      val mockSessionRepository = mock[SessionRepository]
      val mockSdilConnector = mock[SoftDrinksIndustryLevyConnector]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)
      when(mockSdilConnector.checkSmallProducerStatus(any(), any())(any())) thenReturn Future.successful(Some(true))

      val sessionData =
        Json.obj(
          AddASmallProducerPage.toString -> Json.obj(
            "producerName" -> superCola.alias,
            "referenceNumber" -> superCola.sdilRef,
            "lowBand" -> superCola.litreage._1,
            "highBand" -> superCola.litreage._2
          )
        )

      val application =
        applicationBuilder(Some(UserAnswers(sdilNumber, sessionData, List(superCola, sparkyJuice))), Some(ReturnPeriod(year = 2022, quarter = 3)))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[SoftDrinksIndustryLevyConnector].toInstance(mockSdilConnector)
          ).build()

      running(application) {
        val request = FakeRequest(POST, addASmallProducerEditSubmitRoute)
          .withFormUrlEncodedBody(
            ("producerName", superCola.alias),
            ("referenceNumber", "XCSDIL000000069"),
            ("lowBand", superCola.litreage._1.toString),
            ("highBand", superCola.litreage._2.toString)
          )

        val result = route(application, request).value

        contentAsString(result) mustNot include(Messages("addASmallProducer.error.referenceNumber.exists"))
        status(result) mustEqual SEE_OTHER
      }
    }

    "must return bad request(400) when SDIL reference has been changed but " +
      "it's already been added as another small producer" in {

      val mockSessionRepository = mock[SessionRepository]
      val mockSdilConnector = mock[SoftDrinksIndustryLevyConnector]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)
      when(mockSdilConnector.checkSmallProducerStatus(any(), any())(any())) thenReturn Future.successful(Some(true))

      val application =
        applicationBuilder(Some(UserAnswers(sdilNumber, Json.obj(), List(superCola, sparkyJuice))), Some(ReturnPeriod(year = 2022, quarter = 3)))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[SoftDrinksIndustryLevyConnector].toInstance(mockSdilConnector)
          ).build()

      running(application) {
        val request = FakeRequest(POST, addASmallProducerEditSubmitRoute)
          .withFormUrlEncodedBody(
            ("producerName", superCola.alias),
            ("referenceNumber", sparkyJuice.sdilRef),
            ("lowBand", superCola.litreage._1.toString),
            ("highBand", superCola.litreage._2.toString)
          )

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) must include(Messages("addASmallProducer.error.referenceNumber.exists"))

      }
    }

    "must return bad request(400) when either producer name, low band or high band values are changed to an invalid value" in {

      val mockSessionRepository = mock[SessionRepository]
      val mockSdilConnector = mock[SoftDrinksIndustryLevyConnector]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)
      when(mockSdilConnector.checkSmallProducerStatus(any(), any())(any())) thenReturn Future.successful(Some(true))

      val application =
        applicationBuilder(Some(UserAnswers(sdilNumber, Json.obj(), List(superCola, sparkyJuice))),
          Some(ReturnPeriod(year = 2022, quarter = 3)))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[SoftDrinksIndustryLevyConnector].toInstance(mockSdilConnector)
          ).build()

      running(application) {
        val request = FakeRequest(POST, addASmallProducerEditSubmitRoute)
          .withFormUrlEncodedBody(
            ("producerName", superCola.alias),
            ("referenceNumber", superCola.sdilRef),
            ("lowBand", "invalid"),
            ("highBand", superCola.litreage._2.toString)
          )

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) must include(Messages("addASmallProducer.error.lowBand.nonNumeric"))

      }
    }

    "must return 400 (bad request) and not a small producer SDIL reference error " +
      "when a SDIL reference number belongs to a producer that is not registered as a small producer" in {

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
          FakeRequest(POST, addASmallProducerEditSubmitRoute)
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

    "must return 303 when producer is actually a small producer (based on the response from the connector)" in {

      val mockSessionRepository = mock[SessionRepository]
      val mockSdilConnector = mock[SoftDrinksIndustryLevyConnector]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)
      when(mockSdilConnector.checkSmallProducerStatus(any(), any())(any())) thenReturn Future.successful(Some(true))

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
          FakeRequest(POST, addASmallProducerEditSubmitRoute)
            .withFormUrlEncodedBody(
              ("producerName", superCola.alias),
              ("referenceNumber", notASmallProducerSDILReference),
              ("lowBand", superCola.litreage._1.toString),
              ("highBand", superCola.litreage._2.toString)
            )

        val result = route(application, request).value

        status(result) mustEqual 303
        val page = Jsoup.parse(contentAsString(result))
        page.body().text() must not include(Messages("addASmallProducer.error.referenceNumber.notASmallProducer"))
      }
    }

    "must return 303 when response from the connector is None (when checking for small producer status)" in {



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
          FakeRequest(POST, addASmallProducerEditSubmitRoute)
            .withFormUrlEncodedBody(
              ("producerName", superCola.alias),
              ("referenceNumber", notASmallProducerSDILReference),
              ("lowBand", superCola.litreage._1.toString),
              ("highBand", superCola.litreage._2.toString)
            )

        val result = route(application, request).value

        status(result) mustEqual 303
        val page = Jsoup.parse(contentAsString(result))
        page.body().text() must not include (Messages("addASmallProducer.error.referenceNumber.notASmallProducer"))
      }
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
        val res = result.value.map( res => res.get)
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

    "must return 400 (bad request) and keep user entered data in the form " +
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
        page.getElementById("producerName").`val`() mustEqual (producerName)
        page.getElementById("referenceNumber").`val`() mustEqual (superCola.sdilRef)
        page.getElementById("lowBand").`val`() mustEqual (litres.toString)
        page.getElementById("highBand").`val`() mustEqual (litres.toString)


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

    "must redirect to Journey Recovery for a POST if no existing user answers data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, addASmallProducerRoute)
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
