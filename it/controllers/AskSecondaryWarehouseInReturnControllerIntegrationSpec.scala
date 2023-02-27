package controllers

import controllers.testSupport.{Specifications, TestConfiguration}
import play.api.libs.ws.DefaultWSCookie
import play.api.test.WsTestClient

class AskSecondaryWarehouseInReturnControllerIntegrationSpec extends Specifications with TestConfiguration {
  "AskSecondaryWarehouseInReturnController" should {
    "Ask for if user wants to register any UK warehouses where user used to store liable drinks" in {

      given
        .commonPrecondition

      WsTestClient.withClient { client =>
        val result1 = client.url(s"$baseUrl/ask-secondary-warehouses-in-return")
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

