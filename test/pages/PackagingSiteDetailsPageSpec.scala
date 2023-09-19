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

import base.ReturnsTestData._
import base.SpecBase
import forms.PackagingSiteDetailsFormProvider
import models.{CheckMode, NormalMode}
import models.backend.{Site, UkAddress}
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import org.scalatestplus.mockito.MockitoSugar
import pages.behaviours.PageBehaviours
import play.api.routing.Router.empty.routes
import play.api.test.FakeRequest
import play.test.Helpers.contentAsString
import play.twirl.api.Html
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.SummaryListFluency
import views.helpers.returnDetails.PackagingSiteDetailsSummary
import views.html.PackagingSiteDetailsView


class PackagingSiteDetailsPageSpec extends SpecBase with MockitoSugar with SummaryListFluency with PageBehaviours {

  val form = new PackagingSiteDetailsFormProvider()
  val view: PackagingSiteDetailsView = application.injector.instanceOf[PackagingSiteDetailsView]
  val packagingSummaryList: List[SummaryListRow] =
    PackagingSiteDetailsSummary.row2(Map.empty)(messages(application))

  SummaryListViewModel(
    rows = packagingSummaryList
  )

  val PackagingSite2 = Site(
    UkAddress(List("29 Station Place", "Cambridge"), "CB1 2FP"),
    Some("10"),
    None,
    None)

  val packagingSiteListWith2 = Map(("56454651", PackagingSite1), ("45541277", PackagingSite2))
  def doc(result: Html): Document = Jsoup.parse(contentAsString(result))

  object Selectors {
    val body = "govuk-body"
    val button = "govuk-button"
    val form = "form"
    val summaryListKey = "govuk-summary-list__key"
  }

  "packagingSiteDetailsPage" - {

      beRetrievable[Boolean](PackagingSiteDetailsPage)

      beSettable[Boolean](PackagingSiteDetailsPage)

      beRemovable[Boolean](PackagingSiteDetailsPage)
    }
    "have the expected title when there is only 1 packaging site" in {
      val html =
        view(form.apply(), NormalMode, packagingSiteListWith1)(FakeRequest(), messages(application))
      val document = doc(html)

      document.title() shouldBe "You added 1 packaging site - Soft Drinks Industry Levy - GOV.UK"
    }

    "have the expected heading when there is only 1 packaging site" in {
      val html =
        view(form.apply(), NormalMode, packagingSiteListWith1 )(FakeRequest(), messages(application))
      val document = doc(html)

      document
        .getElementsByTag("h1")
        .text shouldBe "You added 1 packaging site"
    }

    "show the correct packaging site in the list when there is only 1 packaging site" in {
      val html =
        view(form.apply(), NormalMode, packagingSiteListWith1 )(FakeRequest(), messages(application))
      val document = doc(html)

      document
        .getElementsByClass("govuk-summary-list__key")
        .text should include("Wild Lemonade Group")
    }

    "not show the remove link when there is only 1 packaging site" in {
      val html =
        view(form.apply(), NormalMode, packagingSiteListWith1)(FakeRequest(), messages(application))
      val document = doc(html)

      val summaryListContents = document.getElementsByClass("govuk-summary-list")
      summaryListContents.text shouldNot include("Remove")
    }

    "have the option to add another UK packaging site" in {
      val html =
        view(form.apply(), NormalMode, packagingSiteListWith1)(FakeRequest(), messages(application))
      val document = doc(html)

      document
        .getElementsByClass("govuk-fieldset__legend--m")
        .text must include("Do you want to add another UK packaging site?")
    }

    "have the expected title when there are 2 packaging sites" in {
      val html =
        view(form.apply(), NormalMode, packagingSiteListWith2)(FakeRequest(), messages(application))
      val document = doc(html)

      document.title() shouldBe "You added 2 packaging sites - Soft Drinks Industry Levy - GOV.UK"
    }

    "have the expected heading when there are 2 packaging sites" in {
      val html =
        view(form.apply(), NormalMode, packagingSiteListWith2)(FakeRequest(), messages(application))
      val document = doc(html)

      document
        .getElementsByTag("h1")
        .text shouldBe "You added 2 packaging sites"
    }

    "show the correct packaging site in the list when there are 2 packaging sites" in {
      val html =
        view(form.apply(), NormalMode, packagingSiteListWith2)(FakeRequest(), messages(application))
      val summaryListContents = doc(html)
        .getElementsByClass("govuk-summary-list__key")

      summaryListContents.size() shouldBe 2
      summaryListContents.first.text() should include ("Wild Lemonade Group")
      summaryListContents.last.text() should include ("29 Station Place")
    }

    "show the remove link when there are 2 packaging sites" in {
      val html =
        view(form.apply(), NormalMode, packagingSiteListWith2)(FakeRequest(), messages(application))

      val summaryActions = doc(html).getElementsByClass("govuk-summary-list__actions")
      summaryActions.size() shouldBe 2
      summaryActions.first.text() should include("Remove")
      summaryActions.last.text() should include("Remove")
    }

  "remove link should go to proper url" in {
    val html =
      view(form.apply(), NormalMode, packagingSiteListWith2)(FakeRequest(), messages(application))

    val removeLink = doc(html).getElementsByClass("govuk-summary-list__actions")
      .tagName("ul").tagName("li").last().getElementsByClass("govuk-link")
    removeLink.attr("href") shouldBe
      "/soft-drinks-industry-levy-returns-frontend/remove-packaging-site-details/45541277"
  }


  "should contain the correct url" - {
    "when in NormalMode" in {
      PackagingSiteDetailsPage.url(NormalMode) mustBe controllers.routes.PackagingSiteDetailsController.onPageLoad(NormalMode).url
    }

    "when in CheckMode" in {
      PackagingSiteDetailsPage.url(CheckMode) mustBe controllers.routes.PackagingSiteDetailsController.onPageLoad(CheckMode).url
    }
  }

}
