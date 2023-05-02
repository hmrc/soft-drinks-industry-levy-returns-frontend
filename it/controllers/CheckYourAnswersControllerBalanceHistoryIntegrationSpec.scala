package controllers

import controllers.testSupport.{ITCoreTestData, Specifications, TestConfiguration}
import org.scalatest.TryValues
import play.api.libs.ws.DefaultWSCookie
import play.api.test.WsTestClient

class CheckYourAnswersControllerBalanceHistoryIntegrationSpec extends Specifications with TestConfiguration with  ITCoreTestData with TryValues {

  override def configParams: Map[String, Any] = Map(
    "balanceAll.enabled" -> true
  )

  "CheckYourAnswersController" should {

    "Load when valid user answers present" in {

      setAnswers(checkYourAnswersFullAnswers)

      given.commonPrecondition
      given.sdilBackend.balanceHistory("XKSDIL000000022", false)

      WsTestClient.withClient { client =>
        val result1 = client.url(s"$baseUrl/check-your-answers")
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