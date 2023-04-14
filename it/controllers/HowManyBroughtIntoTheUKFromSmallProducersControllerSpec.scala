package controllers

import controllers.testSupport.{ITCoreTestData, Specifications, TestConfiguration}
import org.scalatest.TryValues
import play.api.libs.ws.DefaultWSCookie
import play.api.test.WsTestClient

class HowManyBroughtIntoTheUKFromSmallProducersControllerSpec extends Specifications with TestConfiguration with ITCoreTestData with TryValues {
  "HowManyBroughtIntoTheUKFromSmallProducersController" should {
    "Ask for many litres of liable drinks have user packaged at UK sites they operate" in {
      setAnswers(broughtIntoUkFullAnswers.success.value)
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
  }

}