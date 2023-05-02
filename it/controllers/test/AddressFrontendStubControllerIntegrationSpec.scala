package controllers.test

import controllers.testSupport.{ITCoreTestData, Specifications, TestConfiguration}
import org.scalatest.TryValues
import play.api.libs.json.Json
import play.api.libs.ws.DefaultWSCookie
import play.api.test.Helpers.{CONTENT_TYPE, JSON, LOCATION}
import play.api.test.WsTestClient

class AddressFrontendStubControllerIntegrationSpec extends Specifications
  with TestConfiguration with ITCoreTestData with TryValues {

  val initialisePath = "/test-only/api/init"
  val rampOnPath = "/test-only/rampOn"
  val addressesPath = "/test-only/api/confirmed?id=1234567890"

  s"POST $initialisePath" should {
    "return Accepted with a rampOn url in the header" in {
      WsTestClient.withClient { client =>
        val result1 = client.url(s"$baseUrl$initialisePath")
          .withFollowRedirects(false)
          .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
          .addHttpHeaders((CONTENT_TYPE, JSON))
          .post(Json.obj())

        whenReady(result1) { res =>
          res.status mustBe 202
          res.header(LOCATION) mustBe Some("/soft-drinks-industry-levy-returns-frontend/test-only/rampOn")
        }
      }
    }
  }

  s"GET $rampOnPath" should {
    "redirect to the callback url" in {
      WsTestClient.withClient { client =>
        val result1 = client.url(s"$baseUrl$rampOnPath")
          .withFollowRedirects(false)
          .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
          .get()

        whenReady(result1) { res =>
          res.status mustBe 303
          res.header(LOCATION) mustBe Some("/soft-drinks-industry-levy-returns-frontend/address-lookup/callback")
        }
      }
    }
  }

  s"GET $addressesPath" should {
    "return Ok with the confirmed address" in {
      WsTestClient.withClient { client =>
        val result1 = client.url(s"$baseUrl$addressesPath")
          .withFollowRedirects(false)
          .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
          .get()

        val addressConfirmed =
          "[{\"auditRef\":\"bed4bd24-72da-42a7-9338-f43431b7ed72\"," +
            "\"id\":\"GB990091234524\",\"address\":{\"organisation\":\"Some Trading Name\",\"lines\":[\"10 Other Place\"," +
            "\"Some District\",\"Anytown\"],\"postcode\":\"ZZ1 1ZZ\"," +
            "\"country\":{\"code\":\"GB\",\"name\":\"United Kingdom\"}}}]"
        whenReady(result1) { res =>
          res.status mustBe 200
          res.body mustEqual addressConfirmed
        }
      }
    }
  }

}
