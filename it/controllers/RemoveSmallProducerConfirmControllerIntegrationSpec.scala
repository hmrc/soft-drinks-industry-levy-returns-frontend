package controllers

import controllers.testSupport.{ITCoreTestData, Specifications, TestConfiguration}
import org.scalatest.TryValues
import play.api.libs.json.Json
import play.api.libs.ws.DefaultWSCookie
import play.api.test.WsTestClient
import play.mvc.Http.HeaderNames

class RemoveSmallProducerConfirmControllerIntegrationSpec extends Specifications with TestConfiguration with  ITCoreTestData with TryValues {
  "RemoveSmallProducerConfirmController" should {

    "Ask for if user wants to remove this small producer" in {
      val userAnswers = removeSmallProducerConfirmPartialAnswers.success.value
      setAnswers(userAnswers)
      given
        .commonPrecondition

      WsTestClient.withClient { client ⇒
        val result1 = client.url(s"$baseUrl/remove-small-producer-confirm")
          .withFollowRedirects(false)
          .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
          .get()

        whenReady(result1) { res ⇒
          res.status mustBe 200
        }

      }
    }

    "Post the removal for selected small producer " when {

      "user selected yes " in {
        given
          .commonPrecondition

        val userAnswers = removeSmallProducerConfirmPartialAnswers.success.value
        setAnswers(userAnswers)

        WsTestClient.withClient { client =>
          val result =
            client.url(s"$baseUrl/remove-small-producer-confirm")
              .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
              .withHttpHeaders("X-Session-ID" -> "XKSDIL000000022",
                "Csrf-Token" -> "nocheck")
              .withFollowRedirects(false)
              .post(Json.obj("value" -> true))


          whenReady(result) { res =>
            res.status mustBe 303
            res.header(HeaderNames.LOCATION) mustBe Some(s"/soft-drinks-industry-levy-returns-frontend/add-small-producer-next")
          }

        }
      }

      "user selected no " in {
        given
          .commonPrecondition

        val userAnswers = removeSmallProducerConfirmPartialAnswers.success.value
        setAnswers(userAnswers)

        WsTestClient.withClient { client =>
          val result =
            client.url(s"$baseUrl/remove-small-producer-confirm")
              .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
              .withHttpHeaders("X-Session-ID" -> "XKSDIL000000022",
                "Csrf-Token" -> "nocheck")
              .withFollowRedirects(false)
              .post(Json.obj("value" -> false))


          whenReady(result) { res =>
            res.status mustBe 303
            res.header(HeaderNames.LOCATION) mustBe Some(s"/soft-drinks-industry-levy-returns-frontend/small-producer-details")
          }

        }
      }
    }


  }

}

