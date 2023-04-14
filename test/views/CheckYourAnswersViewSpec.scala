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

import config.FrontendAppConfig
import models.{Amounts, ReturnPeriod}
import views.html.CheckYourAnswersView
import org.jsoup.nodes.Document
import play.api.i18n.Messages
import play.api.mvc.Request
import play.api.test.FakeRequest
import play.twirl.api.HtmlFormat
import utilitlies.CurrencyFormatter
import views.helpers.returnDetails.ReturnPeriodQuarter

class CheckYourAnswersViewSpec extends ViewSpecHelper {

  val checkYourAnswersView: CheckYourAnswersView =
    application.injector.instanceOf[CheckYourAnswersView]

  implicit val request: Request[_] = FakeRequest()
  implicit val config: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

//  val htmlWithSA: HtmlFormat.Appendable =
//    enrolledForPTPage(userId, true, routes.EnrolledForPTWithSAController.continue)(FakeRequest(), testMessages)
//  val htmlWithNoSA: HtmlFormat.Appendable =
//    enrolledForPTPage(userId, false, routes.EnrolledForPTController.continue)(FakeRequest(), testMessages)
//  val documentWithSA = doc(htmlWithSA)
//  val documentWithNoSA = doc(htmlWithNoSA)

  object Selectors {
    val heading = "govuk-heading-l"
    val subHeading = "govuk-heading-m"
    val caption = "govuk-caption-l"
    val bodyMargin5 = "govuk-body govuk-!-margin-bottom-5"
    val summaryListRow = "govuk-summary-list__row"
    val summaryListKey = "govuk-summary-list__key"
    val summaryListValue = "govuk-summary-list__value"
    val button = "govuk-button"
  }

  object SubHeadingIds {
    val cyaSubHeading = "cya-sub-header"
    val ownBrands = "ownBrandsPackagedAtYourOwnSite"
    val contractPackedAtYourOwnSite = "contractPackedAtYourOwnSite"
    val contractPackedForRegisteredSmallProducers = "contractPackedForRegisteredSmallProducers"
    val broughtIntoTheUK = "broughtIntoTheUK"
    val broughtIntoTheUKFromSmallProducers = "broughtIntoTheUKFromSmallProducers"
    val exported = "exported"
    val lostOrDestroyed = "lostOrDestroyed"
    val amountToPayTitle = "amount-to-pay-title"
    val registeredSites = "registered-sites"
    val sendYourReturn = "sendYourReturn"
  }

  val amounts = Amounts(1000, 100, 1100)

