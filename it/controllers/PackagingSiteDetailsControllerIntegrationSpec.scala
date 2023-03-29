package controllers

import controllers.testSupport.{ITCoreTestData, Specifications, TestConfiguration}
import models.backend.{Site, UkAddress}
import org.scalatest.TryValues
import play.api.libs.json.Json
import play.api.libs.ws.DefaultWSCookie
import play.api.test.WsTestClient
import play.mvc.Http.HeaderNames

import java.time.LocalDate

class PackagingSiteDetailsControllerIntegrationSpec extends Specifications with TestConfiguration with  ITCoreTestData with TryValues {
  "PackagingSiteDetailsController" should {
    "Ask if user wants to add more packaging sites" in {
      val userAnswers = newPackerOrImporterPartialAnswers.success.value
      setAnswers(userAnswers)
      given
        .commonPrecondition

      WsTestClient.withClient { client =>
        val result1 = client.url(s"$baseUrl/packaging-site-details")
          .withFollowRedirects(false)
          .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
          .get()

        whenReady(result1) { res =>
          res.status mustBe 200
        }

      }
    }

    "Post the request to update packaging site details " when {

      "user selected yes " in {

        given
          .commonPrecondition
        val userAnswers = newPackerOrImporterPartialAnswers.success.value
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
            //            res.header(HeaderNames.LOCATION) mustBe Some(s"/soft-drinks-industry-levy-returns-frontend/add-packaging-site") // TODO when AddressLookup page added and url is determined, update url here
          }
        }
      }

      /* TODO update this once edit functionality and address lookup completed
      "user selected edit on one of the detail lines" in {
        given
          .commonPrecondition
        val userAnswers = newPackerOrImporterPartialAnswers.success.value
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
            //            res.header(HeaderNames.LOCATION) mustBe Some(s"/soft-drinks-industry-levy-returns-frontend/add-packaging-site") // TODO when AddressLookup page added and url is determined, update url here
          }
        }
      }
      */

      /* TODO update this once remove functionality completed
"user selected remove on one of the addresses" in {
given
.commonPrecondition
val userAnswers = newPackerOrImporterPartialAnswers.success.value
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
//            res.header(HeaderNames.LOCATION) mustBe Some(s"/soft-drinks-industry-levy-returns-frontend/add-packaging-site") // TODO when AddressLookup page added and url is determined, update url here
}
}
}
 */

    }
    "Post the request to continue from packaging site details " when {

      "user selected no with at least one packaging site on the list" in {

        given
          .commonPrecondition
        val userAnswers = newPackerOrImporterPartialAnswers.success.value.copy(packagingSiteList =
          List(Site(UkAddress(List("122 Dinsdale Crescent", "Romford"), "RM95 8FQ"), Some("27"),
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
            //        res.header(HeaderNames.LOCATION) mustBe Some(s"/soft-drinks-industry-levy-returns-frontend/check-your-answers") TODO: uncomment this line when CYA work has been completed.
          }
        }
      }

      "user selected no with at least one packaging site on the list AND user is also a new Importer" in {

        given
          .commonPrecondition
        val userAnswers = newPackerOrImporterPartialAnswers.success.value.copy(packagingSiteList =
          List(Site(UkAddress(List("122 Dinsdale Crescent", "Romford"), "RM95 8FQ"), Some("27"),
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
          }
        }
      }
    }

  }
}
