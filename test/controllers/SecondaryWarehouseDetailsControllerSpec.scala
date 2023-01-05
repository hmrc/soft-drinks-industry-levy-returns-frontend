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
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.SecondaryWarehouseDetailsPage
import play.api.inject.bind
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

  val formProvider = new SecondaryWarehouseDetailsFormProvider()
  val form = formProvider()

  lazy val secondaryWarehouseDetailsRoute = routes.SecondaryWarehouseDetailsController.onPageLoad(NormalMode).url

  "SecondaryWarehouseDetails Controller" - {

    val warhouseList = List(Warehouse("ABC Ltd", Address("33 Rhes Priordy", "East London","","","WR53 7CX")),
      Warehouse("Super Cola Ltd", Address("33 Rhes Priordy", "East London","","","SA13 7CE")))

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        implicit val request = FakeRequest(GET, secondaryWarehouseDetailsRoute)
        val result = route(application, request).value

        val view = application.injector.instanceOf[SecondaryWarehouseDetailsView]

        val list: List[Warehouse] =
        List(Warehouse("ABC Ltd", Address("33 Rhes Priordy", "East London","Line 3","Line 4","WR53 7CX")),
             Warehouse("Super Cola Ltd", Address("33 Rhes Priordy", "East London","Line 3","","SA13 7CE")))


        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode, list)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = UserAnswers(userAnswersId).set(SecondaryWarehouseDetailsPage, true).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, secondaryWarehouseDetailsRoute)

        val view = application.injector.instanceOf[SecondaryWarehouseDetailsView]

        val result = route(application, request).value

        val list: List[Warehouse] =
          List(Warehouse("ABC Ltd", Address("33 Rhes Priordy", "East London","Line 3","Line 4","WR53 7CX")),
               Warehouse("Super Cola Ltd", Address("33 Rhes Priordy", "East London","Line 3","","SA13 7CE")))



        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(true), NormalMode , list)(request, messages(application)).toString
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

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, secondaryWarehouseDetailsRoute)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[SecondaryWarehouseDetailsView]

        val result = route(application, request).value

        val list: List[Warehouse] =
          List(Warehouse("ABC Ltd", Address("33 Rhes Priordy", "East London","Line 3","Line 4","WR53 7CX")),
               Warehouse("Super Cola Ltd", Address("33 Rhes Priordy", "East London","Line 3","","SA13 7CE")))


        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode, list)(request, messages(application)).toString
      }
    }
  }
}
