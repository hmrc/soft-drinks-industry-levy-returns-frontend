package controllers

import controllers.testSupport.{ITCoreTestData, Specifications, TestConfiguration}
import models.Amounts
import org.mockito.MockitoSugar.mock
import org.scalatest.TryValues
import play.api.libs.json.Json
import play.api.libs.ws.DefaultWSCookie
import play.api.test.WsTestClient
import repositories.{CacheMap, SDILSessionCache}


class ReturnSentControllerIntegrationSpec extends Specifications with TestConfiguration with  ITCoreTestData with TryValues {

  "ReturnSentController" should {

    "Show the user their submitted information after successfully submitting their return" in {
      setAnswers(checkYourAnswersFullAnswers.copy(submitted = true))
      given.commonPrecondition
      val mockSessionCache = mock[SDILSessionCache]

      WsTestClient.withClient { client =>
        sdilSessionCacheRepo.upsert(CacheMap("XKSDIL000000022",Map("AMOUNTS"-> Json.toJson(Amounts(1000, 100, 1100)))))
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
