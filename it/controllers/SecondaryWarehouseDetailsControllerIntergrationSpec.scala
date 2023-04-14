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

    "user selected remove on one of the addresses" in {

      setAnswers(newPackerPartialAnswers.success.value)
      given
        .commonPrecondition

      WsTestClient.withClient { client =>
        val result = client.url(s"$baseUrl/remove-warehouse-details/1")
          .withFollowRedirects(false)
          .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
          .get()

        whenReady(result) { res =>
          res.status mustBe 200
        }
      }
    }

    "user inputs remove on index that doesn't exist" in {

      setAnswers(newPackerPartialAnswers.success.value)
      given
        .commonPrecondition

      WsTestClient.withClient { client =>
        val result = client.url(s"$baseUrl/remove-warehouse-details/3")
          .withFollowRedirects(false)
          .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
          .get()

        whenReady(result) { res =>
          res.status mustBe 500
        }
      }
    }

  }

}