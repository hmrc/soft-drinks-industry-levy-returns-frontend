package controllers.test

import controllers.testSupport.{ITCoreTestData, Specifications, TestConfiguration}
import models.UserAnswers
import play.api.http.Status.NO_CONTENT
import play.api.libs.json.Json
import play.api.test.WsTestClient

import java.time.Instant

class TestingControllerIntegrationSpec extends
  Specifications with TestConfiguration with ITCoreTestData {

  private val userAnswers = UserAnswers("id", requestReturnPeriod, Json.obj("foo" -> "bar"), List(), lastUpdated = Instant.ofEpochSecond(1))
  private val userAnswers2 = UserAnswers("id2", requestReturnPeriod, Json.obj("foo" -> "bar"), List(), lastUpdated = Instant.ofEpochSecond(1))

  ".resetUserAnswers" should {

    "must remove a record" in {
      setUpData(userAnswers)
      setUpData(userAnswers2)

      getAnswers(userAnswers.id).get.id mustBe userAnswers.id
      getAnswers(userAnswers2.id).get.id mustBe userAnswers2.id

      WsTestClient.withClient { client =>
        val result = client.url(s"$baseUrl/test-only/user-answers/${userAnswers.id}")
          .withFollowRedirects(false)
          .get()

        whenReady(result) { res =>
          res.status mustBe NO_CONTENT
          getAnswers(userAnswers.id) mustBe None
          getAnswers(userAnswers2.id).get.id mustBe userAnswers2.id
        }
      }
    }
  }

}
