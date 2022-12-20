package controllers

import controllers.testSupport.{Specifications, TestConfiguration}
import play.api.libs.ws.DefaultWSCookie
import play.api.test.WsTestClient

class BroughtIntoUKControllerIntegrationSpec extends Specifications with TestConfiguration {
  "BroughtIntoUKController" should {
    "Ask for are you reporting liable drinks brought into uk from outside uk" in {

      given
        .commonPrecondition
      WsTestClient.withClient { client ⇒
        val result1 = client.url(s"$baseUrl/brought-into-uk")
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
