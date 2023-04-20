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
import forms.SecondaryWarehouseDetailsFormProvider
import models.{Address, NormalMode, UserAnswers, Warehouse}
import navigation.{FakeNavigator, Navigator}
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.SecondaryWarehouseDetailsPage
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{SummaryList, SummaryListRow}
import viewmodels.checkAnswers.SecondaryWarehouseDetailsSummary
import viewmodels.govuk.SummaryListFluency
import views.html.SecondaryWarehouseDetailsView

import scala.concurrent.Future

class SecondaryWarehouseDetailsControllerSpec extends SpecBase with MockitoSugar with  SummaryListFluency {

  def onwardRoute = Call("GET", "/foo")
  def doc(result: String): Document = Jsoup.parse(result)

  val formProvider = new SecondaryWarehouseDetailsFormProvider()
  val form = formProvider()

  lazy val secondaryWarehouseDetailsRoute = routes.SecondaryWarehouseDetailsController.onPageLoad(NormalMode).url

  val twoWarhouses:Map[String,Warehouse] = Map("1"-> Warehouse("ABC Ltd", Address("33 Rhes Priordy", "East London","Line 3","Line 4","WR53 7CX")),
    "2" -> Warehouse("Super Cola Ltd", Address("33 Rhes Priordy", "East London","Line 3","","SA13 7CE")))

  val userAnswerTwoWarhouses : UserAnswers = UserAnswers(sdilNumber,Json.obj(), List.empty,Map.empty,twoWarhouses)

  "SecondaryWarehouseDetails Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(userAnswerTwoWarhouses)).build()

      running(application) {
        implicit val request = FakeRequest(GET, secondaryWarehouseDetailsRoute)
        val result = route(application, request).value

        val view = application.injector.instanceOf[SecondaryWarehouseDetailsView]

        val WarhouseMap: Map[String,Warehouse] =
          Map("1"-> Warehouse("ABC Ltd", Address("33 Rhes Priordy", "East London","Line 3","Line 4","WR53 7CX")),
            "2" -> Warehouse("Super Cola Ltd", Address("33 Rhes Priordy", "East London","Line 3","","SA13 7CE")))

        val warehouseSummaryList: List[SummaryListRow] =
          SecondaryWarehouseDetailsSummary.row2(WarhouseMap)(messages(application))

        val summaryList: SummaryList = SummaryListViewModel(
          rows = warehouseSummaryList
        )

        status(result) mustEqual OK
        val summaryListContents = doc(contentAsString(result))
          .getElementsByClass("govuk-summary-list__key")

        summaryListContents.size() mustEqual  2
        summaryListContents.first.text() must include ("ABC Ltd")
        summaryListContents.last.text() must include ("Super Cola Ltd")

        val summaryActions = doc(contentAsString(result)).getElementsByClass("govuk-summary-list__actions-list")
        summaryActions.size() mustEqual 2
        summaryActions.first.text() must include("Remove")
        summaryActions.last.text() must include("Remove")

        val removeLink = doc(contentAsString(result)).getElementsByClass("govuk-summary-list__actions")
          .tagName("ul").tagName("li").last().getElementsByClass("govuk-link").last()
        removeLink.attr("href") mustEqual "/soft-drinks-industry-levy-returns-frontend/remove-warehouse-details/2"
        contentAsString(result) mustEqual view(form, NormalMode, summaryList)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {
      val userAnswers : UserAnswers = UserAnswers(sdilNumber,Json.obj(), List.empty,Map.empty,twoWarhouses)
        .set(SecondaryWarehouseDetailsPage, true).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, secondaryWarehouseDetailsRoute)

        val view = application.injector.instanceOf[SecondaryWarehouseDetailsView]

        val result = route(application, request).value

        val WarhouseMap: Map[String,Warehouse] =
          Map("1"-> Warehouse("ABC Ltd", Address("33 Rhes Priordy", "East London","Line 3","Line 4","WR53 7CX")),
            "2" -> Warehouse("Super Cola Ltd", Address("33 Rhes Priordy", "East London","Line 3","","SA13 7CE")))

        val warehouseSummaryList: List[SummaryListRow] =
          SecondaryWarehouseDetailsSummary.row2(WarhouseMap)(messages(application))

        val summaryList: SummaryList = SummaryListViewModel(
          rows = warehouseSummaryList
        )

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(true), NormalMode , summaryList)(request, messages(application)).toString
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
          FakeRequest(POST, secondaryWarehouseDetailsRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(userAnswerTwoWarhouses)).build()

      running(application) {
        val request =
          FakeRequest(POST, secondaryWarehouseDetailsRoute)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[SecondaryWarehouseDetailsView]

        val result = route(application, request).value

        val WarhouseMap: Map[String,Warehouse] =
          Map("1"-> Warehouse("ABC Ltd", Address("33 Rhes Priordy", "East London","Line 3","Line 4","WR53 7CX")),
               "2" -> Warehouse("Super Cola Ltd", Address("33 Rhes Priordy", "East London","Line 3","","SA13 7CE")))

        val warehouseSummaryList: List[SummaryListRow] =
         SecondaryWarehouseDetailsSummary.row2(WarhouseMap)(messages(application))

        val summaryList: SummaryList = SummaryListViewModel(
          rows = warehouseSummaryList
        )


        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode, summaryList)(request, messages(application)).toString
      }
    }
  }
}
