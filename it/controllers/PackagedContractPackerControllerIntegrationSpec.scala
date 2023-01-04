package controllers

import controllers.testSupport.{ITCoreTestData, Specifications, TestConfiguration}
import org.scalatest.TryValues
import play.api.libs.json.Json
import play.api.libs.ws.DefaultWSCookie
import play.api.test.WsTestClient
import play.mvc.Http.HeaderNames

class PackagedContractPackerControllerIntegrationSpec extends Specifications with TestConfiguration with  ITCoreTestData with TryValues {
  "PackagedContractPackerController" should {


    "Ask for if user is  reporting liable drink as a third party or contract packer at UK sites user operate" when {

      "user entered No on own brand page" in {
        val userAnswers = ownBrandPageFalseAnswers.success.value
        setAnswers(userAnswers)
        given
          .commonPrecondition

        WsTestClient.withClient { client ⇒
          val result1 = client.url(s"$baseUrl/packaged-as-contract-packer")
            .withFollowRedirects(false)
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
            .get()

          whenReady(result1) { res ⇒
            res.status mustBe 200
          }

        }
      }

      "user entered Yes on own brand page " in {
        val userAnswers = brandPackagedOwnSiteAnswers.success.value
        setAnswers(userAnswers)
        given
          .commonPrecondition

        WsTestClient.withClient { client ⇒
          val result1 = client.url(s"$baseUrl/packaged-as-contract-packer")
            .withFollowRedirects(false)
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
            .get()

          whenReady(result1) { res ⇒
            res.status mustBe 200
          }

        }
      }

    }

    "Post the packaged as contract packer" when {

      "user selected yes" in {
        val userAnswers = brandPackagedOwnSiteAnswers.success.value
        setAnswers(userAnswers)
        given
          .commonPrecondition

        WsTestClient.withClient { client =>
          val result =
            client.url(s"$baseUrl/packaged-as-contract-packer")
              .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
              .withHttpHeaders("X-Session-ID" -> "XKSDIL000000022",
                "Csrf-Token" -> "nocheck")
              .withFollowRedirects(false)
              .post(Json.obj("value" -> true))


          whenReady(result) { res =>
            res.status mustBe 303
            res.header(HeaderNames.LOCATION) mustBe Some(s"/soft-drinks-industry-levy-returns-frontend/how-many-packaged-as-contract-packer")
          }
        }
      }

      "user selected No`" in {
        val userAnswers = brandPackagedOwnSiteAnswers.success.value
        setAnswers(userAnswers)
        given
          .commonPrecondition

        WsTestClient.withClient { client =>
          val result =
            client.url(s"$baseUrl/packaged-as-contract-packer")
              .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
              .withHttpHeaders("X-Session-ID" -> "XKSDIL000000022",
                "Csrf-Token" -> "nocheck")
              .withFollowRedirects(false)
              .post(Json.obj("value" -> false))


          whenReady(result) { res =>
            res.status mustBe 303
            res.header(HeaderNames.LOCATION) mustBe Some(s"/soft-drinks-industry-levy-returns-frontend/exemptions-for-small-producers")
          }
        }
      }



    }
  }

}
