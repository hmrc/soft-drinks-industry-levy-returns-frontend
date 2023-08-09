package controllers

import models.SmallProducer
import org.jsoup.Jsoup
import org.scalatest.TryValues
import pages.SmallProducerDetailsPage
import play.api.libs.json.{JsObject, Json}
import play.api.libs.ws.DefaultWSCookie
import play.api.test.WsTestClient
import play.mvc.Http.HeaderNames

class SmallProducerDetailsControllerIntegrationSpec extends ControllerITTestHelper with TryValues {
  "SmallProducerDetailsController" should {
    "Ask for if user wants to add more small producer" in {
      val userAnswers = smallProducerDetaisPartialAnswers.success.value
      setUpData(userAnswers)
      given
        .commonPreconditionChangeSubscription(aSubscription)

      WsTestClient.withClient { client =>
        val result1 = client.url(s"$baseUrl/small-producer-details")
          .withFollowRedirects(false)
          .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
          .get()

        whenReady(result1) { res =>
          res.status mustBe 200
        }
      }
    }

    "Not pre populate the radio buttons" when {
      "user selected yes previously" in {
        val userAnswers = smallProducerDetaisPartialAnswers.success.value
          .set(SmallProducerDetailsPage, true).success.value
        setUpData(userAnswers)
        given
          .commonPreconditionChangeSubscription(aSubscription)

        WsTestClient.withClient { client =>
          val result1 = client.url(s"$baseUrl/small-producer-details")
            .withFollowRedirects(false)
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
            .get()

          whenReady(result1) { res =>
            res.status mustBe 200
            val doc = Jsoup.parse(res.body)
            assert(!doc.getElementById("value-no").hasAttr("checked"))
            assert(!doc.getElementById("value").hasAttr("checked"))
          }
        }
      }
    }


      "Post the exemption for small producer " when {
        "user selected yes " in {

          val expectedResult: Some[JsObject] = Some(
            Json.obj(
              "ownBrands" -> true,
              "brandsPackagedAtOwnSites" -> Json.obj("lowBand" -> 1000, "highBand" -> 1000),
              "packagedContractPacker" -> true,
              "howManyAsAContractPacker" -> Json.obj("lowBand" -> 1000, "highBand" -> 1000),
              "exemptionsForSmallProducers" -> true,
              "addASmallProducer" -> Json.obj("producerName" -> "Super Cola Ltd", "referenceNumber" -> "XZSDIL000000234", "lowBand" -> 1000, "highBand" -> 1000),
              "smallProducerDetails" -> true
            ))

          given
            .commonPreconditionChangeSubscription(aSubscription)

          val userAnswers = addASmallProducerFullAnswers.success.value
          setUpData(userAnswers)

          WsTestClient.withClient { client =>
            val result =
              client.url(s"$baseUrl/small-producer-details")
                .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
                .withHttpHeaders("X-Session-ID" -> "XKSDIL000000022",
                  "Csrf-Token" -> "nocheck")
                .withFollowRedirects(false)
                .post(Json.obj("value" -> "true"))

            whenReady(result) { res =>
              res.status mustBe 303
              res.header(HeaderNames.LOCATION) mustBe Some(s"/soft-drinks-industry-levy-returns-frontend/add-small-producer-next")
              getAnswers(sdilNumber).map(userAnswers => userAnswers.data) mustBe expectedResult
            }

          }
        }

        "user selected no with 0 small producers on the list" in {

          val expectedResult: Some[JsObject] = Some(
            Json.obj(
              "ownBrands" -> true,
              "brandsPackagedAtOwnSites" -> Json.obj("lowBand" -> 1000, "highBand" -> 1000),
              "packagedContractPacker" -> true,
              "howManyAsAContractPacker" -> Json.obj("lowBand" -> 1000, "highBand" -> 1000),
              "exemptionsForSmallProducers" -> true,
              "smallProducerDetails" -> false
            ))

          given
            .commonPreconditionChangeSubscription(aSubscription)

          val userAnswers = smallProducerDetaisNoProducerAnswers
          setUpData(userAnswers)

          WsTestClient.withClient { client =>
            val result =
              client.url(s"$baseUrl/small-producer-details")
                .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
                .withHttpHeaders("X-Session-ID" -> "XKSDIL000000022",
                  "Csrf-Token" -> "nocheck")
                .withFollowRedirects(false)
                .post(Json.obj("value" -> "false"))


            whenReady(result) { res =>
              res.status mustBe 303
              res.header(HeaderNames.LOCATION) mustBe Some(s"/soft-drinks-industry-levy-returns-frontend/exemptions-for-small-producers")
              getAnswers(sdilNumber).map(userAnswers => userAnswers.data) mustBe expectedResult
            }

          }

        }

        "user selected no with at least one small producer on the list" in {

          val expectedResult: Some[JsObject] = Some(
            Json.obj(
              "ownBrands" -> true,
              "brandsPackagedAtOwnSites" -> Json.obj("lowBand" -> 1000, "highBand" -> 1000),
              "packagedContractPacker" -> true,
              "howManyAsAContractPacker" -> Json.obj("lowBand" -> 1000, "highBand" -> 1000),
              "exemptionsForSmallProducers" -> true,
              "addASmallProducer" -> Json.obj("producerName" -> "Super Cola Ltd", "referenceNumber" -> "XZSDIL000000234", "lowBand" -> 1000, "highBand" -> 1000),
              "smallProducerDetails" -> false
            ))

          given
            .commonPreconditionChangeSubscription(aSubscription)

          val userAnswers = addASmallProducerFullAnswers.success.value.copy(smallProducerList = List(SmallProducer("", "", (1L, 1L))))
          setUpData(userAnswers)

          WsTestClient.withClient { client =>
            val result =
              client.url(s"$baseUrl/small-producer-details")
                .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
                .withHttpHeaders("X-Session-ID" -> "XKSDIL000000022",
                  "Csrf-Token" -> "nocheck")
                .withFollowRedirects(false)
                .post(Json.obj("value" -> "false"))


            whenReady(result) { res =>
              res.status mustBe 303
              res.header(HeaderNames.LOCATION) mustBe Some(s"/soft-drinks-industry-levy-returns-frontend/brought-into-uk")
              getAnswers(sdilNumber).map(userAnswers => userAnswers.data) mustBe expectedResult
            }

          }

        }
      }
      testUnauthorisedUser(baseUrl + "/small-producer-details")
    }

}
