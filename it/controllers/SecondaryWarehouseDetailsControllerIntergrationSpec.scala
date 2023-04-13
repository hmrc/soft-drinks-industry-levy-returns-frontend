package controllers

import controllers.testSupport.{ITCoreTestData, Specifications, TestConfiguration}
import org.scalatest.TryValues
import play.api.libs.ws.DefaultWSCookie
import play.api.test.WsTestClient

class SecondaryWarehouseDetailsControllerIntergrationSpec extends Specifications with TestConfiguration with ITCoreTestData with TryValues {
  "SecondaryWarehouseDetailsController" should {
    "Ask for if user wants to add more warehouses" in {

      setAnswers(newPackerPartialAnswers.success.value)
      given
        .commonPrecondition

      WsTestClient.withClient { client =>
        val result1 = client.url(s"$baseUrl/secondary-warehouse-details")
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