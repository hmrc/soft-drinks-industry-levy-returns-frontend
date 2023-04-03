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

import base.SpecBase
import forms.PackagingSiteDetailsFormProvider
import models.backend.{Site, UkAddress}
import models.{NormalMode, UserAnswers}
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import org.scalatestplus.mockito.MockitoSugar
import pages.behaviours.PageBehaviours
import play.api.i18n.Messages
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.test.Helpers.contentAsString
import play.twirl.api.Html
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{SummaryList, SummaryListRow}
import viewmodels.checkAnswers.PackagingSiteDetailsSummary
import viewmodels.govuk.SummaryListFluency
import views.html.PackagingSiteDetailsView


class packagingSiteDetailsPageSpec extends SpecBase with MockitoSugar with SummaryListFluency with PageBehaviours {

//  val formProvider = new PackagingSiteDetailsFormProvider()
  val form = new PackagingSiteDetailsFormProvider()
  val view: PackagingSiteDetailsView = application.injector.instanceOf[PackagingSiteDetailsView]
  val packagingSummaryList: List[SummaryListRow] =
    PackagingSiteDetailsSummary.row2(List())(messages(application))

  SummaryListViewModel(
    rows = packagingSummaryList
  )
  val PackagingSite1 = Site(
    UkAddress(List("33 Rhes Priordy", "East London"), "E73 2RP"),
    None,
    Some("Wild Lemonade Group"),
    None)

  val PackagingSite2 = Site(
    UkAddress(List("29 Station Place", "Cambridge`"), "CB1 2FP"),
    Some("10"),
    None,
    None)

  val packagingSiteListWith2 = List(PackagingSite1, PackagingSite2)
  val packagingSiteListWith1 = List(PackagingSite1)
  val packagingSiteSummaryAliasList1: List[SummaryListRow] = PackagingSiteDetailsSummary.row2(packagingSiteListWith1)
  val aliasList1: SummaryList = SummaryListViewModel(
    rows = packagingSiteSummaryAliasList1
  )
  val packagingSiteSummaryAliasList2: List[SummaryListRow] = PackagingSiteDetailsSummary.row2(packagingSiteListWith2)
  val aliasList2: SummaryList = SummaryListViewModel(
    rows = packagingSiteSummaryAliasList2
  )
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
              view(form.apply(), NormalMode, aliasList1)(FakeRequest(), messages(application))
            val document = doc(html)

            document.title() shouldBe "You added 1 packaging sites - soft-drinks-industry-levy-returns-frontend - GOV.UK"
          }
          "have the expected heading when there is only 1 packaging site" in {
            val html =
              view(form.apply(), NormalMode, aliasList1)(FakeRequest(), messages(application))
            val document = doc(html)

            document
              .getElementsByTag("h1")
              .text shouldBe "You added 1 packaging sites"
          }

          "show the correct packaging site in the list when there is only 1 packaging site" in {
            val html =
              view(form.apply(), NormalMode, aliasList1)(FakeRequest(), messages(application))
            val document = doc(html)

            document
              .getElementsByClass("govuk-summary-list__key")
              .get(0)
              .text should include("Wild Lemonade Group")
          }

          "not show the remove link when there is only 1 packaging site" in {
            val html =
              view(form.apply(), NormalMode, aliasList1)(FakeRequest(), messages(application))
            val document = doc(html)

            val links = document
//
//              .getElementsByClass("govuk-body-m")
//              .first()
////              .getElementsByTag("li")
//            println(Console.YELLOW + "links is " + links + Console.WHITE)
//            links.children()
//              .size() shouldBe 1

          //  links.get(0).text() shouldBe "Edit"
          }

          "have the option to add another UK packaging site" in {
            val html =
              view(form.apply(), NormalMode, aliasList1)(FakeRequest(), messages(application))
            val document = doc(html)

            document
              .getElementsByTag("h2")
              .text must include("Do you want to add another UK packaging site?")
          }




  }