  "checkYourAnswersView" - {
    val html: HtmlFormat.Appendable =
      checkYourAnswersView(baseAlias, returnPeriod, UserAnswersTestData.emptyUserDetails, amounts, true)
    val document: Document = doc(html)

    "should have the expected title" in {
      document.title() mustEqual Messages("checkYourAnswers.title")
    }

    "should have the expected heading" in {
      document.getElementsByClass(Selectors.heading).text() mustEqual Messages("checkYourAnswers.title")
    }

    "should have the expected caption" - {
      List(0, 1, 2, 3).foreach(quater => {
        val returnPeriodWithQuater = ReturnPeriod(2022, quater)
        val html1: HtmlFormat.Appendable =
          checkYourAnswersView(baseAlias, returnPeriodWithQuater, UserAnswersTestData.emptyUserDetails, amounts, true)
        val document1: Document = doc(html1)

        s"when in return period is in quater $quater" in {
          val returnPeriodString = ReturnPeriodQuarter.formatted(returnPeriodWithQuater)
          document1.getElementsByClass(Selectors.caption).text() mustEqual s"$baseAlias - $returnPeriodString"
        }
      })
    }

    "should include the amount to pay sub header" - {
      "when the amount total is positive" in {
        val html1: HtmlFormat.Appendable =
          checkYourAnswersView(baseAlias, returnPeriod, UserAnswersTestData.emptyUserDetails, amounts, true)
        val document1: Document = doc(html1)
        val expectedResult = Messages("youNeedToPay", CurrencyFormatter.formatAmountOfMoneyWithPoundSign(amounts.total))

        val element = document1.getElementById("cya-sub-header")
        element.className() mustEqual Selectors.subHeading
        element.text() mustEqual expectedResult
      }

      "when the amount total is positive" in {
        val amountsWithNegativeTotal = amounts.copy(total = -1000)
        val html1: HtmlFormat.Appendable =
          checkYourAnswersView(baseAlias, returnPeriod, UserAnswersTestData.emptyUserDetails, amountsWithNegativeTotal, true)
        val document1: Document = doc(html1)
        val expectedResult = Messages("yourSoftDrinksLevyAccountsWillBeCredited", CurrencyFormatter.formatAmountOfMoneyWithPoundSign(amountsWithNegativeTotal.total * -1))

        val element = document1.getElementById("cya-sub-header")
        element.className() mustEqual Selectors.subHeading
        element.text() mustEqual expectedResult
      }
    }

    "should not contain the amount to pay sub header" - {
      "when the total amount is 0" in {
        val amountsWith0Total = amounts.copy(total = 0)
        val html1: HtmlFormat.Appendable =
          checkYourAnswersView(baseAlias, returnPeriod, UserAnswersTestData.emptyUserDetails, amountsWith0Total, true)
        val document1: Document = doc(html1)
        document1.getElementById("cya-sub-header") mustBe null
      }
    }


//    UserAnswersTestData.userAnswersModels.map { case (key, userAnswers) =>
//      s"when the $key" - {
//
//
//      }
//    }

    "should have the sendYourReturn sub heading" in {
      val element = document.getElementById("sendYourReturn")
      element.className() mustEqual Selectors.subHeading
      element.text() mustEqual Messages("sendYourReturn")
    }

    "should have the sendYourReturnConfirmation govuk body" in {
      val element = document.getElementById("sendYourReturnConfirmation")
      element.className() mustEqual Selectors.bodyMargin5
      element.text() mustEqual Messages("sendYourReturnConfirmation")
    }

  }

//  "EnrolledForPTPage" when {
//    "the user has SA" should {
//      "contain the correct title" in {
//        documentWithSA.title shouldBe EnrolledForPTPageMessages.title
//      }
//      "contain the correct first header" in {
//        documentWithSA
//          .getElementsByClass(Selectors.heading)
//          .text shouldBe EnrolledForPTPageMessages.heading
//      }
//
//      validateTimeoutDialog(documentWithSA)
//      validateTechnicalHelpLinkPresent(documentWithSA)
//      validateAccessibilityStatementLinkPresent(documentWithSA)
//
//      "contain the correct body" in {
//        documentWithSA
//          .getElementsByClass(Selectors.body)
//          .text shouldBe EnrolledForPTPageMessages.paragraphSA
//      }
//
//      "contain the correct second header" in {
//        documentWithSA
//          .getElementsByClass(Selectors.saHeading)
//          .text shouldBe EnrolledForPTPageMessages.heading2
//      }
//
//      "contain the correct button" in {
//        documentWithSA
//          .getElementsByClass(Selectors.button)
//          .text shouldBe EnrolledForPTPageMessages.button
//      }
//
//      "contains a form with the correct action" in {
//        documentWithSA
//          .select(Selectors.form)
//          .attr("action") shouldBe EnrolledForPTPageMessages.saAction
//      }
//    }
//
//    "the user has no SA" should {
//      "contain the correct title" in {
//        documentWithNoSA.title shouldBe EnrolledForPTPageMessages.title
//      }
//      "contain the correct first header" in {
//        documentWithNoSA
//          .getElementsByClass("govuk-heading-xl")
//          .text shouldBe EnrolledForPTPageMessages.heading
//      }
//
//      validateTimeoutDialog(documentWithNoSA)
//      validateTechnicalHelpLinkPresent(documentWithNoSA)
//
//      "contain the correct body" in {
//        documentWithNoSA
//          .getElementsByClass("govuk-body")
//          .text shouldBe EnrolledForPTPageMessages.paragraphNoSA
//      }
//      "contain the correct button" in {
//        documentWithNoSA
//          .getElementsByClass("govuk-button")
//          .text shouldBe EnrolledForPTPageMessages.button
//      }
//
//      "contains a form with the correct action" in {
//        documentWithSA
//          .select(Selectors.form)
//          .attr("action") shouldBe EnrolledForPTPageMessages.saAction
//      }
//    }
//  }
}
