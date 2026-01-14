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

class BrandsPackagedAtOwnSitesControllerIntegrationSpec extends ControllerITTestHelper with TryValues {

  "BrandsPackagedAtOwnSitesController" should {

    "Ask for many litres of liable drinks have user packaged at UK sites they operate" in {
      val userAnswers = ownBrandPageAnswers.success.value
      setUpData(userAnswers)
      build
        .commonPreconditionChangeSubscription(aSubscription)

      WsTestClient.withClient { client =>
        val result1 = client
          .url(s"$baseUrl/how-many-own-brands-packaged-at-own-sites")
          .withFollowRedirects(false)
          .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
          .get()

        whenReady(result1) { res =>
          res.status mustBe 200
        }

      }
    }

    "Post the Own brand packaged at own sites " in {

      val expectedResult: Some[JsObject] =
        Some(Json.obj("ownBrands" -> true, "brandsPackagedAtOwnSites" -> Json.obj("lowBand" -> 1000, "highBand" -> 1000)))

      build
        .commonPreconditionChangeSubscription(aSubscription)

      val userAnswers = ownBrandPageAnswers.success.value
      setUpData(userAnswers)

      WsTestClient.withClient { client =>
        val result =
          client
            .url(s"$baseUrl/how-many-own-brands-packaged-at-own-sites")
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
            .withHttpHeaders("X-Session-ID" -> "XKSDIL000000022", "Csrf-Token" -> "nocheck")
            .withFollowRedirects(false)
            .post(Json.obj("lowBand" -> "1000", "highBand" -> "1000"))

        whenReady(result) { res =>
          res.status mustBe 303
          res.header(HeaderNames.LOCATION) mustBe Some(s"/soft-drinks-industry-levy-returns-frontend/packaged-as-contract-packer")
          getAnswers(sdilNumber).map(userAnswers => userAnswers.data) mustBe expectedResult
        }

      }
    }

    testUnauthorisedUser(baseUrl + "/how-many-own-brands-packaged-at-own-sites")

  }

}
