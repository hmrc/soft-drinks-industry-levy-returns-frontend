package controllers

import models.UserAnswers
import org.scalatest.TryValues
import play.api.libs.json.{JsObject, Json}
import play.api.libs.ws.DefaultWSCookie
import play.api.test.WsTestClient
import play.mvc.Http.HeaderNames

class BroughtIntoUKControllerIntegrationSpec extends ControllerITTestHelper with TryValues{
  "BroughtIntoUKController" should {

    val broughtIntoUkUrl = "brought-into-uk"

    "Ask for are you reporting liable drinks brought into uk from outside uk" in {
      val userAnswers = broughtIntoUkPartialAnswers.success.value
      setUpData(userAnswers)
      given
        .commonPreconditionChangeSubscription(aSubscription)
      WsTestClient.withClient { client =>
        val result1 = client.url(s"$baseUrl/$broughtIntoUkUrl")
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
        given
          .commonPreconditionChangeSubscription(aSubscription)

        val expectedResult: Some[JsObject] = Some(Json.obj("broughtIntoUK" ->  true))
        setUpData(emptyUserAnswers)


        WsTestClient.withClient { client =>
          val result =
            client.url(s"$baseUrl/$broughtIntoUkUrl")
              .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
              .withHttpHeaders("X-Session-ID" -> "XKSDIL000000022",
                "Csrf-Token" -> "nocheck")
              .withFollowRedirects(false)
              .post(Json.obj("value" -> true))


          whenReady(result) { res =>
            res.status mustBe 303
            res.header(HeaderNames.LOCATION) mustBe Some(s"/soft-drinks-industry-levy-returns-frontend/how-many-brought-into-uk")
            getAnswers(sdilNumber).map(userAnswers => userAnswers.data) mustBe expectedResult
          }

        }
      }

      "user selected no " in {
        given
          .commonPreconditionChangeSubscription(aSubscription)

        val userAnswers = exemptionsForSmallProducersFullAnswers.success.value
        setUpData(userAnswers)

        WsTestClient.withClient { client =>
          val result =
            client.url(s"$baseUrl/$broughtIntoUkUrl")
              .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
              .withHttpHeaders("X-Session-ID" -> "XKSDIL000000022",
                "Csrf-Token" -> "nocheck")
              .withFollowRedirects(false)
              .post(Json.obj("value" -> false))


          whenReady(result) { res =>
            res.status mustBe 303
            res.header(HeaderNames.LOCATION) mustBe Some(s"/soft-drinks-industry-levy-returns-frontend/brought-into-uk-from-small-producers")
          }

        }
      }

    }
    testUnauthorisedUser(baseUrl + "/" + broughtIntoUkUrl)
  }

}
