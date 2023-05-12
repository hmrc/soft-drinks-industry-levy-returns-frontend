package controllers

import controllers.testSupport.{ITCoreTestData, Specifications, TestConfiguration}
import models.backend.{Site, UkAddress}
import org.scalatest.TryValues
import play.api.libs.json.{JsObject, Json}
import play.api.libs.ws.DefaultWSCookie
import play.api.test.WsTestClient
import play.mvc.Http.HeaderNames

import java.time.LocalDate

class PackagingSiteDetailsControllerIntegrationSpec extends Specifications with TestConfiguration with  ITCoreTestData with TryValues {
  "PackagingSiteDetailsController" should {

    "Post the request to update packaging site details " when {

      "user selected yes " in {

        val expectedResult:Some[JsObject] = Some(
          Json.obj(
            "ownBrands" -> false,
            "packagedContractPacker" -> true,
            "howManyAsAContractPacker" -> Json.obj("lowBand" -> 1000, "highBand" -> 1000),
            "exemptionsForSmallProducers" -> false,
            "broughtIntoUK" -> false,
            "broughtIntoUkFromSmallProducers" -> false,
            "claimCreditsForExports" -> false,
            "packagingSiteDetails" -> true
          ))

        given
          .commonPrecondition
        val userAnswers = newPackerPartialAnswers
        setAnswers(userAnswers)

        WsTestClient.withClient { client =>
          val result =
            client.url(s"$baseUrl/packaging-site-details")
              .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
              .withHttpHeaders("X-Session-ID" -> "XCSDIL000000069",
                "Csrf-Token" -> "nocheck")
              .withFollowRedirects(false)
              .post(Json.obj("value" -> "true"))


          whenReady(result) { res =>
            res.status mustBe 303
            res.header(HeaderNames.LOCATION) mustBe Some(s"/soft-drinks-industry-levy-returns-frontend")
            getAnswers(sdilNumber).map(userAnswers => userAnswers.data) mustBe expectedResult
          }
        }
      }

      "user selected remove on one of the addresses" in {
        given
        .commonPrecondition
        val userAnswers = newPackerPartialAnswers
        setAnswers(userAnswers)
        WsTestClient.withClient { client =>
          val result =
            client.url(s"$baseUrl/packaging-site-details")
              .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
              .withHttpHeaders("X-Session-ID" -> "XCSDIL000000069",
                "Csrf-Token" -> "nocheck")
              .withFollowRedirects(false)
              .post(Json.obj("value" -> "true"))
            whenReady(result) { res =>
              res.status mustBe 303
              res.header(HeaderNames.LOCATION) mustBe Some(s"/soft-drinks-industry-levy-returns-frontend")
            }
        }
      }
    }

    "Post the request to continue from packaging site details " when {

      "user selected no with at least one packaging site on the list" in {

        val expectedResult: Some[Map[String,Site]] =  Some(Map("4564561" -> Site(UkAddress(List("122 Dinsdale Crescent", "Romford"), "RM95 8FQ"), Some("27"),
          Some("Super Lemonade Group"), Some(LocalDate.of(2017, 4, 23)))))

        given
          .commonPrecondition
        val userAnswers = newPackerPartialAnswers.copy(packagingSiteList =
          Map("4564561" -> Site(UkAddress(List("122 Dinsdale Crescent", "Romford"), "RM95 8FQ"), Some("27"),
            Some("Super Lemonade Group"), Some(LocalDate.of(2017, 4, 23)))))
        setAnswers(userAnswers)

        WsTestClient.withClient { client =>
          val result =
            client.url(s"$baseUrl/packaging-site-details")
              .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
              .withHttpHeaders("X-Session-ID" -> "XCSDIL000000069",
                "Csrf-Token" -> "nocheck")
              .withFollowRedirects(false)
              .post(Json.obj("value" -> "false"))


          whenReady(result) { res =>
            res.status mustBe 303
            res.header(HeaderNames.LOCATION) mustBe Some(s"/soft-drinks-industry-levy-returns-frontend/check-your-answers")
            getAnswers(sdilNumber).map(userAnswers => userAnswers.packagingSiteList) mustBe expectedResult
          }
        }
      }

      "user selected no with at least one packaging site on the list AND user is also a new Importer" in {
        val expectedResult: Some[Map[String,Site]] =  Some(Map("6541651568" -> Site(UkAddress(List("122 Dinsdale Crescent", "Romford"), "RM95 8FQ"), Some("27"),
          Some("Super Lemonade Group"), Some(LocalDate.of(2017, 4, 23)))))

        given
          .commonPrecondition
        val userAnswers = newPackerPartialNewImporterAnswers.copy(
          packagingSiteList =
          Map("6541651568" -> Site(UkAddress(List("122 Dinsdale Crescent", "Romford"), "RM95 8FQ"), Some("27"),
            Some("Super Lemonade Group"), Some(LocalDate.of(2017, 4, 23)))))
        setAnswers(userAnswers)

        WsTestClient.withClient { client =>
          val result =
            client.url(s"$baseUrl/packaging-site-details")
              .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
              .withHttpHeaders("X-Session-ID" -> "XSSDIL000000232",
                "Csrf-Token" -> "nocheck")
              .withFollowRedirects(false)
              .post(Json.obj("value" -> "false"))

          whenReady(result) { res =>
            res.status mustBe 303
            res.header(HeaderNames.LOCATION) mustBe Some(s"/soft-drinks-industry-levy-returns-frontend/ask-secondary-warehouses-in-return")
            getAnswers(sdilNumber).map(userAnswers => userAnswers.packagingSiteList) mustBe expectedResult
          }
        }
      }
    }

  }
}
