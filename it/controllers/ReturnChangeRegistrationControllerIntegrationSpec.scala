package controllers

import controllers.testSupport.{ITCoreTestData, Specifications, TestConfiguration}
import org.jsoup.Jsoup
import play.api.http.Status.{OK, SEE_OTHER}
import play.api.libs.ws.DefaultWSCookie
import play.api.test.WsTestClient
import play.mvc.Http.HeaderNames

class ReturnChangeRegistrationControllerIntegrationSpec extends Specifications with TestConfiguration with ITCoreTestData {

  "GET" should {
    "return view" in {
      setAnswers(emptyUserAnswers)
      given
        .commonPrecondition

      WsTestClient.withClient { client =>
        val result = client.url(s"$baseUrl/return-change-registration")
          .withFollowRedirects(false)
          .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
          .get()

        whenReady(result) { res =>
          res.status mustBe OK
          val doc = Jsoup.parse(res.body)
          doc.getElementsByTag("h1").text() mustBe "You changed your soft drinks business activity"
          doc.getElementsByTag("title").text() mustBe "You changed your soft drinks business activity - Soft Drinks Industry Levy - GOV.UK"
        }
      }
    }
  }
  "POST" should {
    "take user to next destination successfully if user " in {
      setAnswers(newPackerPartialAnswers)
      given
        .commonPrecondition

      WsTestClient.withClient { client =>
        val result = client.url(s"$baseUrl/return-change-registration")
          .withFollowRedirects(false)
          .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
          .withFollowRedirects(false)
          .withHttpHeaders("X-Session-ID" -> "XKSDIL000000022",
            "Csrf-Token" -> "nocheck")
          .post("")

        whenReady(result) { res =>
          res.status mustBe SEE_OTHER
          res.header(HeaderNames.LOCATION) mustBe Some(s"/soft-drinks-industry-levy-returns-frontend/pack-at-business-address-in-return")
        }
      }
    }
    s"take user to next destination successfully if new packer is true" in {
      setAnswers(newPackerPartialAnswers)
      given
        .commonPrecondition

      WsTestClient.withClient { client =>
        val result = client.url(s"$baseUrl/return-change-registration")
          .withFollowRedirects(false)
          .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
          .withFollowRedirects(false)
          .withHttpHeaders("X-Session-ID" -> "XKSDIL000000022",
            "Csrf-Token" -> "nocheck")
          .post("")

        whenReady(result) { res =>
          res.status mustBe SEE_OTHER
          res.header(HeaderNames.LOCATION) mustBe Some(s"/soft-drinks-industry-levy-returns-frontend/pack-at-business-address-in-return")
        }
      }
    }
    s"take user to next destination successfully if new packer is false and new importer is true" in {
      setAnswers(newImporterAnswers)
      given
        .commonPrecondition

      WsTestClient.withClient { client =>
        val result = client.url(s"$baseUrl/return-change-registration")
          .withFollowRedirects(false)
          .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
          .withFollowRedirects(false)
          .withHttpHeaders("X-Session-ID" -> "XGSDIL000001611",
            "Csrf-Token" -> "nocheck")
          .post("")

        whenReady(result) { res =>
          res.status mustBe SEE_OTHER
          res.header(HeaderNames.LOCATION) mustBe Some(s"/soft-drinks-industry-levy-returns-frontend/ask-secondary-warehouses-in-return")
        }
      }
    }
  }
}
