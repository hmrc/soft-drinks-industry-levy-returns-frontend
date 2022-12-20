package controllers

import controllers.testSupport.{Specifications, TestConfiguration}
import play.api.libs.ws.DefaultWSCookie
import play.api.test.WsTestClient

class HowManyCreditsForExportControllerIntegrationSpec extends Specifications with TestConfiguration {
  "HowManyCreditsForExportController" should {
    "Ask for how many credits user wants to claim for liable drinks that have ben exported" in {
      given
        .commonPrecondition

      WsTestClient.withClient { client ⇒
        val result1 = client.url(s"$baseUrl/how-many-credits-for-exports")
          .withFollowRedirects(false)
          .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
          .get()

        whenReady(result1) { res ⇒
          res.status mustBe 200
        }

      }
    }
  }
}