package controllers.test

import controllers.testSupport.{ITCoreTestData, Specifications, TestConfiguration}
import org.scalatest.TryValues
import play.api.libs.json.Json
import play.api.libs.ws.DefaultWSCookie
import play.api.test.Helpers.{CONTENT_TYPE, JSON, LOCATION}
import play.api.test.WsTestClient
import play.api.libs.ws.JsonBodyWritables.writeableOf_JsValue

class AddressFrontendStubControllerIntegrationSpec extends Specifications
  with TestConfiguration with ITCoreTestData with TryValues {

  val initialisePath = "/test-only/api/init"
  val addressesPath = "/test-only/api/confirmed?id=1234567890"

  s"POST $initialisePath" should {
    "return Accepted with a rampOn url in the header" in {
      WsTestClient.withClient { client =>
        val result1 = client.url(s"$baseUrl$initialisePath")
          .withFollowRedirects(false)
          .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
          .addHttpHeaders((CONTENT_TYPE, JSON))
          .post(Json.obj("options" -> Json.obj("continueUrl" -> "foo")))

        whenReady(result1) { res =>
          res.status mustBe 202
          res.header(LOCATION) mustBe Some("foo?id=foobarwizzbang")
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
          "{\"auditRef\":\"bed4bd24-72da-42a7-9338-f43431b7ed72\"," +
            "\"id\":\"GB990091234524\",\"address\":{\"organisation\":\"Some Trading Name\",\"lines\":[\"10 Other Place\"," +
            "\"Some District\",\"Anytown\"],\"postcode\":\"ZZ1 1ZZ\"," +
            "\"country\":{\"code\":\"GB\",\"name\":\"United Kingdom\"}}}"
        whenReady(result1) { res =>
          res.status mustBe 200
          Json.parse(res.body) mustEqual Json.parse(addressConfirmed)
        }
      }
    }
  }

}
