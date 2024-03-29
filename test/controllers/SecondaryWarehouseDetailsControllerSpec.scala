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
import errors.SessionDatabaseInsertError
import forms.SecondaryWarehouseDetailsFormProvider
import helpers.LoggerHelper
import models.backend.{ Site, UkAddress }
import models.{ NormalMode, UserAnswers }
import navigation.{ FakeNavigator, Navigator }
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.mockito.ArgumentMatchers.any
import org.mockito.{ ArgumentMatchers, MockitoSugar }
import pages.SecondaryWarehouseDetailsPage
import play.api.data.Form
import play.api.inject.bind
import play.api.mvc.{ AnyContentAsEmpty, Call }
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import services.{ AddressLookupService, WarehouseDetails }
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{ SummaryList, SummaryListRow }
import utilitlies.GenericLogger
import viewmodels.govuk.SummaryListFluency
import views.helpers.returnDetails.SecondaryWarehouseDetailsSummary
import views.html.SecondaryWarehouseDetailsView

import scala.concurrent.Future

class SecondaryWarehouseDetailsControllerSpec extends SpecBase with MockitoSugar with SummaryListFluency with LoggerHelper {

  def onwardRoute: Call = Call("GET", "/foo")
  def doc(result: String): Document = Jsoup.parse(result)

  val formProvider = new SecondaryWarehouseDetailsFormProvider()
  val form: Form[Boolean] = formProvider()

  lazy val secondaryWarehouseDetailsRoute: String = routes.SecondaryWarehouseDetailsController.onPageLoad(NormalMode).url

  val twoWarehouses: Map[String, Site] = Map(
    "1" -> Site(UkAddress(List("33 Rhes Priordy", "East London", "Line 3", "Line 4"), "WR53 7CX"), tradingName = Some("ABC Ltd")),
    "2" -> Site(UkAddress(List("33 Rhes Priordy", "East London", "Line 3", ""), "SA13 7CE"), tradingName = Some("Super Cola Ltd")))

  val userAnswerTwoWarehouses: UserAnswers = emptyUserAnswers.copy(warehouseList = twoWarehouses)

  "SecondaryWarehouseDetails Controller" - {

    "must redirect to returns sent page if return is already submitted" in {
      val application = applicationBuilder(userAnswers = Some(submittedAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, secondaryWarehouseDetailsRoute)
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.ReturnSentController.onPageLoad.url
      }
    }

    "must redirect to returns sent page if return is already submitted when hitting the submit" in {
      val application = applicationBuilder(userAnswers = Some(submittedAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, secondaryWarehouseDetailsRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.ReturnSentController.onPageLoad.url
      }
    }

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(userAnswerTwoWarehouses)).build()

      running(application) {
        implicit val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(GET, secondaryWarehouseDetailsRoute)
        val result = route(application, request).value

        val view = application.injector.instanceOf[SecondaryWarehouseDetailsView]

        val WarhouseMap: Map[String, Site] =
          Map(
            "1" -> Site(UkAddress(List("33 Rhes Priordy", "East London", "Line 3", "Line 4"), "WR53 7CX"), tradingName = Some("ABC Ltd")),
            "2" -> Site(UkAddress(List("33 Rhes Priordy", "East London", "Line 3", ""), "SA13 7CE"), tradingName = Some("Super Cola Ltd")))

        val warehouseSummaryList: List[SummaryListRow] =
          SecondaryWarehouseDetailsSummary.warehouseDetailRow(WarhouseMap)(messages(application))

        val summaryList: SummaryList = SummaryListViewModel(
          rows = warehouseSummaryList)

        status(result) mustEqual OK

        val summaryListContents = doc(contentAsString(result))
          .getElementsByClass("govuk-summary-list__key")

        summaryListContents.size() mustEqual 2
        summaryListContents.first.text() must include("ABC Ltd")
        summaryListContents.last.text() must include("Super Cola Ltd")

        val summaryActions = doc(contentAsString(result)).getElementsByClass("govuk-summary-list__actions")
        summaryActions.size() mustEqual 2
        summaryActions.first.text() must include("Remove")
        summaryActions.last.text() must include("Remove")

        val removeLink = doc(contentAsString(result)).getElementsByClass("govuk-summary-list__actions")
          .tagName("ul").tagName("li").last().getElementsByClass("govuk-link").last()
        removeLink.attr("href") mustEqual "/soft-drinks-industry-levy-returns-frontend/remove-warehouse-details/2"
        contentAsString(result) mustEqual view(form, NormalMode, summaryList)(request, messages(application)).toString
      }
    }

