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

package views

import org.jsoup.Jsoup
import play.api.test.FakeRequest
import views.helpers.ViewSpecHelper
import views.html.auth.SignedOutView

class SignedOutViewSpec extends ViewSpecHelper {

  val view = application.injector.instanceOf[SignedOutView]
  "view" - {
    "should be correct" in {
      val viewBody = Jsoup.parse(view()(FakeRequest(), implicitly).body)
      viewBody.title() mustBe "For your security, we signed you out - Soft Drinks Industry Levy - GOV.UK"
      viewBody.getElementsByTag("h1").first().text() mustBe "For your security, we signed you out"
      viewBody.getElementsByClass("govuk-body").first().text() mustBe "We did not save your answers."

    }
  }
}
