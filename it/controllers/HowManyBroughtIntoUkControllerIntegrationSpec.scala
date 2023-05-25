package controllers

import controllers.testSupport.{ITCoreTestData, Specifications, TestConfiguration}
import org.scalatest.TryValues
import play.api.libs.json.{JsObject, Json}
import play.api.libs.ws.DefaultWSCookie
import play.api.test.WsTestClient
import play.mvc.Http.HeaderNames

class HowManyBroughtIntoUkControllerIntegrationSpec extends Specifications with TestConfiguration  with  ITCoreTestData with TryValues{
  "HowManyBroughtIntoUkController" should {

    "Ask for how many liable drinks brought in UK" in {

      val userAnswers = broughtIntoUkFullAnswers.success.value
      setUpData(userAnswers)
      given
        .commonPrecondition

      WsTestClient.withClient { client =>
        val result1 = client.url(s"$baseUrl/how-many-brought-into-uk")
          .withFollowRedirects(false)
          .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
          .get()

        whenReady(result1) { res =>
          res.status mustBe 200
        }

      }
    }

    "Post the how many brought into UK " in {

      val expectedResult:Some[JsObject] = Some(
        Json.obj(
          "ownBrands" -> true,
          "brandsPackagedAtOwnSites" -> Json.obj("lowBand" -> 1000,"highBand" -> 1000),
          "packagedContractPacker" -> true,
          "howManyAsAContractPacker" -> Json.obj("lowBand" -> 1000, "highBand" -> 1000),
          "exemptionsForSmallProducers" -> false,
          "broughtIntoUK" -> true,
          "HowManyBroughtIntoUk" -> Json.obj("lowBand" -> 1000, "highBand" -> 1000)
        ))

      val userAnswers = howManyBroughtIntoUkFullAnswers.success.value
      setUpData(userAnswers)

      given
        .commonPrecondition


      WsTestClient.withClient { client =>
        val result =
          client.url(s"$baseUrl/how-many-brought-into-uk")
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
            .withHttpHeaders("X-Session-ID" -> "XKSDIL000000022",
              "Csrf-Token" -> "nocheck")
            .withFollowRedirects(false)
            .post(Json.obj("lowBand" -> "1000", "highBand" -> "1000"))


        whenReady(result) { res =>
          res.status mustBe 303
          res.header(HeaderNames.LOCATION) mustBe Some(s"/soft-drinks-industry-levy-returns-frontend/brought-into-uk-from-small-producers")
          getAnswers(sdilNumber).map(userAnswers => userAnswers.data) mustBe expectedResult
        }

      }

    }
  }

}
