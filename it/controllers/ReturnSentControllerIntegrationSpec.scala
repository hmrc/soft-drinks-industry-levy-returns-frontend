/*
 * Copyright 2026 HM Revenue & Customs
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

import models.Amounts
import org.jsoup.Jsoup
import play.api.libs.ws.DefaultWSCookie
import play.api.test.Helpers.await
import play.api.test.WsTestClient
import repositories.SDILSessionKeys

class ReturnSentControllerIntegrationSpec extends ReturnSentPageValidationHelper {

  val path = "/return-sent"

  s"GET $path" should {
    "render the return sent page with the expected content" when {
      "a user sends a nil return and amounts of zero" in {
        await(sdilSessionCache.save[Amounts](sdilNumber, SDILSessionKeys.AMOUNTS, amountsZero))
        val nilReturnUserAnswers = emptyUserAnswers.copy(isNilReturn = true, submitted = true)
        setUpData(nilReturnUserAnswers)
        build.commonPreconditionChangeSubscription(aSubscription)
        WsTestClient.withClient { client =>
          val result1 = client
            .url(s"$baseUrl/return-sent")
            .withFollowRedirects(false)
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
            .get()

          whenReady(result1) { res =>
            res.status mustBe 200
            val page = Jsoup.parse(res.body)
            page.title() mustBe "Return sent - Soft Drinks Industry Levy - GOV.UK"
            validatePanelForReturnSent(page)
            validateWhatNextSection(page, None)
            val details = page.getElementsByClass("govuk-details").get(0)
            validateSummaryRowsPresentWithAllNo(details, amountsZero)
          }
        }
      }

      "a user sends a nil return and amounts of have account in credit" in {
        await(sdilSessionCache.save[Amounts](sdilNumber, SDILSessionKeys.AMOUNTS, Amounts(0, 1000, -1000)))
        val nilReturnUserAnswers = emptyUserAnswers.copy(isNilReturn = true, submitted = true)
        setUpData(nilReturnUserAnswers)
        build.commonPreconditionChangeSubscription(aSubscription)
        WsTestClient.withClient { client =>
          val result1 = client
            .url(s"$baseUrl/return-sent")
            .withFollowRedirects(false)
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
            .get()

          whenReady(result1) { res =>
            res.status mustBe 200
            val page = Jsoup.parse(res.body)
            page.title() mustBe "Return sent - Soft Drinks Industry Levy - GOV.UK"
            validatePanelForReturnSent(page)
            validateWhatNextSection(page, amountCredit = Some("£1,000.00"))
            val details = page.getElementsByClass("govuk-details").get(0)
            validateSummaryRowsPresentWithAllNo(details, Amounts(0, 1000, -1000))
          }
        }
      }

      "a user sends a nil return and amounts already owed" in {
        await(sdilSessionCache.save[Amounts](sdilNumber, SDILSessionKeys.AMOUNTS, Amounts(0, -1000, 1000)))
        val nilReturnUserAnswers = emptyUserAnswers.copy(isNilReturn = true, submitted = true)
        setUpData(nilReturnUserAnswers)
        build.commonPreconditionChangeSubscription(aSubscription)
        WsTestClient.withClient { client =>
          val result1 = client
            .url(s"$baseUrl/return-sent")
            .withFollowRedirects(false)
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
            .get()

          whenReady(result1) { res =>
            res.status mustBe 200
            val page = Jsoup.parse(res.body)
            page.title() mustBe "Return sent - Soft Drinks Industry Levy - GOV.UK"
            validatePanelForReturnSent(page)
            validateWhatNextSection(page, Some("£1,000.00"))
            val details = page.getElementsByClass("govuk-details").get(0)
            validateSummaryRowsPresentWithAllNo(details, Amounts(0, -1000, 1000))
          }
        }
      }
      "all answers are no and amounts of zero" in {
        await(sdilSessionCache.save[Amounts](sdilNumber, SDILSessionKeys.AMOUNTS, amountsZero))
        setUpData(checkYourAnswersFullAnswersAllNo.copy(submitted = true))
        build.commonPreconditionChangeSubscription(aSubscription)
        WsTestClient.withClient { client =>
          val result1 = client
            .url(s"$baseUrl/return-sent")
            .withFollowRedirects(false)
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
            .get()

          whenReady(result1) { res =>
            res.status mustBe 200
            val page = Jsoup.parse(res.body)
            page.title() mustBe "Return sent - Soft Drinks Industry Levy - GOV.UK"
            validatePanelForReturnSent(page)
            validateWhatNextSection(page, None)
            val details = page.getElementsByClass("govuk-details").get(0)
            validateSummaryRowsPresentWithAllNo(details, amountsZero)
          }
        }
      }

      "all answers are no and amounts of have account in credit" in {
        await(sdilSessionCache.save[Amounts](sdilNumber, SDILSessionKeys.AMOUNTS, Amounts(0, 1000, -1000)))
        setUpData(checkYourAnswersFullAnswersAllNo.copy(submitted = true))
        build.commonPreconditionChangeSubscription(aSubscription)
        WsTestClient.withClient { client =>
          val result1 = client
            .url(s"$baseUrl/return-sent")
            .withFollowRedirects(false)
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
            .get()

          whenReady(result1) { res =>
            res.status mustBe 200
            val page = Jsoup.parse(res.body)
            page.title() mustBe "Return sent - Soft Drinks Industry Levy - GOV.UK"
            validatePanelForReturnSent(page)
            validateWhatNextSection(page, amountCredit = Some("£1,000.00"))
            val details = page.getElementsByClass("govuk-details").get(0)
            validateSummaryRowsPresentWithAllNo(details, Amounts(0, 1000, -1000))
          }
        }
      }

      "all answers are no and amounts already owed" in {
        await(sdilSessionCache.save[Amounts](sdilNumber, SDILSessionKeys.AMOUNTS, Amounts(0, -1000, 1000)))
        setUpData(checkYourAnswersFullAnswersAllNo.copy(submitted = true))
        build.commonPreconditionChangeSubscription(aSubscription)
        WsTestClient.withClient { client =>
          val result1 = client
            .url(s"$baseUrl/return-sent")
            .withFollowRedirects(false)
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
            .get()

          whenReady(result1) { res =>
            res.status mustBe 200
            val page = Jsoup.parse(res.body)
            page.title() mustBe "Return sent - Soft Drinks Industry Levy - GOV.UK"
            validatePanelForReturnSent(page)
            validateWhatNextSection(page, Some("£1,000.00"))
            val details = page.getElementsByClass("govuk-details").get(0)
            validateSummaryRowsPresentWithAllNo(details, Amounts(0, -1000, 1000))
          }
        }
      }

      "all answers equal yes with litres were submitted and the balance brought forward is 0" in {
        await(sdilSessionCache.save[Amounts](sdilNumber, SDILSessionKeys.AMOUNTS, Amounts(420, 0, 420)))
        setUpData(checkYourAnswersFullAnswers.copy(submitted = true))
        build.commonPreconditionChangeSubscription(aSubscription).sdilBackend.balanceHistoryNone(sdilNumber)

        WsTestClient.withClient { client =>
          val result1 = client
            .url(s"$baseUrl/return-sent")
            .withFollowRedirects(false)
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
            .get()

          whenReady(result1) { res =>
            res.status mustBe 200
            val page = Jsoup.parse(res.body)
            page.title() mustBe "Return sent - Soft Drinks Industry Levy - GOV.UK"
            validatePanelForReturnSent(page)
            validateWhatNextSection(page, Some("£420.00"))
            val details = page.getElementsByClass("govuk-details").get(0)
            validateSummaryRowsPresentWithAllYes(details, Amounts(420, 0, 420), false)
          }
        }
      }

      "all answers equal yes with litres were submitted and the balance brought forward is positive" in {
        await(sdilSessionCache.save[Amounts](sdilNumber, SDILSessionKeys.AMOUNTS, Amounts(420, -1000, 1420)))

        setUpData(checkYourAnswersFullAnswers.copy(submitted = true))
        build.commonPreconditionChangeSubscription(aSubscription).sdilBackend.balanceHistoryInCredit(sdilNumber)

        WsTestClient.withClient { client =>
          val result1 = client
            .url(s"$baseUrl/return-sent")
            .withFollowRedirects(false)
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
            .get()

          whenReady(result1) { res =>
            res.status mustBe 200
            val page = Jsoup.parse(res.body)
            page.title() mustBe "Return sent - Soft Drinks Industry Levy - GOV.UK"
            validatePanelForReturnSent(page)
            validateWhatNextSection(page, Some("£1,420.00"))
            val details = page.getElementsByClass("govuk-details").get(0)
            validateSummaryRowsPresentWithAllYes(details, Amounts(420, -1000, 1420), false)
          }
        }
      }

      "all answers equal yes with litres were submitted and the balance brought forward is negative" in {
        await(sdilSessionCache.save[Amounts](sdilNumber, SDILSessionKeys.AMOUNTS, Amounts(420, 2000, -1580)))

        setUpData(checkYourAnswersFullAnswers.copy(submitted = true))
        build.commonPreconditionChangeSubscription(aSubscription).sdilBackend.balanceHistory(sdilNumber)

        WsTestClient.withClient { client =>
          val result1 = client
            .url(s"$baseUrl/return-sent")
            .withFollowRedirects(false)
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
            .get()

          whenReady(result1) { res =>
            res.status mustBe 200
            val page = Jsoup.parse(res.body)
            page.title() mustBe "Return sent - Soft Drinks Industry Levy - GOV.UK"
            validatePanelForReturnSent(page)
            validateWhatNextSection(page, amountCredit = Some("£1,580.00"))
            val details = page.getElementsByClass("govuk-details").get(0)
            validateSummaryRowsPresentWithAllYes(details, Amounts(420, 2000, -1580), false)
          }
        }
      }
    }
  }
}
