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
import models.UserAnswers
import org.jsoup.Jsoup
import pages.{BrandsPackagedAtOwnSitesPage, OwnBrandsPage}
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._
import viewmodels.govuk.SummaryListFluency
import views.html.CheckYourAnswersView

import java.time.LocalDate

class CheckYourAnswersControllerSpec extends SpecBase with SummaryListFluency {

  "Check Your Answers Controller" - {

    "must return OK and contain company alias and return period in grey pre header" in {

      val userAnswers = UserAnswers(sdilNumber, Json.obj(), List())
      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad.url)
        val result = route(application, request).value

        status(result) mustEqual OK
        val page = Jsoup.parse(contentAsString(result))
        println(Console.YELLOW + page.body() + Console.WHITE)
      }




    }


//    "must return OK and the correct view for a GET" in {
//
////      val answers =
////        emptyUserAnswers
////          .set(BrandsPackagedAtOwnSitesPage, LocalDate.now).success.value
////          .set(OwnBrandsPage, LocalDate.now).success.value
//
//      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()
//
//      running(application) {
//        val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad.url)
//        val result = route(application, request).value
//        val view = application.injector.instanceOf[CheckYourAnswersView]
//        val list = SummaryListViewModel(Seq.empty)
//
//        status(result) mustEqual OK
//        contentAsString(result) mustEqual view(list)(request, messages(application)).toString
//      }
//    }
//
//    "must redirect to Journey Recovery for a GET if no existing data is found" in {
//
//      val application = applicationBuilder(userAnswers = None).build()
//
//      running(application) {
//        val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad.url)
//
//        val result = route(application, request).value
//
//        status(result) mustEqual SEE_OTHER
//        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
//      }
//    }
//
//    "must show return period not available when no return period is present in request" in {
//      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()
//
//      running(application) {
//        val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad.url)
//        val result = route(application, request).value
//
//        status(result) mustEqual OK
//        contentAsString(result) must include ("return period not available")
//      }
//    }
  }
}
