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

import play.api.libs.ws.DefaultWSCookie
import play.api.test.WsTestClient

class HowManyCreditsForLostDamagedIntegrationSpec extends ControllerITTestHelper {

  "controller" should {
    "Ask for many litres of liable drinks have user packaged at UK sites they operate" in {
      setUpData(emptyUserAnswers)
      build
        .commonPreconditionChangeSubscription(aSubscription)

      WsTestClient.withClient { client =>
        val result1 = client
          .url(s"$baseUrl/how-many-credits-for-lost-damaged")
          .withFollowRedirects(false)
          .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
          .get()

        whenReady(result1) { res =>
          res.status mustBe 200
        }

      }
    }
    testUnauthorisedUser(baseUrl + "/how-many-credits-for-lost-damaged")
  }

}
