package controllers

import controllers.testSupport.{ITCoreTestData, Specifications, TestConfiguration}
import models.NormalMode
import models.retrieved.RetrievedActivity
import org.jsoup.Jsoup
import play.api.http.Status.{OK, SEE_OTHER}
import play.api.libs.ws.DefaultWSCookie
import play.api.test.WsTestClient
import play.mvc.Http.HeaderNames

class ReturnChangeRegistrationControllerIntegrationSpec extends Specifications with TestConfiguration with ITCoreTestData {

  "GET" should {
    "return view" in {
      setUpData(newPackerPartialAnswers)
      given
        .commonPreconditionChangeSubscription(aSubscription)

      WsTestClient.withClient { client =>
        val result = client.url(s"$baseUrl/return-change-registration")
          .withFollowRedirects(false)
          .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
          .get()

        whenReady(result) { res =>
          res.status mustBe OK
          val doc = Jsoup.parse(res.body)
          doc.getElementsByTag("h1").text() mustBe "You changed your soft drinks business activity"
          doc.title() mustBe "You changed your soft drinks business activity - Soft Drinks Industry Levy - GOV.UK"
          doc.getElementById("main-content").getElementsByTag("a").attr("href") mustBe "/soft-drinks-industry-levy-returns-frontend/packaged-as-contract-packer"
        }
      }
    }
  }
  "POST" should {

    "take user to pack at business address only if new packer and there are no production sites in the retrieved subscription" in {
      setUpData(newPackerPartialAnswers)
      given
        .commonPreconditionChangeSubscription(aSubscription.copy(productionSites = List.empty))

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
          res.header(HeaderNames.LOCATION) mustBe Some(routes.PackAtBusinessAddressController.onPageLoad(NormalMode).url)
        }
      }
    }

    "take user to ask secondary warehouse if not new packer and there are no production sites in the retrieved subscription" in {
      setUpData(newPackerPartialAnswers)

      val activity = RetrievedActivity(smallProducer = false, largeProducer = true, contractPacker = true,
        importer = false, voluntaryRegistration = false)

      given
        .commonPreconditionChangeSubscription(aSubscription.copy(activity = activity,productionSites = List.empty))

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
          res.header(HeaderNames.LOCATION) mustBe Some(routes.AskSecondaryWarehouseInReturnController.onPageLoad(NormalMode).url)
        }
      }
    }

    "take user to add warehouse if new packer and at least one production site exists in the retrieved subscription" in {
      setUpData(newPackerPartialAnswers)
      given
        .commonPreconditionChangeSubscription(aSubscription)

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
          res.header(HeaderNames.LOCATION) mustBe Some(routes.AskSecondaryWarehouseInReturnController.onPageLoad(NormalMode).url)
        }
      }
    }

    s"take user to next destination successfully if new packer is false and new importer is true" in {
      setUpData(newImporterAnswers)
      given
        .commonPreconditionChangeSubscription(aSubscription)

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
          res.header(HeaderNames.LOCATION) mustBe Some(routes.AskSecondaryWarehouseInReturnController.onPageLoad(NormalMode).url)
        }
      }
    }
  }
}
