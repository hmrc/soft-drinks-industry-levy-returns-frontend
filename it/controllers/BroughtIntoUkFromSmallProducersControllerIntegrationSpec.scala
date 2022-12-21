package controllers

import controllers.testSupport.{Specifications, TestConfiguration}
import play.api.libs.ws.DefaultWSCookie
import play.api.test.WsTestClient

class BroughtIntoUkFromSmallProducersControllerIntegrationSpec extends Specifications with TestConfiguration {
  "BroughtIntoUkFromSmallProducersController" should {
    "Ask for Are you reporting liable drinks you have brought into the UK from small producers" in {

      given
        .commonPrecondition
      WsTestClient.withClient { client ⇒
        val result1 = client.url(s"$baseUrl/brought-into-uk-from-small-producers")
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