    "must return OK and contain correct header when 0 warehouses available" in {

      val userAnswers = emptyUserAnswers
      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        implicit val request = FakeRequest(GET, secondaryWarehouseDetailsRoute)
        val result = route(application, request).value

        status(result) mustEqual OK
        val page = Jsoup.parse(contentAsString(result))
        page.title() mustEqual "You added 0 UK warehouses - Soft Drinks Industry Levy - GOV.UK"
        page.getElementsByTag("h1").text must include("You added 0 UK warehouses")
      }
    }

    "must return OK and contain correct header when 1 warehouse available" in {

      val userAnswers = emptyUserAnswers.copy(warehouseList =
        Map("1" -> Site(UkAddress(List("33 Rhes Priordy", "East London", "Line 3", "Line 4"), "WR53 7CX"), tradingName = Some("ABC Ltd"))))

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        implicit val request = FakeRequest(GET, secondaryWarehouseDetailsRoute)
        val result = route(application, request).value

        status(result) mustEqual OK
        val page = Jsoup.parse(contentAsString(result))
        page.title() mustEqual "You added 1 UK warehouse - Soft Drinks Industry Levy - GOV.UK"
        page.getElementsByTag("h1").text must include("You added 1 UK warehouse")
      }
    }

    "must return OK and contain correct header when 2 warehouses available" in {

      val application = applicationBuilder(userAnswers = Some(userAnswerTwoWarehouses)).build()

      running(application) {
        implicit val request = FakeRequest(GET, secondaryWarehouseDetailsRoute)
        val result = route(application, request).value

        status(result) mustEqual OK
        val page = Jsoup.parse(contentAsString(result))
        page.title() mustEqual "You added 2 UK warehouses - Soft Drinks Industry Levy - GOV.UK"
        page.getElementsByTag("h1").text must include("You added 2 UK warehouses")
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {
      val userAnswers: UserAnswers = emptyUserAnswers.copy(warehouseList = twoWarehouses)
        .set(SecondaryWarehouseDetailsPage, true).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, secondaryWarehouseDetailsRoute)

        val view = application.injector.instanceOf[SecondaryWarehouseDetailsView]

        val result = route(application, request).value

        val warehouseMap: Map[String, Site] =
          Map(
            "1" -> Site(UkAddress(List("33 Rhes Priordy", "East London", "Line 3", "Line 4"), "WR53 7CX"), tradingName = Some("ABC Ltd")),
            "2" -> Site(UkAddress(List("33 Rhes Priordy", "East London", "Line 3", ""), "SA13 7CE"), tradingName = Some("Super Cola Ltd")))

        val warehouseSummaryList: List[SummaryListRow] =
          SecondaryWarehouseDetailsSummary.warehouseDetailRow(warehouseMap)(messages(application))

        val summaryList: SummaryList = SummaryListViewModel(
          rows = warehouseSummaryList)

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(true), NormalMode, summaryList)(request, messages(application)).toString
      }
    }

    "must redirect to the next page when valid data is submitted (true)" in {
      val mockSessionRepository = mock[SessionRepository]
      val mockAddressLookupService = mock[AddressLookupService]
      val onwardUrlForALF = "foobarwizz"

      when(mockSessionRepository.set(any())) thenReturn Future.successful(Right(true))

      when(mockAddressLookupService.initJourneyAndReturnOnRampUrl(
        ArgumentMatchers.eq(WarehouseDetails), ArgumentMatchers.any(), ArgumentMatchers.any())(
          ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
        .thenReturn(Future.successful(onwardUrlForALF))

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[AddressLookupService].toInstance(mockAddressLookupService))
          .build()

      running(application) {
        val request =
          FakeRequest(POST, secondaryWarehouseDetailsRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardUrlForALF

        verify(mockAddressLookupService, times(1)).initJourneyAndReturnOnRampUrl(
          ArgumentMatchers.eq(WarehouseDetails), ArgumentMatchers.any(), ArgumentMatchers.any())(
            ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any())
      }
    }
    "must redirect to the next page when valid data is submitted (false)" in {
      val mockSessionRepository = mock[SessionRepository]
      val mockAddressLookupService = mock[AddressLookupService]
      when(mockSessionRepository.set(any())) thenReturn Future.successful(Right(true))

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[AddressLookupService].toInstance(mockAddressLookupService))
          .build()

      running(application) {
        val request =
          FakeRequest(POST, secondaryWarehouseDetailsRoute)
            .withFormUrlEncodedBody(("value", "false"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.CheckYourAnswersController.onPageLoad.url

        verify(mockAddressLookupService, times(0)).initJourneyAndReturnOnRampUrl(
          ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any())(
            ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any())
      }
    }
    "must return error if ALF on ramp call returns error" in {
      val mockSessionRepository = mock[SessionRepository]
      val mockAddressLookupService = mock[AddressLookupService]
      when(mockSessionRepository.set(any())) thenReturn Future.successful(Right(true))

      when(mockAddressLookupService.initJourneyAndReturnOnRampUrl(
        ArgumentMatchers.eq(WarehouseDetails), ArgumentMatchers.any(), ArgumentMatchers.any())(
          ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
        .thenReturn(Future.failed(new Exception("uh oh spaghetio")))

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[AddressLookupService].toInstance(mockAddressLookupService))
          .build()

      running(application) {
        val request =
          FakeRequest(POST, secondaryWarehouseDetailsRoute)
            .withFormUrlEncodedBody(("value", "true"))

        intercept[Exception](await(route(application, request).value))

        verify(mockAddressLookupService, times(1)).initJourneyAndReturnOnRampUrl(
          ArgumentMatchers.eq(WarehouseDetails), ArgumentMatchers.any(), ArgumentMatchers.any())(
            ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any())
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(userAnswerTwoWarehouses)).build()

      running(application) {
        val request =
          FakeRequest(POST, secondaryWarehouseDetailsRoute)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[SecondaryWarehouseDetailsView]

        val result = route(application, request).value

        val WarehouseMap: Map[String, Site] =
          Map(
            "1" -> Site(UkAddress(List("33 Rhes Priordy", "East London", "Line 3", "Line 4"), "WR53 7CX"), tradingName = Some("ABC Ltd")),
            "2" -> Site(UkAddress(List("33 Rhes Priordy", "East London", "Line 3", ""), "SA13 7CE"), tradingName = Some("Super Cola Ltd")))

        val warehouseSummaryList: List[SummaryListRow] =
          SecondaryWarehouseDetailsSummary.warehouseDetailRow(WarehouseMap)(messages(application))

        val summaryList: SummaryList = SummaryListViewModel(
          rows = warehouseSummaryList)

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode, summaryList)(request, messages(application)).toString
      }
    }

    "should log an error message when internal server error is returned when user answers are not set in session repository" in {
      val mockSessionRepository = mock[SessionRepository]
      when(mockSessionRepository.set(any())) thenReturn Future.successful(Left(SessionDatabaseInsertError))

      val app =
        applicationBuilder(Some(completedUserAnswers))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .build()

      running(app) {
        withCaptureOfLoggingFrom(application.injector.instanceOf[GenericLogger].logger) { events =>
          val request = FakeRequest(POST, secondaryWarehouseDetailsRoute).withFormUrlEncodedBody(("value", "false"))
          await(route(app, request).value)
          events.collectFirst {
            case event =>
              event.getLevel.levelStr mustEqual "ERROR"
              event.getMessage mustEqual "Failed to set value in session repository while attempting set on secondaryWarehouseDetails"
          }.getOrElse(fail("No logging captured"))
        }
      }
    }

  }
}
