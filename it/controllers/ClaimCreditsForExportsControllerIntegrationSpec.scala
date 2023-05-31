package controllers

import controllers.testSupport.{ITCoreTestData, Specifications, TestConfiguration}
import org.scalatest.TryValues
import pages.{BroughtIntoUKPage, HowManyBroughtIntoUkPage}
import play.api.libs.json.{JsObject, Json}
import play.api.libs.ws.DefaultWSCookie
import play.api.test.WsTestClient
import play.mvc.Http.HeaderNames

class ClaimCreditsForExportsControllerIntegrationSpec extends Specifications with TestConfiguration with  ITCoreTestData with TryValues {
  "ClaimCreditsForExportsController" should {

    "Ask if user is needing to claim a credit for liable drinks that have been exported" in {
      val userAnswers = broughtIntoUkFromSmallProducersFullAnswers.success.value
      setUpData(userAnswers)
      given
        .commonPrecondition(aSubscription)

      WsTestClient.withClient { client =>
        val result1 = client.url(s"$baseUrl/claim-credits-for-exports")
          .withFollowRedirects(false)
          .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
          .get()

        whenReady(result1) { res =>
          res.status mustBe 200
        }

      }
    }

    "Post the brought into UK " when {
      "user selected yes " in {
        val expectedResult: Some[JsObject] = Some(Json.obj("claimCreditsForExports"-> true))
        given
          .commonPrecondition(aSubscription)
        setUpData(emptyUserAnswers)

        WsTestClient.withClient { client =>
          val result =
            client.url(s"$baseUrl/claim-credits-for-exports")
              .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
              .withHttpHeaders("X-Session-ID" -> "XKSDIL000000022",
                "Csrf-Token" -> "nocheck")
              .withFollowRedirects(false)
              .post(Json.obj("value" -> true))


          whenReady(result) { res =>
            res.status mustBe 303
            res.header(HeaderNames.LOCATION) mustBe Some(s"/soft-drinks-industry-levy-returns-frontend/how-many-credits-for-exports")
            getAnswers(sdilNumber).map(userAnswers => userAnswers.data) mustBe expectedResult
          }
        }
      }

      "user selected no " in {

        val expectedResult: Some[JsObject] = Some(Json.obj("claimCreditsForExports"-> false))
        given
          .commonPrecondition(aSubscription)
        setUpData(emptyUserAnswers)

        WsTestClient.withClient { client =>
          val result =
            client.url(s"$baseUrl/claim-credits-for-exports")
              .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
              .withHttpHeaders("X-Session-ID" -> "XKSDIL000000022",
                "Csrf-Token" -> "nocheck")
              .withFollowRedirects(false)
              .post(Json.obj("value" -> false))


          whenReady(result) { res =>
            res.status mustBe 303
            res.header(HeaderNames.LOCATION) mustBe Some(s"/soft-drinks-industry-levy-returns-frontend/claim-credits-for-lost-damaged")
            getAnswers(sdilNumber).map(userAnswers => userAnswers.data) mustBe expectedResult
          }

        }
      }
      "user selected no and has litres in bands" in {
        given
          .commonPrecondition(aSubscription)

        val userAnswers = claimCreditsForLostDamagedPageWithLitresFullAnswers.success.value
        setUpData(userAnswers)

        WsTestClient.withClient { client =>
          val result =
            client.url(s"$baseUrl/claim-credits-for-exports")
              .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
              .withHttpHeaders("X-Session-ID" -> "XKSDIL000000022",
                "Csrf-Token" -> "nocheck")
              .withFollowRedirects(false)
              .post(Json.obj("value" -> false))


          whenReady(result) { res =>
            res.status mustBe 303
            res.header(HeaderNames.LOCATION) mustBe Some(s"/soft-drinks-industry-levy-returns-frontend/claim-credits-for-lost-damaged")
            val updatedAnswers = getAnswers(sdilNumber).get
            updatedAnswers.get(BroughtIntoUKPage) mustBe Some(false)
            updatedAnswers.get(HowManyBroughtIntoUkPage) mustBe None
          }

        }
      }
    }
  }
}