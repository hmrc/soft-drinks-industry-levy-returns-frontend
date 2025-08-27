package controllers

import models.SmallProducer
import org.scalatest.TryValues
import play.api.libs.json.{JsObject, Json}
import play.api.libs.ws.DefaultWSCookie
import play.api.test.WsTestClient
import play.mvc.Http.HeaderNames
import play.api.libs.ws.JsonBodyWritables.writeableOf_JsValue

class ExemptionsForSmallProducersControllerIntegrationSpec extends ControllerITTestHelper with TryValues {
  "ExemptionsForSmallProducersController" should {

    "Ask for if user need to claim an exemption for any of the liable drinks they have packaged for registered small producers" in {
      val userAnswers = exemptionsForSmallProducersPartialAnswers.success.value
      setUpData(userAnswers)
      build
        .commonPreconditionChangeSubscription(aSubscription)

      WsTestClient.withClient { client =>
        val result1 = client.url(s"$baseUrl/exemptions-for-small-producers")
          .withFollowRedirects(false)
          .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
          .get()

        whenReady(result1) { res =>
          res.status mustBe 200
        }

      }
    }

    "Post the exemption for small producer " when {

      "user selected yes " in {

        val expectedResult:Some[JsObject] = Some(
          Json.obj(
            "ownBrands" -> true,
            "brandsPackagedAtOwnSites" -> Json.obj("lowBand" -> 1000,"highBand" -> 1000),
            "packagedContractPacker" -> true,
            "howManyAsAContractPacker" -> Json.obj("lowBand" -> 1000, "highBand" -> 1000),
            "exemptionsForSmallProducers" -> true
          ))


        build
          .commonPreconditionChangeSubscription(aSubscription)

        val userAnswers = exemptionsForSmallProducersFullAnswers.success.value
        setUpData(userAnswers)

        WsTestClient.withClient { client =>
          val result =
            client.url(s"$baseUrl/exemptions-for-small-producers")
              .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
              .withHttpHeaders("X-Session-ID" -> "XKSDIL000000022",
                "Csrf-Token" -> "nocheck")
              .withFollowRedirects(false)
              .post(Json.obj("value" -> true))


          whenReady(result) { res =>
            res.status mustBe 303
            res.header(HeaderNames.LOCATION) mustBe Some(s"/soft-drinks-industry-levy-returns-frontend/add-small-producer")
            getAnswers(sdilNumber).map(userAnswers => userAnswers.data) mustBe expectedResult
          }

        }
      }

      "user selected no " in {

        val expectedResult:Some[JsObject] = Some(
          Json.obj(
            "ownBrands" -> true,
            "brandsPackagedAtOwnSites" -> Json.obj("lowBand" -> 1000,"highBand" -> 1000),
            "packagedContractPacker" -> true,
            "howManyAsAContractPacker" -> Json.obj("lowBand" -> 1000, "highBand" -> 1000),
            "exemptionsForSmallProducers" -> false
          ))

        build
          .commonPreconditionChangeSubscription(aSubscription)

        val userAnswers = exemptionsForSmallProducersFullAnswers.success.value
        setUpData(userAnswers)

        WsTestClient.withClient { client =>
          val result =
            client.url(s"$baseUrl/exemptions-for-small-producers")
              .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
              .withHttpHeaders("X-Session-ID" -> "XKSDIL000000022",
                "Csrf-Token" -> "nocheck")
              .withFollowRedirects(false)
              .post(Json.obj("value" -> false))


          whenReady(result) { res =>
            res.status mustBe 303
            res.header(HeaderNames.LOCATION) mustBe Some(s"/soft-drinks-industry-levy-returns-frontend/brought-into-uk")
            getAnswers(sdilNumber).map(userAnswers => userAnswers.data) mustBe expectedResult
          }

        }
      }

    }
    "Post the new form data with an empty SmallProducerList when a previously answered yes is changed to no " in {

      build
        .commonPreconditionChangeSubscription(aSubscription)

      val userAnswers = addASmallProducerFullAnswers.success.value.copy(smallProducerList = List(SmallProducer("","",(1L, 1L))))
      setUpData(userAnswers)

      getAnswers(sdilNumber).map(userAnswers => userAnswers.smallProducerList).get.size mustBe 1

      WsTestClient.withClient { client =>
        val result =
          client.url(s"$baseUrl/change-exemptions-for-small-producers")
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
            .withHttpHeaders("X-Session-ID" -> "XKSDIL000000022",
              "Csrf-Token" -> "nocheck")
            .withFollowRedirects(false)
            .post(Json.obj("value" -> false))

        whenReady(result) { res =>
          res.status mustBe 303
          res.header(HeaderNames.LOCATION) mustBe Some(s"/soft-drinks-industry-levy-returns-frontend/check-your-answers")
          getAnswers(sdilNumber).map(userAnswers => userAnswers.smallProducerList).get.size mustBe 0
        }

      }

    }
    testUnauthorisedUser(baseUrl + "/exemptions-for-small-producers")
  }

}
