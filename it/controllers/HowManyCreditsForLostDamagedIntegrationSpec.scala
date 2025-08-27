package controllers

import play.api.libs.ws.DefaultWSCookie
import play.api.test.WsTestClient
import play.api.libs.ws.JsonBodyWritables.writeableOf_JsValue

class HowManyCreditsForLostDamagedIntegrationSpec extends ControllerITTestHelper {

  "controller" should {
    "Ask for many litres of liable drinks have user packaged at UK sites they operate" in {
      setUpData(emptyUserAnswers)
      build
        .commonPreconditionChangeSubscription(aSubscription)

      WsTestClient.withClient { client =>
        val result1 = client.url(s"$baseUrl/how-many-credits-for-lost-damaged")
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