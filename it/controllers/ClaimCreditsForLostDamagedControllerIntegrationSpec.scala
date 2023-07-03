package controllers

import org.scalatest.TryValues
import play.api.libs.json.{JsObject, Json}
import play.api.libs.ws.DefaultWSCookie
import play.api.test.WsTestClient
import play.mvc.Http.HeaderNames

class ClaimCreditsForLostDamagedControllerIntegrationSpec extends ControllerITTestHelper with TryValues{
  "ClaimCreditsForLostDamagedController" should {

    "Ask for if user want to claim a credit for liable drinks which they been lost or destroyed" in {

      given
        .commonPreconditionChangeSubscription(aSubscription)
      setUpData(emptyUserAnswers)

      WsTestClient.withClient { client =>
        val result1 = client.url(s"$baseUrl/claim-credits-for-lost-damaged")
          .withFollowRedirects(false)
          .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
          .get()

        whenReady(result1) { res =>
          res.status mustBe 200
        }

      }
    }

    "Post the claim credit for lost damaged " when {

      "user selected yes " in {

        val expectedResult:Some[JsObject] = Some(Json.obj("claimCreditsForLostDamaged"-> true))

        given
          .commonPreconditionChangeSubscription(aSubscription)
        setUpData(emptyUserAnswers
        )
        WsTestClient.withClient { client =>
          val result =
            client.url(s"$baseUrl/claim-credits-for-lost-damaged")
              .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
              .withHttpHeaders("X-Session-ID" -> "XKSDIL000000022",
                "Csrf-Token" -> "nocheck")
              .withFollowRedirects(false)
              .post(Json.obj("value" -> true))


          whenReady(result) { res =>
            res.status mustBe 303
            res.header(HeaderNames.LOCATION) mustBe Some(s"/soft-drinks-industry-levy-returns-frontend/how-many-credits-for-lost-damaged")
            getAnswers(sdilNumber).map(userAnswers => userAnswers.data) mustBe expectedResult
          }

        }
      }

    }

      "user selected no " in {
        val expectedResult:Some[JsObject] = Some(Json.obj("claimCreditsForLostDamaged"-> false))

        given
          .commonPreconditionChangeSubscription(aSubscription)
        setUpData(emptyUserAnswers)

        WsTestClient.withClient { client =>
          val result =
            client.url(s"$baseUrl/claim-credits-for-lost-damaged")
              .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
              .withHttpHeaders("X-Session-ID" -> "XKSDIL000000022",
                "Csrf-Token" -> "nocheck")
              .withFollowRedirects(false)
              .post(Json.obj("value" -> false))


          whenReady(result) { res =>
            res.status mustBe 303
            res.header(HeaderNames.LOCATION) mustBe Some("/soft-drinks-industry-levy-returns-frontend/check-your-answers")
            getAnswers(sdilNumber).map(userAnswers => userAnswers.data) mustBe expectedResult
          }
        }
      }
    testUnauthorisedUser(baseUrl + "/claim-credits-for-lost-damaged")
    }
}