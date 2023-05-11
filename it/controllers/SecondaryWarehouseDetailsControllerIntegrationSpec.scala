package controllers

import controllers.testSupport.{ITCoreTestData, Specifications, TestConfiguration}
import models.SmallProducer
import org.scalatest.TryValues
import play.api.libs.json.{JsObject, Json}
import play.api.libs.ws.DefaultWSCookie
import play.api.test.WsTestClient
import play.mvc.Http.HeaderNames

class SecondaryWarehouseDetailsControllerIntegrationSpec extends Specifications with TestConfiguration with ITCoreTestData with TryValues {
  "SecondaryWarehouseDetailsController" should {
    "Ask for if user wants to add more warehouses" in {

      setAnswers(newPackerPartialAnswers)
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

    "user selected no with at no warehouse in the list" in {

      val expectedResult:Some[JsObject] = Some(
        Json.obj(
          "ownBrands" -> false,
          "packagedContractPacker" -> true,
          "howManyAsAContractPacker" -> Json.obj("lowBand" -> 1000, "highBand" -> 1000),
          "exemptionsForSmallProducers" -> false,
          "broughtIntoUK" -> false,
          "broughtIntoUkFromSmallProducers" -> false,
          "claimCreditsForExports" -> false,
          "secondaryWarehouseDetails" -> false
        ))

      given
        .commonPrecondition

      val userAnswers = newPackerPartialAnswers

      WsTestClient.withClient { client =>
        val result =
          client.url(s"$baseUrl/secondary-warehouse-details")
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
            .withHttpHeaders("X-Session-ID" -> "XKSDIL000000022",
              "Csrf-Token" -> "nocheck")
            .withFollowRedirects(false)
            .post(Json.obj("value" -> "false"))


        whenReady(result) { res =>
          res.status mustBe 303
          res.header(HeaderNames.LOCATION) mustBe Some(s"/soft-drinks-industry-levy-returns-frontend/check-your-answers")
          getAnswers(sdilNumber).map(userAnswers => userAnswers.data) mustBe expectedResult
        }

      }

    }

  }

}