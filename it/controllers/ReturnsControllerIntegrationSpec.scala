package controllers

import models.{DefaultUserAnswersData, NormalMode}
import play.api.libs.json.{JsObject, Json}
import play.api.libs.ws.DefaultWSCookie
import play.api.test.WsTestClient
import play.mvc.Http.HeaderNames

class ReturnsControllerIntegrationSpec extends ControllerITTestHelper {
  def url(nilReturn: Boolean) = s"/submit-return/year/2018/quarter/1/nil-return/$nilReturn"

  s"GET ${url(true)}" when {
    val nilReturnUrl = s"$baseUrl${url(true)}"
    "the return period is pending" should {
      "setup default/override user answers with no for all pages and redirect to cya" when {
        "there is no useranswers in the database" in {
          given
            .commonPreconditionChangeSubscription(aSubscription)

          WsTestClient.withClient { client =>
            val result1 = client.url(nilReturnUrl)
              .withFollowRedirects(false)
              .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
              .get()

            whenReady(result1) { res =>
              res.status mustBe 303
              res.header(HeaderNames.LOCATION).value mustBe routes.CheckYourAnswersController.onPageLoad.url
              val userAnswers = getAnswers(sdilNumber)
              userAnswers.get.data mustBe Json.toJson(new DefaultUserAnswersData(aSubscription))
            }
          }
        }

        "there is useranswers already in the database" in {

          setUpData(broughtIntoUkFromSmallProducersFullAnswers.success.value)
          given
            .commonPreconditionChangeSubscription(aSubscription)

          WsTestClient.withClient { client =>
            val result1 = client.url(nilReturnUrl)
              .withFollowRedirects(false)
              .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
              .get()

            whenReady(result1) { res =>
              res.status mustBe 303
              res.header(HeaderNames.LOCATION).value mustBe routes.CheckYourAnswersController.onPageLoad.url
              val userAnswers = getAnswers(sdilNumber).get
              userAnswers.data mustNot equal(broughtIntoUkFromSmallProducersFullAnswers.success.value.data)
              userAnswers.data mustBe Json.toJson(new DefaultUserAnswersData(aSubscription))
            }
          }
        }
      }
    }
    "the return period is not pending" should {
      "redirect to sdil frontend" in {
        given
          .authorisedWithNoPendingReturns(aSubscription)

        WsTestClient.withClient { client =>
          val result1 = client.url(nilReturnUrl)
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
    }
    testUnauthorisedUser(nilReturnUrl)
  }

  s"GET ${url(false)}" when {
    val returnUrl = s"$baseUrl${url(false)}"
    "the return period is pending" should {
      "setup/override user answers with empty data and redirect to ownbrands" when {
        "there is no useranswers in the database and the user is not a small producer" in {
          given
            .commonPreconditionChangeSubscription(aSubscription)

          WsTestClient.withClient { client =>
            val result1 = client.url(returnUrl)
              .withFollowRedirects(false)
              .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
              .get()

            whenReady(result1) { res =>
              res.status mustBe 303
              res.header(HeaderNames.LOCATION).value mustBe routes.OwnBrandsController.onPageLoad(NormalMode).url
              val userAnswers = getAnswers(sdilNumber)
              userAnswers.get.data mustBe Json.obj()
            }
          }
        }

        "there is user answers already in the database" in {

          setUpData(broughtIntoUkFromSmallProducersFullAnswers.success.value)
          given
            .commonPreconditionChangeSubscription(aSubscription)

          WsTestClient.withClient { client =>
            val result1 = client.url(returnUrl)
              .withFollowRedirects(false)
              .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
              .get()

            whenReady(result1) { res =>
              res.status mustBe 303
              res.header(HeaderNames.LOCATION).value mustBe routes.OwnBrandsController.onPageLoad(NormalMode).url
              val userAnswers = getAnswers(sdilNumber).get
              userAnswers.data mustNot equal(broughtIntoUkFromSmallProducersFullAnswers.success.value.data)
              userAnswers.data mustBe Json.obj()
            }
          }
        }
      }

      "setup/override user answers with empty data and redirect to packaged contract packer" when {
        "there is no useranswers in the database and the user is a small producer" in {
          given
            .commonPreconditionChangeSubscription(aSubscription.copy(activity = aSubscription.activity.copy(smallProducer = true)))

          WsTestClient.withClient { client =>
            val result1 = client.url(returnUrl)
              .withFollowRedirects(false)
              .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
              .get()

            whenReady(result1) { res =>
              res.status mustBe 303
              res.header(HeaderNames.LOCATION).value mustBe routes.PackagedContractPackerController.onPageLoad(NormalMode).url
              val userAnswers = getAnswers(sdilNumber)
              userAnswers.get.data mustBe Json.obj()
            }
          }
        }

        "there is user answers already in the database" in {

          setUpData(broughtIntoUkFromSmallProducersFullAnswers.success.value)
          given
            .commonPreconditionChangeSubscription(aSubscription)

          WsTestClient.withClient { client =>
            val result1 = client.url(returnUrl)
              .withFollowRedirects(false)
              .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
              .get()

            whenReady(result1) { res =>
              res.status mustBe 303
              res.header(HeaderNames.LOCATION).value mustBe routes.OwnBrandsController.onPageLoad(NormalMode).url
              val userAnswers = getAnswers(sdilNumber).get
              userAnswers.data mustNot equal(broughtIntoUkFromSmallProducersFullAnswers.success.value.data)
              userAnswers.data mustBe Json.obj()
            }
          }
        }
      }
    }
    "the return period is not pending" should {
      "redirect to sdil frontend" in {
        given
          .authorisedWithNoPendingReturns(aSubscription)

        WsTestClient.withClient { client =>
          val result1 = client.url(returnUrl)
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
    }
    testUnauthorisedUser(returnUrl)
  }
}