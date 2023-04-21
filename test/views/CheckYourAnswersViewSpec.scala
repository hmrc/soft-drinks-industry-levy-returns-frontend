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

import base.UserAnswersTestData
import config.FrontendAppConfig
import controllers.routes
import models.{Amounts, ReturnPeriod}
import views.html.CheckYourAnswersView
import org.jsoup.nodes.Document
import play.api.i18n.Messages
import play.api.mvc.Request
import play.api.test.FakeRequest
import play.twirl.api.HtmlFormat
import utilitlies.CurrencyFormatter
import views.helpers.returnDetails.ReturnPeriodQuarter

class CheckYourAnswersViewSpec extends ReturnDetailsSummaryRowTestHelper {

  val checkYourAnswersView: CheckYourAnswersView =
    application.injector.instanceOf[CheckYourAnswersView]

  implicit val request: Request[_] = FakeRequest()
  implicit val config: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

  val amounts = Amounts(1000, 100, 1100)

  val call = routes.CheckYourAnswersController.onSubmit(false)

  "checkYourAnswersView" - {
    val html: HtmlFormat.Appendable =
      checkYourAnswersView(baseAlias, returnPeriod, UserAnswersTestData.emptyUserDetails, amounts, call)
    val document: Document = doc(html)

    "should have the expected title" in {
      document.title() must include(Messages("checkYourAnswers.title"))
    }

    "should have the expected heading" in {
      document.getElementsByClass(Selectors.heading).text() mustEqual Messages("checkYourAnswers.title")
    }

    "should have the expected caption" - {
      List(0, 1, 2, 3).foreach(quater => {
        val returnPeriodWithQuater = ReturnPeriod(2022, quater)
        val html1: HtmlFormat.Appendable =
          checkYourAnswersView(baseAlias, returnPeriodWithQuater, UserAnswersTestData.emptyUserDetails, amounts, call)
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
          checkYourAnswersView(baseAlias, returnPeriod, UserAnswersTestData.emptyUserDetails, amounts, call)
        val document1: Document = doc(html1)
        val expectedResult = Messages("youNeedToPay", CurrencyFormatter.formatAmountOfMoneyWithPoundSign(amounts.total))

        val element = document1.getElementById("cya-sub-header")
        element.className() mustEqual Selectors.subHeading
        element.text() mustEqual expectedResult
      }

      "when the amount total is negative" in {
        val amountsWithNegativeTotal = amounts.copy(total = -1000)
        val html1: HtmlFormat.Appendable =
          checkYourAnswersView(baseAlias, returnPeriod, UserAnswersTestData.emptyUserDetails, amountsWithNegativeTotal, call)
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
          checkYourAnswersView(baseAlias, returnPeriod, UserAnswersTestData.emptyUserDetails, amountsWith0Total, call)
        val document1: Document = doc(html1)
        document1.getElementById("cya-sub-header") mustBe null
      }
    }

    UserAnswersTestData.userAnswersModels.foreach { case (key, userAnswers) =>
      s"when the $key" - {
        val html1: HtmlFormat.Appendable =
          checkYourAnswersView(baseAlias, returnPeriod, userAnswers, amounts, call)
        val document1: Document = doc(html1)
        testSummaryLists(key, document1, userAnswers, isCheckAnswers = true)
      }
    }

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

    "should include a form" - {
      val form = document.getElementsByTag("form")
      "that has the method POST" in {
        form.attr("method") mustBe "POST"
      }
      "that has the correct action" in {
        form.attr("action") mustBe call.url
      }

      "that contains the correct button" in {
        val button = form.get(0).getElementsByClass(Selectors.button)
        button.text() mustEqual Messages("confirmDetailsAndSendReturn")
      }
    }

    "should include a link to print page" in {
      val printPageElements = document.getElementById("printPage")
      printPageElements.className() mustBe Selectors.bodyM
      val link = printPageElements.getElementsByClass(Selectors.link)
      link.text() mustEqual Messages("site.print")
      link.attr("href") mustEqual "javascript:window.print()"
    }
  }
}
