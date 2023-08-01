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

package pages


import forms.RemovePackagingDetailsConfirmationFormProvider
import models.NormalMode
import pages.behaviours.PageBehaviours
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.twirl.api.Html
import views.ViewSpecHelper
import views.html.RemovePackagingDetailsConfirmationView

class RemovePackagingDetailsConfirmationPageSpec extends ViewSpecHelper with PageBehaviours {

  lazy val view: RemovePackagingDetailsConfirmationView = application.injector.instanceOf[RemovePackagingDetailsConfirmationView]
  lazy val form: RemovePackagingDetailsConfirmationFormProvider = application.injector.instanceOf[RemovePackagingDetailsConfirmationFormProvider]
  implicit val fakeRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()

  "RemovePackagingDetailsConfirmationPage" - {

    beRetrievable[Boolean](RemovePackagingDetailsConfirmationPage)

    beSettable[Boolean](RemovePackagingDetailsConfirmationPage)

    beRemovable[Boolean](RemovePackagingDetailsConfirmationPage)

    "display correctly without form errors" in {
      val result = doc(view(form(),NormalMode, "foo", Html("foo2")))
      result.getElementById("packagingSiteDetails").text() mustBe "foo2"
      result.getElementsByTag("title").first().text() mustBe "Are you sure you want to remove this packaging site? - Soft Drinks Industry Levy - GOV.UK"
      result.getElementsByTag("h1").first().attr("class") mustBe "govuk-heading-l"
      result.getElementsByTag("h1").first().text() mustBe "Are you sure you want to remove this packaging site?"
      result.getElementsByClass("hmrc-sign-out-nav__link").first().attr("href") mustBe controllers.auth.routes.AuthController.signOut().url
      result.getElementsByClass("govuk-radios__item").first().text() mustBe "Yes"
      result.getElementsByClass("govuk-radios__item").last().text() mustBe "No"
      result.getElementsByTag("button").first().text() mustBe "Continue"
      result.getElementsByClass("govuk-back-link").text() mustBe "Back"
      result.getElementsByTag("form").first().attr("action") mustBe controllers.routes.RemovePackagingDetailsConfirmationController.onSubmit("foo").url
    }

    "display correctly with form errors" in {
      val result = doc(view(form().bind(Map("value" -> "")),NormalMode, "foo", Html("foo2")))
      result.getElementById("packagingSiteDetails").text() mustBe "foo2"
      result.getElementsByTag("title").first().text() mustBe "Error: Are you sure you want to remove this packaging site? - Soft Drinks Industry Levy - GOV.UK"
      result.getElementsByTag("h1").first().attr("class") mustBe "govuk-heading-l"
      result.getElementsByTag("h1").first().text() mustBe "Are you sure you want to remove this packaging site?"
      result.getElementsByClass("hmrc-sign-out-nav__link").first().attr("href") mustBe controllers.auth.routes.AuthController.signOut().url
      result.getElementsByClass("govuk-radios__item").first().text() mustBe "Yes"
      result.getElementsByClass("govuk-radios__item").last().text() mustBe "No"
      result.getElementsByTag("button").first().text() mustBe "Continue"
      result.getElementsByClass("govuk-back-link").text() mustBe "Back"
      result.getElementsByTag("form").first().attr("action") mustBe controllers.routes.RemovePackagingDetailsConfirmationController.onSubmit("foo").url
      result.getElementsByClass("govuk-error-summary__title").text() mustBe "There is a problem"
      result.getElementById("value-error").text() mustBe "Error: Select yes if you want to remove this packaging site"
    }
  }
}
