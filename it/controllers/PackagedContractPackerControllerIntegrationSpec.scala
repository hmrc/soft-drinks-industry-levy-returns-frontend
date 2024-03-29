package controllers

import org.scalatest.TryValues
import play.api.libs.json.{JsObject, Json}
import play.api.libs.ws.DefaultWSCookie
import play.api.test.WsTestClient
import play.mvc.Http.HeaderNames

class PackagedContractPackerControllerIntegrationSpec extends ControllerITTestHelper with TryValues {
  "PackagedContractPackerController" should {


    "Ask for if user is  reporting liable drink as a third party or contract packer at UK sites user operate" when {

      "user entered No on own brand page" in {
        val userAnswers = ownBrandPageFalseAnswers.success.value
        setUpData(userAnswers)
        given
          .commonPreconditionChangeSubscription(aSubscription)

        WsTestClient.withClient { client =>
          val result1 = client.url(s"$baseUrl/packaged-as-contract-packer")
            .withFollowRedirects(false)
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
            .get()

          whenReady(result1) { res =>
            res.status mustBe 200
          }

        }
      }

      "user entered Yes on own brand page " in {
        val userAnswers = brandPackagedOwnSiteAnswers.success.value
        setUpData(userAnswers)
        given
          .commonPreconditionChangeSubscription(aSubscription)

        WsTestClient.withClient { client =>
          val result1 = client.url(s"$baseUrl/packaged-as-contract-packer")
            .withFollowRedirects(false)
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
            .get()

          whenReady(result1) { res =>
            res.status mustBe 200
          }
        }
      }

    }

    "Post the packaged as contract packer" when {

      "user selected yes" in {

        val expectedResult:Some[JsObject] = Some(
          Json.obj(
            "ownBrands" -> true,
            "brandsPackagedAtOwnSites" -> Json.obj("lowBand" -> 1000,"highBand" -> 1000),
            "packagedContractPacker" -> true
          ))

        val userAnswers = brandPackagedOwnSiteAnswers.success.value
        setUpData(userAnswers)
        given
          .commonPreconditionChangeSubscription(aSubscription)

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
            getAnswers(sdilNumber).map(userAnswers => userAnswers.data) mustBe expectedResult
          }
        }
      }

      "user selected No" in {
        val expectedResult:Some[JsObject] = Some(
          Json.obj(
            "ownBrands" -> true,
            "brandsPackagedAtOwnSites" -> Json.obj("lowBand" -> 1000,"highBand" -> 1000),
            "packagedContractPacker" -> false
          ))

        val userAnswers = brandPackagedOwnSiteAnswers.success.value
        setUpData(userAnswers)
        given
          .commonPreconditionChangeSubscription(aSubscription)

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
            getAnswers(sdilNumber).map(userAnswers => userAnswers.data) mustBe expectedResult
          }
        }
      }



    }

    testUnauthorisedUser(baseUrl + "/packaged-as-contract-packer")
  }

}
