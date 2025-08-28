package controllers

import org.scalatest.TryValues
import play.api.libs.json.{JsObject, Json}
import play.api.libs.ws.DefaultWSCookie
import play.api.test.WsTestClient
import play.mvc.Http.HeaderNames
import play.api.libs.ws.JsonBodyWritables.writeableOf_JsValue

class BroughtIntoUkFromSmallProducersControllerIntegrationSpec extends ControllerITTestHelper with TryValues {

  "BroughtIntoUkFromSmallProducersController" should {

    val broughtIntoUkFromSmallProducersUrl = "brought-into-uk-from-small-producers"
    "Ask for Are you reporting liable drinks you have brought into the UK from small producers" in {
      val userAnswers = broughtIntoUkFullAnswers.success.value
      setUpData(userAnswers)
      build
        .commonPreconditionChangeSubscription(aSubscription)
      WsTestClient.withClient { client =>
        val result1 = client.url(s"$baseUrl/$broughtIntoUkFromSmallProducersUrl")
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
        val expectedResult: Some[JsObject] = Some(Json.obj("broughtIntoUkFromSmallProducers" -> true))

        build
          .commonPreconditionChangeSubscription(aSubscription)
        setUpData(emptyUserAnswers)

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
            getAnswers(sdilNumber).map(userAnswers => userAnswers.data) mustEqual expectedResult
          }

        }
      }

      "user selected no " in {
        val expectedResult: Some[JsObject] = Some(Json.obj("broughtIntoUkFromSmallProducers" -> false))

        build
          .commonPreconditionChangeSubscription(aSubscription)
        setUpData(emptyUserAnswers)

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
            getAnswers(sdilNumber).map(userAnswers => userAnswers.data) mustEqual expectedResult
          }
        }
      }
    }
    testUnauthorisedUser(baseUrl + "/" + broughtIntoUkFromSmallProducersUrl)
  }

}
