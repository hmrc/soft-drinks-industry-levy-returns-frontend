package controllers

import controllers.testSupport.{Specifications, TestConfiguration}
import play.api.test.WsTestClient

class HowManyBoughtIntoUkControllerIntegrationSpec extends Specifications with TestConfiguration {
  "HowManyBoughtIntoUkController" should {
    "Ask for how many liable drinks brought in UK" in {
      given
        .commonPrecondition

      WsTestClient.withClient { client ⇒
        val result1 = client.url(s"$baseUrl/how-many-brought-into-uk")
          .withFollowRedirects(false)
          //..addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
          .get()

        whenReady(result1) { res ⇒
          res.status mustBe 303
        }

      }
    }
  }

}
