package controllers

import controllers.testSupport.{ITCoreTestData, Specifications, TestConfiguration}
import org.scalatest.TryValues
import play.api.libs.json.{JsObject, Json}
import play.api.libs.ws.DefaultWSCookie
import play.api.test.WsTestClient
import play.mvc.Http.HeaderNames

class HowManyCreditsForExportControllerIntegrationSpec extends Specifications with TestConfiguration with  ITCoreTestData with TryValues {
  "HowManyCreditsForExportController" should {

    "Ask for how many credits user wants to claim for liable drinks that have ben exported" in {
      val userAnswers = broughtIntoUkFromSmallProducersFullAnswers.success.value
      setAnswers(userAnswers)

      given
        .commonPrecondition

      WsTestClient.withClient { client =>
        val result1 = client.url(s"$baseUrl/how-many-credits-for-exports")
          .withFollowRedirects(false)
          .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
          .get()

        whenReady(result1) { res =>
          res.status mustBe 200
        }

      }
    }

    "Post how many claim credits for export " in {
      val expectedResult:Some[JsObject] = Some(
        Json.obj(
          "ownBrands" -> true,
          "brandsPackagedAtOwnSites" -> Json.obj("lowBand" -> 1000,"highBand" -> 1000),
          "packagedContractPacker" -> true,
          "howManyAsAContractPacker" -> Json.obj("lowBand" -> 1000, "highBand" -> 1000),
          "exemptionsForSmallProducers" -> false,
          "broughtIntoUK" -> false,
          "broughtIntoUkFromSmallProducers" -> false,
          "howManyCreditsForExport" -> Json.obj("lowBand" -> 1000, "highBand" ->1000)
        ))

      given
        .commonPrecondition


      WsTestClient.withClient { client =>
        val result =
          client.url(s"$baseUrl/how-many-credits-for-exports")
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
            .withHttpHeaders("X-Session-ID" -> "XKSDIL000000022",
              "Csrf-Token" -> "nocheck")
            .withFollowRedirects(false)
            .post(Json.obj("lowBand" -> "1000", "highBand" -> "1000"))


        whenReady(result) { res =>
          res.status mustBe 303
          res.header(HeaderNames.LOCATION) mustBe Some(s"/soft-drinks-industry-levy-returns-frontend/claim-credits-for-lost-damaged")
          getAnswers(sdilNumber).map(userAnswers => userAnswers.data) mustBe expectedResult
        }

      }

    }
  }
}