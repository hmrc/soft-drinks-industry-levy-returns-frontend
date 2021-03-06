package controllers

import controllers.testSupport.{ITCoreTestData, Specifications, TestConfiguration}
import org.scalatest.TryValues
import play.api.libs.json.Json
import play.api.libs.ws.DefaultWSCookie
import play.api.test.WsTestClient
import play.mvc.Http.HeaderNames

class BroughtIntoUkFromSmallProducersControllerIntegrationSpec extends Specifications with TestConfiguration with  ITCoreTestData with TryValues {
  "BroughtIntoUkFromSmallProducersController" should {

    val broughtIntoUkFromSmallProducersUrl = "brought-into-uk-from-small-producers"
    "Ask for Are you reporting liable drinks you have brought into the UK from small producers" in {
      val userAnswers = broughtIntoUkFullAnswers.success.value
      setAnswers(userAnswers)
      given
        .commonPrecondition
      WsTestClient.withClient { client ⇒
        val result1 = client.url(s"$baseUrl/$broughtIntoUkFromSmallProducersUrl")
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

        val userAnswers = broughtIntoUkFullAnswers.success.value
        setAnswers(userAnswers)
        given
          .commonPrecondition

        WsTestClient.withClient { client =>
          val result =
            client.url(s"$baseUrl/$broughtIntoUkFromSmallProducersUrl")
              .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
              .withHttpHeaders("X-Session-ID" -> "XKSDIL000000022",
                "Csrf-Token" -> "nocheck")
              .withFollowRedirects(false)
              .post(Json.obj("value" -> true))


          whenReady(result) { res =>
            res.status mustBe 303
            res.header(HeaderNames.LOCATION) mustBe Some(s"/soft-drinks-industry-levy-returns-frontend/how-many-into-uk-small-producers")
          }

        }
      }

      "user selected no " in {
        given
          .commonPrecondition

        val userAnswers = exemptionsForSmallProducersFullAnswers.success.value
        setAnswers(userAnswers)

        WsTestClient.withClient { client =>
          val result =
            client.url(s"$baseUrl/$broughtIntoUkFromSmallProducersUrl")
              .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
              .withHttpHeaders("X-Session-ID" -> "XKSDIL000000022",
                "Csrf-Token" -> "nocheck")
              .withFollowRedirects(false)
              .post(Json.obj("value" -> false))


          whenReady(result) { res =>
            res.status mustBe 303
            res.header(HeaderNames.LOCATION) mustBe Some(s"/soft-drinks-industry-levy-returns-frontend/claim-credits-for-exports")
          }

        }
      }

    }
  }

}
