package controllers

import controllers.testSupport.{ITCoreTestData, Specifications, TestConfiguration}
import org.scalatest.TryValues
import play.api.libs.ws.DefaultWSCookie
import play.api.test.WsTestClient

class AddressLookupControllerIntegrationSpec extends Specifications
  with TestConfiguration with ITCoreTestData with TryValues {

  val path = "/address-lookup/callback"

  s"GET $path" should {
    "return Ok" in {
      WsTestClient.withClient { client =>
        val result1 = client.url(s"$baseUrl$path")
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
