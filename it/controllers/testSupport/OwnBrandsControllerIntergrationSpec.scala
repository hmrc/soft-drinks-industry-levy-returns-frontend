package controllers.testSupport

import play.api.libs.ws.DefaultWSCookie
import play.api.test.WsTestClient

class OwnBrandsControllerIntergrationSpec extends Specifications with TestConfiguration {
  "OwnBrandsController" should {
    "Ask if user is reporting liable drinks they have packaged as a third party or contract packer at UK sites user operates" in {

      given
        .commonPrecondition

      WsTestClient.withClient { client ⇒
        val result1 = client.url(s"$baseUrl/own-brands-packaged-at-own-sites")
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