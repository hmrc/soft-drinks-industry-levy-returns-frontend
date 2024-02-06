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

import base.ReturnsTestData._
import base.UserAnswersTestData
import base.UserAnswersTestData.userIsSmallProducer
import config.FrontendAppConfig
import controllers.routes
import models.{ Amounts, ReturnPeriod }
import org.jsoup.nodes.Document
import play.api.i18n.Messages
import play.api.mvc.{ Call, Request }
import play.api.test.FakeRequest
import play.twirl.api.HtmlFormat
import utilitlies.CurrencyFormatter
import views.helpers.ReturnDetailsSummaryRowTestHelper
import views.helpers.returnDetails.ReturnPeriodQuarter
import views.html.CheckYourAnswersView

class CheckYourAnswersViewSpec extends ReturnDetailsSummaryRowTestHelper {

  val checkYourAnswersView: CheckYourAnswersView =
    application.injector.instanceOf[CheckYourAnswersView]

  implicit val request: Request[_] = FakeRequest()
  implicit val config: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

  val amounts: Amounts = Amounts(1000, 100, 1100)
  val isSmallProducer: Boolean = false

  val call: Call = routes.CheckYourAnswersController.onSubmit

  "checkYourAnswersView" - {
    val html: HtmlFormat.Appendable =
      checkYourAnswersView(baseAlias, returnPeriod, UserAnswersTestData.emptyUserDetails, amounts, call, isSmallProducer)
    val document: Document = doc(html)

    "should have the expected title" in {
      document.title() must include(Messages("checkYourAnswers.title"))
    }

    "should have the expected heading" in {
      document.getElementsByClass(Selectors.heading).text() mustEqual Messages("checkYourAnswers.title")
    }

    "should have the expected caption" - {
      List(0, 1, 2, 3).foreach(quarter => {
        val returnPeriodWithQuarter = ReturnPeriod(2022, quarter)
        val html1: HtmlFormat.Appendable =
          checkYourAnswersView(baseAlias, returnPeriodWithQuarter, UserAnswersTestData.emptyUserDetails, amounts, call, isSmallProducer)
        val document1: Document = doc(html1)

        s"when in return period is in quarter $quarter" in {
          val returnPeriodString = ReturnPeriodQuarter.formatted(returnPeriodWithQuarter)
          document1.getElementById("cya-returnPeriod").text() mustEqual s"This return is for $baseAlias for $returnPeriodString"
        }
      })
    }

    "should include the amount to pay sub header" - {
      "when the amount total is positive" in {
        val html1: HtmlFormat.Appendable =
          checkYourAnswersView(baseAlias, returnPeriod, UserAnswersTestData.emptyUserDetails, amounts, call, isSmallProducer)
        val document1: Document = doc(html1)
        val expectedResult = Messages("youNeedToPay", CurrencyFormatter.formatAmountOfMoneyWithPoundSign(amounts.total))

        val element = document1.getElementById("cya-inset-sub-header")
        element.className() mustEqual Selectors.insetSubHeading
        element.text() mustEqual expectedResult
      }

      "when the amount total is negative" in {
        val amountsWithNegativeTotal = amounts.copy(total = -1000)
        val html1: HtmlFormat.Appendable =
          checkYourAnswersView(baseAlias, returnPeriod, UserAnswersTestData.emptyUserDetails, amountsWithNegativeTotal, call, isSmallProducer)
        val document1: Document = doc(html1)
        val expectedResult =
          Messages("yourSoftDrinksLevyAccountsWillBeCredited", CurrencyFormatter.formatAmountOfMoneyWithPoundSign(amountsWithNegativeTotal.total * -1))

        val element = document1.getElementById("cya-inset-sub-header")
        element.className() mustEqual Selectors.insetSubHeading
        element.text() mustEqual expectedResult
      }
    }

    "should not contain the amount to pay sub header" - {
      "when the total amount is 0" in {
        val amountsWith0Total = amounts.copy(total = 0)
        val html1: HtmlFormat.Appendable =
          checkYourAnswersView(baseAlias, returnPeriod, UserAnswersTestData.emptyUserDetails, amountsWith0Total, call, isSmallProducer)
        val document1: Document = doc(html1)
        document1.getElementById("cya-sub-header") mustBe null
      }
    }

    "should not contain the own brands sub header" - {
      "when the user is a small producer" in {
        val amountsWith0Total = amounts.copy(total = 0)
        val html1: HtmlFormat.Appendable =
          checkYourAnswersView(baseAlias, returnPeriod, userIsSmallProducer, amountsWith0Total, call, isSmallProducer = true)
        val document1: Document = doc(html1)
        document1.getElementById("ownBrandsPackagedAtYourOwnSite") mustBe null
      }
    }

    UserAnswersTestData.userAnswersModels.foreach {
      case (key, userAnswers) =>
        s"when the $key" - {
          val html1: HtmlFormat.Appendable =
            checkYourAnswersView(baseAlias, returnPeriod, userAnswers, amounts, call, isSmallProducer)
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
