package controllers

import controllers.testSupport.{ITCoreTestData, Specifications, TestConfiguration}
import org.scalatest.TryValues
import play.api.libs.ws.DefaultWSCookie
import play.api.test.WsTestClient

class ReturnSentControllerIntergrationSpec extends Specifications with TestConfiguration with  ITCoreTestData with TryValues {
  "ReturnSentController" should {
    "Redirect to beginning of journey when no returns sent" in {
      val userAnswers = smallProducerDetaisPartialAnswers.success.value
      setAnswers(userAnswers)
      given
        .commonPrecondition

      WsTestClient.withClient { client =>
        val result1 = client.url(s"$baseUrl/return-sent")
          .withFollowRedirects(false)
          .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
          .get()

        whenReady(result1) { res =>
          res.status mustBe 303
        }

      }
    }
  }
}
