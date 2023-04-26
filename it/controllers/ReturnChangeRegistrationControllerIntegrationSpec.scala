package controllers

import controllers.testSupport.{ITCoreTestData, Specifications, TestConfiguration}
import org.jsoup.Jsoup
import pages.PackagedContractPackerPage
import play.api.http.Status.{OK, SEE_OTHER}
import play.api.libs.json.Json
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
          doc.getElementsByTag("title").text() mustBe "You changed your soft drinks business activity - soft-drinks-industry-levy-returns-frontend - GOV.UK"
        }
      }
    }
  }
  "POST" should {
    "take user to next destination successfully if user answers empty" in {
      setAnswers(emptyUserAnswers)
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
          res.header(HeaderNames.LOCATION) mustBe Some(s"/soft-drinks-industry-levy-returns-frontend/exemptions-for-small-producers")
        }
      }
    }
    s"take user to next destination successfully if $PackagedContractPackerPage is true" in {
      setAnswers(emptyUserAnswers.set(PackagedContractPackerPage, true).get)
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
          res.header(HeaderNames.LOCATION) mustBe Some(s"/soft-drinks-industry-levy-returns-frontend/how-many-packaged-as-contract-packer")
        }
      }
    }
    s"take user to next destination successfully if $PackagedContractPackerPage is false" in {
      setAnswers(emptyUserAnswers.set(PackagedContractPackerPage, false).get)
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
          res.header(HeaderNames.LOCATION) mustBe Some(s"/soft-drinks-industry-levy-returns-frontend/exemptions-for-small-producers")
        }
      }
    }
  }
}
