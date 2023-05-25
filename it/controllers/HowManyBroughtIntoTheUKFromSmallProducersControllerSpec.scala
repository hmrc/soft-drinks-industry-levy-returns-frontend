package controllers

import controllers.testSupport.{ITCoreTestData, Specifications, TestConfiguration}
import org.scalatest.TryValues
import play.api.libs.json.{JsObject, Json}
import play.api.libs.ws.DefaultWSCookie
import play.api.test.WsTestClient
import play.mvc.Http.HeaderNames

class HowManyBroughtIntoTheUKFromSmallProducersControllerSpec extends Specifications with TestConfiguration with ITCoreTestData with TryValues {
  "HowManyBroughtIntoTheUKFromSmallProducersController" should {
    "Ask for many litres of liable drinks have user packaged at UK sites they operate" in {
      setUpData(broughtIntoUkFullAnswers.success.value)
      given
        .commonPrecondition

      WsTestClient.withClient { client =>
        val result1 = client.url(s"$baseUrl/how-many-into-uk-small-producers")
          .withFollowRedirects(false)
          .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
          .get()

        whenReady(result1) { res =>
          res.status mustBe 200
        }

      }
    }

    "Post the Own brand packaged at own sites " in {

      val expectedResult:Some[JsObject] = Some(
        Json.obj(
          "ownBrands" -> true,
          "brandsPackagedAtOwnSites" -> Json.obj("lowBand" -> 1000,"highBand" -> 1000),
          "packagedContractPacker" -> true,
          "howManyAsAContractPacker" -> Json.obj("lowBand" -> 1000, "highBand" -> 1000),
          "howManyBroughtIntoTheUKFromSmallProducers"-> Json.obj("lowBand" -> 1000, "highBand" -> 1000)
        ))

      given
        .commonPrecondition

      val userAnswers = howManyAsContractPackerFullAnswers.success.value
      setUpData(userAnswers)

      WsTestClient.withClient { client =>
        val result =
          client.url(s"$baseUrl/how-many-into-uk-small-producers")
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
            .withHttpHeaders("X-Session-ID" -> "XKSDIL000000022",
              "Csrf-Token" -> "nocheck")
            .withFollowRedirects(false)
            .post(Json.obj("lowBand" -> "1000", "highBand" -> "1000"))


        whenReady(result) { res =>
          res.status mustBe 303
          res.header(HeaderNames.LOCATION) mustBe Some(s"/soft-drinks-industry-levy-returns-frontend/claim-credits-for-exports")
          getAnswers(sdilNumber).map(userAnswers => userAnswers.data) mustBe expectedResult
        }

      }

    }

  }

}