package controllers

import controllers.testSupport.{ITCoreTestData, Specifications, TestConfiguration}
import play.api.http.HeaderNames
import play.api.libs.json.JsValue
import play.api.libs.ws.{DefaultWSCookie, WSClient, WSResponse}
import play.api.test.WsTestClient
import play.api.libs.ws.JsonBodyWritables.writeableOf_JsValue

import scala.concurrent.Future

trait ControllerITTestHelper extends Specifications with TestConfiguration with ITCoreTestData {

  def createClientRequestGet(client: WSClient, url: String): Future[WSResponse] = {
    client.url(url)
      .withFollowRedirects(false)
      .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
      .get()
  }

  def createClientRequestPOST(client: WSClient, url: String, json: JsValue): Future[WSResponse] = {
    client.url(url)
      .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
      .withHttpHeaders("X-Session-ID" -> "XKSDIL000000022",
        "Csrf-Token" -> "nocheck")
      .withFollowRedirects(false)
      .post(json)
  }

  def testUnauthorisedUser(url: String, optJson: Option[JsValue] = None, requiresSubscription: Boolean = true): Unit = {
    "the user is unauthenticated" should {
      "redirect to gg-signin" in {
        build.unauthorisedPrecondition

        WsTestClient.withClient { client =>
          val result1 = optJson match {
            case Some(json) => createClientRequestPOST(client, url, json)
            case _ => createClientRequestGet(client, url)
          }

          whenReady(result1) { res =>
            res.status mustBe 303
            res.header(HeaderNames.LOCATION).get must include("/bas-gateway/sign-in")
          }
        }
      }
    }

      "redirect to the account home page" should {
        "the user is authorised with UTR but has no sdilSubscription" in {
          build.authorisedWithNoSubscriptionPrecondition
          WsTestClient.withClient { client =>
            val result1 = optJson match {
              case Some(json) => createClientRequestPOST(client, url, json)
              case _ => createClientRequestGet(client, url)
            }

            whenReady(result1) { res =>
              res.status mustBe 303
              res.header(HeaderNames.LOCATION).get must include("/soft-drinks-industry-levy-account-frontend")
            }
          }
        }

        "the user is authorised with SdilRef but has no sdilSubscription" in {
          build.authorisedWithNoSubscriptionPreconditionWithSDilRef
          WsTestClient.withClient { client =>
            val result1 = optJson match {
              case Some(json) => createClientRequestPOST(client, url, json)
              case _ => createClientRequestGet(client, url)
            }

            whenReady(result1) { res =>
              res.status mustBe 303
              res.header(HeaderNames.LOCATION).get must include("/soft-drinks-industry-levy-account-frontend")
            }
          }
        }

        "the user is authorised but has no enrolment" in {
          build.authorisedButNoEnrolmentsPrecondition
          WsTestClient.withClient { client =>
            val result1 = optJson match {
              case Some(json) => createClientRequestPOST(client, url, json)
              case _ => createClientRequestGet(client, url)
            }

            whenReady(result1) { res =>
              res.status mustBe 303
              res.header(HeaderNames.LOCATION).get must include("/soft-drinks-industry-levy-account-frontend")
            }
          }
        }
    }

    "the user is authorised but has no identifer" should {
      "render the error page" in {
        build.authorisedButInternalIdPrecondition

        WsTestClient.withClient { client =>
          val result1 = optJson match {
            case Some(json) => createClientRequestPOST(client, url, json)
            case _ => createClientRequestGet(client, url)
          }

          whenReady(result1) { res =>
            res.status mustBe 303
            res.header(HeaderNames.LOCATION).get must include("/soft-drinks-industry-levy-account-frontend")
          }
        }
      }
    }
  }

}
