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

import org.scalatest.TryValues
import play.api.libs.json.{JsObject, Json}
import play.api.libs.ws.DefaultWSCookie
import play.api.test.WsTestClient
import play.mvc.Http.HeaderNames
import play.api.libs.ws.JsonBodyWritables.writeableOf_JsValue

class BroughtIntoUKControllerIntegrationSpec extends ControllerITTestHelper with TryValues {
  "BroughtIntoUKController" should {

    val broughtIntoUkUrl = "brought-into-uk"

    "Ask for are you reporting liable drinks brought into uk from outside uk" in {
      val userAnswers = broughtIntoUkPartialAnswers.success.value
      setUpData(userAnswers)
      build
        .commonPreconditionChangeSubscription(aSubscription)
      WsTestClient.withClient { client =>
        val result1 = client
          .url(s"$baseUrl/$broughtIntoUkUrl")
          .withFollowRedirects(false)
          .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
          .get()

        whenReady(result1) { res =>
          res.status mustBe 200
        }

      }
    }

    "Post the brought into UK " when {
      "user selected yes " in {
        build
          .commonPreconditionChangeSubscription(aSubscription)

        val expectedResult: Some[JsObject] = Some(Json.obj("broughtIntoUK" -> true))
        setUpData(emptyUserAnswers)

        WsTestClient.withClient { client =>
          val result =
            client
              .url(s"$baseUrl/$broughtIntoUkUrl")
              .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
              .withHttpHeaders("X-Session-ID" -> "XKSDIL000000022", "Csrf-Token" -> "nocheck")
              .withFollowRedirects(false)
              .post(Json.obj("value" -> true))

          whenReady(result) { res =>
            res.status mustBe 303
            res.header(HeaderNames.LOCATION) mustBe Some(s"/soft-drinks-industry-levy-returns-frontend/how-many-brought-into-uk")
            getAnswers(sdilNumber).map(userAnswers => userAnswers.data) mustBe expectedResult
          }

        }
      }

      "user selected no " in {
        build
          .commonPreconditionChangeSubscription(aSubscription)

        val userAnswers = exemptionsForSmallProducersFullAnswers.success.value
        setUpData(userAnswers)

        WsTestClient.withClient { client =>
          val result =
            client
              .url(s"$baseUrl/$broughtIntoUkUrl")
              .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
              .withHttpHeaders("X-Session-ID" -> "XKSDIL000000022", "Csrf-Token" -> "nocheck")
              .withFollowRedirects(false)
              .post(Json.obj("value" -> false))

          whenReady(result) { res =>
            res.status mustBe 303
            res.header(HeaderNames.LOCATION) mustBe Some(s"/soft-drinks-industry-levy-returns-frontend/brought-into-uk-from-small-producers")
          }

        }
      }

    }
    testUnauthorisedUser(baseUrl + "/" + broughtIntoUkUrl)
  }

}
