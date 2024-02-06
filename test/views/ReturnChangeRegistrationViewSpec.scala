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

import controllers.routes
import models.NormalMode
import org.jsoup.Jsoup
import play.api.test.FakeRequest
import views.helpers.ViewSpecHelper
import views.html.ReturnChangeRegistrationView

class ReturnChangeRegistrationViewSpec extends ViewSpecHelper {

  val view: ReturnChangeRegistrationView = application.injector.instanceOf[ReturnChangeRegistrationView]

  "page" - {
    "should render correctly with Packaged Contract Packer link when coPacker is false but coPacker data was completed" in {
      val urlLink: String = routes.PackagedContractPackerController.onPageLoad(NormalMode).url
      val renderedView = Jsoup.parse(view(urlLink)(FakeRequest(), implicitly).body)
      renderedView.title() mustBe "You changed your soft drinks business activity - Soft Drinks Industry Levy - GOV.UK"
      renderedView.getElementsByTag("h1").text() mustBe "You changed your soft drinks business activity"
      renderedView.getElementsByTag("h1").attr("class") mustBe "govuk-heading-l"

      val bodyText = renderedView.getElementsByClass("govuk-body").eachText()
      bodyText.get(0) mustBe "In this return, you told us that you have packaged liable drinks in the UK."
      bodyText.get(1) mustBe "This is different to your registered business activity, so we will update your registration."
      bodyText.get(2) mustBe "If you made a mistake, you need to go back and change your answers."
      bodyText.get(3) mustBe "If you’re happy with the change, select Update Registration."
      renderedView.getElementsByTag("button").first().text() mustBe "Update registration"
      val linkInBodyText = renderedView.getElementById("main-content").getElementsByTag("a")
      linkInBodyText.attr("href") mustBe "/soft-drinks-industry-levy-returns-frontend/packaged-as-contract-packer"
    }

    "should render correctly with Brought into UK link when importer is false but import data was completed" in {
      val urlLink: String = routes.BroughtIntoUKController.onPageLoad(NormalMode).url
      val renderedView = Jsoup.parse(view(urlLink)(FakeRequest(), implicitly).body)
      renderedView.title() mustBe "You changed your soft drinks business activity - Soft Drinks Industry Levy - GOV.UK"
      renderedView.getElementsByTag("h1").text() mustBe "You changed your soft drinks business activity"
      renderedView.getElementsByTag("h1").attr("class") mustBe "govuk-heading-l"

      val bodyText = renderedView.getElementsByClass("govuk-body").eachText()
      bodyText.get(0) mustBe "In this return, you told us that you have packaged liable drinks in the UK."
      bodyText.get(1) mustBe "This is different to your registered business activity, so we will update your registration."
      bodyText.get(2) mustBe "If you made a mistake, you need to go back and change your answers."
      bodyText.get(3) mustBe "If you’re happy with the change, select Update Registration."
      renderedView.getElementsByTag("button").first().text() mustBe "Update registration"
      val linkInBodyText = renderedView.getElementById("main-content").getElementsByTag("a")
      linkInBodyText.attr("href") mustBe "/soft-drinks-industry-levy-returns-frontend/brought-into-uk"
    }
  }
}
