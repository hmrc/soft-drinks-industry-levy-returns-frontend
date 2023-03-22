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
import models.NormalMode
import org.jsoup.Jsoup
import play.api.i18n.Messages
import play.api.test.FakeRequest
import play.api.test.Helpers._

class ChangeRegistrationControllerSpec extends SpecBase {

  "ChangeRegistration Controller" - {

    "must return OK and the correct link for packer within the view for a GET" in {

      val application = applicationBuilder2(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, routes.ChangeRegistrationController.onPageLoad().url)

        val result = route(application, request).value
        val page = Jsoup.parse(contentAsString(result))
        status(result) mustEqual OK
        contentAsString(result) must include (routes.PackagedContractPackerController.onPageLoad(NormalMode).url)
        page.getElementsByTag("h1").text() mustEqual Messages("ChangeRegistration.title")

      }
    }

    "must return OK and the correct link for importer within the view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, routes.ChangeRegistrationController.onPageLoad().url)

        val result = route(application, request).value

        val page = Jsoup.parse(contentAsString(result))
        status(result) mustEqual OK
        contentAsString(result) must include(routes.BroughtIntoUKController.onPageLoad(NormalMode).url)
        page.getElementsByTag("h1").text() mustEqual Messages("ChangeRegistration.title")

      }
    }
  }
}
