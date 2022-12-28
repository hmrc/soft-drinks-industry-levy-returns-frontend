package controllers

import controllers.testSupport.{Specifications, TestConfiguration}
import play.api.libs.ws.DefaultWSCookie
import play.api.test.WsTestClient

class SmallProducerDetailsControllerIntegrationSpec extends Specifications with TestConfiguration {
  "SmallProducerDetailsController" should {
    "Ask for if user wants to add more small producer" in {

      given
        .commonPrecondition

      WsTestClient.withClient { client ⇒
        val result1 = client.url(s"$baseUrl/small-producer-details")
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
