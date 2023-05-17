package controllers

import controllers.testSupport.{ITCoreTestData, Specifications, TestConfiguration}
import org.scalatest.TryValues
import play.api.libs.ws.DefaultWSCookie
import play.api.test.WsTestClient

class ReturnSentControllerIntegrationSpec extends Specifications with TestConfiguration with  ITCoreTestData with TryValues {

  "ReturnSentController" should {

    "Show the user there submitted information after successfully submitting their return" in {
      val userAnswers = checkYourAnswersFullAnswers
      setAnswers(userAnswers.copy(submitted = true))
      given
        .commonPrecondition

      WsTestClient.withClient { client =>
        val result1 = client.url(s"$baseUrl/return-sent")
          .withFollowRedirects(false)
          .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
          .get()

        whenReady(result1) { res =>
          res.status mustBe 200
        }

      }
    }

    "Redirect to beginning of journey when no returns sent" in {
      val userAnswers = smallProducerDetaisPartialAnswers.success.value
      setAnswers(userAnswers.copy(submitted = false))
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
