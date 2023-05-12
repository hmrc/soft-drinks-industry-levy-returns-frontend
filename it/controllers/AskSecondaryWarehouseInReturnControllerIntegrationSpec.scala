package controllers

import controllers.testSupport.{ITCoreTestData, Specifications, TestConfiguration}
import play.api.libs.json.{JsObject, Json}
import play.api.libs.ws.DefaultWSCookie
import play.api.test.WsTestClient

class AskSecondaryWarehouseInReturnControllerIntegrationSpec extends Specifications with TestConfiguration with ITCoreTestData{
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

    "user selects yes and saves and continues updating the user answers" in {


      val expectedResult:Some[JsObject] = Some(
        Json.obj(
          "ownBrands" -> true,
          "brandsPackagedAtOwnSites" -> Json.obj("lowBand" -> 1000,"highBand" -> 1000),
          "packagedContractPacker" -> false,
          "claimCreditsForLostDamaged"->false,
        "askSecondaryWarehouseInReturn"->true
        ))

      given
        .commonPrecondition

      WsTestClient.withClient { client =>
        val result1 = client.url(s"$baseUrl/ask-secondary-warehouses-in-return")
          .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
          .withHttpHeaders("X-Session-ID" -> "XKSDIL000000022",
            "Csrf-Token" -> "nocheck")
          .withFollowRedirects(false)
          .post(Json.obj("value" ->  true))

        whenReady(result1) { res =>
          res.status mustBe 303
          getAnswers(sdilNumber).map(userAnswers => userAnswers.data) mustBe expectedResult
        }

      }
    }
  }

}

