package controllers

import models.{DefaultUserAnswersData, NormalMode}
import play.api.libs.json.Json
import play.api.libs.ws.DefaultWSCookie
import play.api.test.WsTestClient
import play.mvc.Http.HeaderNames
import play.api.libs.ws.JsonBodyWritables.writeableOf_JsValue

class ReturnsControllerIntegrationSpec extends ControllerITTestHelper {
  def url(nilReturn: Boolean) = s"/submit-return/year/2018/quarter/1/nil-return/$nilReturn"

  val urlsForNilReturnValues = Map(true -> s"${url(true)}",
    false -> s"${url(false)}")

  urlsForNilReturnValues.foreach { case (isNilReturn, path) =>
    s"GET $path" should {
      val expectedLocation = if (isNilReturn) {
        routes.CheckYourAnswersController.onPageLoad.url
      } else {
        routes.OwnBrandsController.onPageLoad(NormalMode).url
      }
      s"setup default/override user answers with no for all pages and redirect to $expectedLocation" when {
        "there is no return period in the cache, the return period is pending and there are no user answers in the database" in {
          build
            .commonPreconditionChangeSubscription(aSubscription)

          WsTestClient.withClient { client =>
            val result1 = client.url(s"$baseUrl$path")
              .withFollowRedirects(false)
              .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
              .get()

            whenReady(result1) { res =>
              res.status mustBe 303
              res.header(HeaderNames.LOCATION).value mustBe expectedLocation
              val userAnswers = getAnswers(sdilNumber)
              val expectedData = if (isNilReturn) {
                Json.toJson(new DefaultUserAnswersData(aSubscription))
              } else {
                Json.obj()
              }
              userAnswers.get.data mustBe expectedData
            }
          }
        }

        "there is a return period in the cache that doesn't match the request return period which is pending and there are user answers in the database" in {
          setUpData(broughtIntoUkFromSmallProducersFullAnswers.success.value.copy(returnPeriod = diffReturnPeriod))
          build
            .commonPreconditionChangeSubscription(aSubscription)

          WsTestClient.withClient { client =>
            val result1 = client.url(s"$baseUrl$path")
              .withFollowRedirects(false)
              .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
              .get()

            whenReady(result1) { res =>
              res.status mustBe 303
              res.header(HeaderNames.LOCATION).value mustBe expectedLocation
              val userAnswers = getAnswers(sdilNumber)
              val expectedData = if (isNilReturn) {
                Json.toJson(new DefaultUserAnswersData(aSubscription))
              } else {
                Json.obj()
              }
              userAnswers.get.data mustBe expectedData
            }
          }
        }

        "there is a return period in the cache that matches the request return period which is pending and there are user answers in the database that has a nilReturn missmatch" in {
          val userAnswers = broughtIntoUkFromSmallProducersFullAnswers.success.value.copy(isNilReturn = !isNilReturn)

          setUpData(userAnswers.copy(returnPeriod = diffReturnPeriod))
          build
            .commonPreconditionChangeSubscription(aSubscription)

          WsTestClient.withClient { client =>
            val result1 = client.url(s"$baseUrl$path")
              .withFollowRedirects(false)
              .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
              .get()

            whenReady(result1) { res =>
              res.status mustBe 303
              res.header(HeaderNames.LOCATION).value mustBe expectedLocation
              val updatedUA = getAnswers(sdilNumber)
              val expectedData = if (isNilReturn) {
                Json.toJson(new DefaultUserAnswersData(aSubscription))
              } else {
                Json.obj()
              }
              updatedUA.get.data mustBe expectedData
            }
          }
        }
      }

      s"not update user answers and redirect to $expectedLocation" when {
        "there is a return period in the cache matching the return period request and the user answers in the database are not submitted and have same nilReturn" in {
          val userAnswers = broughtIntoUkFromSmallProducersFullAnswers.success.value.copy(isNilReturn = isNilReturn, submitted = false)

          setUpData(userAnswers)
          build
            .commonPreconditionChangeSubscription(aSubscription)

          WsTestClient.withClient { client =>
            val result1 = client.url(s"$baseUrl$path")
              .withFollowRedirects(false)
              .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
              .get()

            whenReady(result1) { res =>
              res.status mustBe 303
              res.header(HeaderNames.LOCATION).value mustBe expectedLocation
              val updatedUA = getAnswers(sdilNumber)
              updatedUA.get.data mustBe userAnswers.data
            }
          }
        }
      }

      "should redirect to sdilHome" when {
        "there is no return period in cache and the return period is not pending" in {
          build
            .authorisedWithNoPendingReturns(aSubscription)

          WsTestClient.withClient { client =>
            val result1 = client.url(s"$baseUrl$path")
              .withFollowRedirects(false)
              .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
              .get()

            whenReady(result1) { res =>
              res.status mustBe 303
              res.header(HeaderNames.LOCATION).value mustBe "http://localhost:8707/soft-drinks-industry-levy-account-frontend/home"
              getAnswers(sdilNumber).isEmpty mustBe true
            }
          }
        }

        "there is a returnPeriod in cache matching the request period and userAnswers that have been submitted" in {
          val userAnswers = broughtIntoUkFromSmallProducersFullAnswers.success.value.copy(submitted = true)

          setUpData(userAnswers)
          build
            .commonPreconditionChangeSubscription(aSubscription)

          WsTestClient.withClient { client =>
            val result1 = client.url(s"$baseUrl$path")
              .withFollowRedirects(false)
              .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
              .get()

            whenReady(result1) { res =>
              res.status mustBe 303
              res.header(HeaderNames.LOCATION).value mustBe "http://localhost:8707/soft-drinks-industry-levy-account-frontend/home"
            }
          }
        }
      }
      testUnauthorisedUser(s"$baseUrl$path")
    }
  }
}