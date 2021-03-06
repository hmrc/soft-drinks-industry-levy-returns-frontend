package controllers

import controllers.testSupport.{ITCoreTestData, Specifications, TestConfiguration}
import org.scalatest.TryValues
import play.api.libs.json.Json
import play.api.libs.ws.DefaultWSCookie
import play.api.test.WsTestClient
import play.mvc.Http.HeaderNames

class ClaimCreditsForExportsControllerIntergrationSpec extends Specifications with TestConfiguration with  ITCoreTestData with TryValues {
  "ClaimCreditsForExportsController" should {

    "Ask if user is needing to claim a credit for liable drinks that have been exported" in {
      val userAnswers = broughtIntoUkFromSmallProducersFullAnswers.success.value
      setAnswers(userAnswers)
      given
        .commonPrecondition

      WsTestClient.withClient { client ⇒
        val result1 = client.url(s"$baseUrl/claim-credits-for-exports")
          .withFollowRedirects(false)
          .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
          .get()

        whenReady(result1) { res ⇒
          res.status mustBe 200
        }

      }
    }

    "Post the brought into UK " when {

      "user selected yes " in {

        val userAnswers = broughtIntoUkFromSmallProducersFullAnswers.success.value
        setAnswers(userAnswers)
        given
          .commonPrecondition

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
          }

        }
      }

      "user selected no " in {
        given
          .commonPrecondition

        val userAnswers = broughtIntoUkFromSmallProducersFullAnswers.success.value
        setAnswers(userAnswers)

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
          }

        }
      }

    }
  }
}