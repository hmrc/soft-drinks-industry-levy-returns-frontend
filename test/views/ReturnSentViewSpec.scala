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
import config.FrontendAppConfig
import models.Amounts
import org.jsoup.nodes.Document
import play.api.i18n.Messages
import play.api.mvc.Request
import play.api.test.FakeRequest
import play.twirl.api.HtmlFormat
import views.helpers.ReturnDetailsSummaryRowTestHelper
import views.html.ReturnSentView

class ReturnSentViewSpec extends ReturnDetailsSummaryRowTestHelper {

  val returnSentView: ReturnSentView =
    application.injector.instanceOf[ReturnSentView]

  implicit val request: Request[_] = FakeRequest()
  implicit val config: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

  val amounts: Amounts = Amounts(1000, 100, 1100)

  val amountsLists: Map[String, Amounts] = Map(
    "total owed for this quarter is positive" -> amounts,
    "total owed for this quarter is 0" -> amounts.copy(total = 0),
    "total owed for this quarter is negative" -> amounts.copy(total = -1000))

  val amountOwed = "£100"

  "returnSentView" - {
    val html: HtmlFormat.Appendable =
      returnSentView(returnPeriod, UserAnswersTestData.emptyUserDetails, amounts, aSubscription, amountOwed)
    val document: Document = doc(html)

    "should have the expected title" in {
      document.title() must include(Messages("returnSent.title"))
    }

    "should include the expected panel" in {
      val panel = document.getElementsByClass(Selectors.panel).get(0)
      panel.getElementsByClass(Selectors.panel_title).text() mustEqual Messages("returnSent.title")
      panel.getElementsByClass(Selectors.panel_body).text() mustEqual "Soft Drinks Industry Levy return April to June 2022 for the Super Lemonade Plc"
    }

    "should include a link to print page" in {
      val printPageElements = document.getElementById("printPage")
      printPageElements.className() mustBe Selectors.bodyM
      val link = printPageElements.getElementsByClass(Selectors.link)
      link.text() mustEqual Messages("site.print")
      link.attr("href") mustEqual "javascript:window.print()"
    }

    amountsLists.foreach {
      case (key, amount) =>
        val html1: HtmlFormat.Appendable =
          returnSentView(returnPeriod, emptyUserAnswers, amount, aSubscription, amountOwed)
        val document1: Document = doc(html1)
        s"when the $key" - {
          "should include a what to do next section" - {
            "that has the expected heading" in {
              val subHeading = document1.getElementById("whatNextHeader")
              subHeading.text() mustEqual Messages("returnSent.headerOne")
            }
            "that has the expected body" - {
              val body = document1.getElementById("whatNextText")
              "which has the expectedText" in {
                val expectedText = if (amount.total > 0) {
                  "You need to pay £100 by 30 July 2022." +
                    " Make sure you include your Soft Drinks Industry Levy reference." +
                    " XKSDIL000000022 when making a payment." +
                    " See how to pay the levy (opens in a new tab)" +
                    " Your next return will be for July to September 2022." +
                    " You must send this return and make any payments by 30 October 2022."
                } else if (amount.total < 0) {
                  "You do not need to do anything else." +
                    " We will take the payment from your Soft Drinks Industry Levy account shortly." +
                    " Your next return will be for July to September 2022." +
                    " You must send this return and make any payments by 30 October 2022."
                } else {
                  "You do not need to do anything else." +
                    " Your next return will be for July to September 2022." +
                    " You must send this return and make any payments by 30 October 2022."
                }
                body.text() mustEqual expectedText
              }
              if (amount.total > 0) {
                "which has the sdil number in bold" in {
                  body.getElementsByTag("strong").text() mustBe aSubscription.sdilRef
                }

                "which has the expected link" in {
                  val link = body.getElementsByClass(Selectors.link)
                  link.text() mustEqual "See how to pay the levy (opens in a new tab)"
                  link.attr("href") mustEqual "https://www.gov.uk/guidance/pay-the-soft-drinks-industry-levy-notice-5"
                }
              }
            }
          }
        }
    }

    "should include a help with this service section" - {
      "that has the expected subheading" in {
        val subHeading = document.getElementById("helpWithThisServiceHeading")
        subHeading.text() mustEqual Messages("returnSent.headerTwo")
      }

      "that has the expected body" in {
        val body = document.getElementById("helpWithThisServiceText")
        body.text() mustEqual Messages("returnSent.points")
      }

      "that includes the expected bullet list section" in {
        val bulletList = document
          .getElementsByClass(Selectors.bulletList)
          .get(0)
          .getElementsByTag("li")
          .eachText()
        bulletList.size() mustBe 4
        val expectedList = List(
          Messages("returnSent.list1"),
          Messages("returnSent.list2"),
          Messages("returnSent.list3"),
          Messages("returnSent.list4"))
        expectedList.foreach(listItem =>
          bulletList must contain(listItem))
      }
    }

    "should include a link back to the homepage" in {
      val returnToDashboardSection = document.getElementById("goToDashboard")
      val link = returnToDashboardSection.getElementsByClass(Selectors.link)
      link.text() mustEqual Messages("returnSent.help.link")
      link.get(0).attr("href") mustEqual config.sdilHomeUrl
    }

    "should include a details section" - {

      "that has a details section" - {
        "that has the expected details text" in {
          val details = document.getElementsByClass(Selectors.details).get(0)
          details.getElementsByClass(Selectors.detailsText).text() mustEqual "View the details of your return"
        }
        "has contains the expected content" - {
          UserAnswersTestData.userAnswersModels.foreach {
            case (key, userAnswers) =>
              s"when the $key" - {
                val html1: HtmlFormat.Appendable =
                  returnSentView(returnPeriod, userAnswers, amounts, aSubscription, amountOwed)
                val document1: Document = doc(html1)
                val details = document1.getElementsByClass(Selectors.details).get(0)
                testSummaryLists(key, details, userAnswers, false)
              }
          }
        }
      }
    }
  }
}
