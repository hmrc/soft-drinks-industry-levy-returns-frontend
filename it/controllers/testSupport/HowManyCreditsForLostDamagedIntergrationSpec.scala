package controllers.testSupport

import play.api.libs.ws.DefaultWSCookie
import play.api.test.WsTestClient

class HowManyCreditsForLostDamagedIntergrationSpec extends Specifications with TestConfiguration {
  "PackagedContractPackerController" should {
    "Ask for many litres of liable drinks have user packaged at UK sites they operate" in {

      given
        .commonPrecondition

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
  }

}