package controllers

import controllers.testSupport.{Specifications, TestConfiguration}
import play.api.libs.ws.DefaultWSCookie
import play.api.test.WsTestClient

class PackagedContractPackerControllerIntegrationSpec extends Specifications with TestConfiguration {
  "PackagedContractPackerController" should {
    "Ask for if user is  reporting liable drink as a third party or contract packer at UK sites user operate" in {

      given
        .commonPrecondition

      WsTestClient.withClient { client ⇒
        val result1 = client.url(s"$baseUrl/packaged-as-contract-packer")
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
