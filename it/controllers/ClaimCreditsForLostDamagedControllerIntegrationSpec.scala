package controllers

import controllers.testSupport.{ITCoreTestData, Specifications, TestConfiguration}
import org.scalatest.TryValues
import play.api.libs.json.Json
import play.api.libs.ws.DefaultWSCookie
import play.api.test.WsTestClient
import play.mvc.Http.HeaderNames

class ClaimCreditsForLostDamagedControllerIntegrationSpec extends Specifications with TestConfiguration with  ITCoreTestData with TryValues{
  "ClaimCreditsForLostDamagedController" should {

    "Ask for if user want to claim a credit for liable drinks which they been lost or destroyed" in {

      given
        .commonPrecondition

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

        val userAnswers = creditsForLostDamagedPartialAnswers.success.value
        setAnswers(userAnswers)
        given
          .commonPrecondition

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
          }

        }
      }

      "user selected no " when {

        "user is a new importer" in {
          given
            .commonPrecondition

          val userAnswers = creditsForLostDamagedPartialAnswers.success.value
          setAnswers(userAnswers)

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
              res.header(HeaderNames.LOCATION) mustBe Some(s"/soft-drinks-industry-levy-returns-frontend/return-change-registration")
            }

          }
        }

        "user is a neither a new  importer or new packer" in {
          given
            .commonPrecondition

          val userAnswers = creditsForCopackerDamagedPartialAnswers.success.value
          setAnswers(userAnswers)

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
              res.header(HeaderNames.LOCATION) mustBe Some(s"/soft-drinks-industry-levy-returns-frontend/check-your-answers")
            }

          }
        }

      }

    }
  }
}