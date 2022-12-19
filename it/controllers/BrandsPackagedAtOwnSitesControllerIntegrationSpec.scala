package controllers

import controllers.testSupport.{Specifications, TestConfiguration}
import play.api.libs.ws.DefaultWSCookie
import play.api.test.WsTestClient

class BrandsPackagedAtOwnSitesControllerIntegrationSpec extends Specifications with TestConfiguration {
  "PackagedContractPackerController" should {
    "Ask for many litres of liable drinks have user packaged at UK sites they operate" in {

      given
        .commonPrecondition

      WsTestClient.withClient { client ⇒
        val result1 = client.url(s"$baseUrl/how-many-own-brands-packaged-at-own-sites")
          .withFollowRedirects(false)
          .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
          .get()

        whenReady(result1) { res ⇒
          res.status mustBe 303
        }

      }
    }
  }

}
