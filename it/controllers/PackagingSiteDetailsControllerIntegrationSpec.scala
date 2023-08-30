package controllers

import controllers.testSupport.helpers.ALFTestHelper
import models.UserAnswers
import models.alf.init._
import models.backend.{Site, UkAddress}
import org.scalatest.TryValues
import play.api.libs.json.{JsObject, Json}
import play.api.libs.ws.DefaultWSCookie
import play.api.test.WsTestClient
import play.mvc.Http.HeaderNames

import java.time.LocalDate

class PackagingSiteDetailsControllerIntegrationSpec extends ControllerITTestHelper with TryValues {
  "PackagingSiteDetailsController" should {

    "Post the request to update packaging site details " when {

      "user selected yes, user should be taken to ALF" in {
        val journeyConfigToBePosted: JourneyConfig = JourneyConfig(
          version = 2,
          options = JourneyOptions(
            continueUrl = s"http://localhost:8703/soft-drinks-industry-levy-returns-frontend/off-ramp/packing-sites/${sdilNumber}",
            homeNavHref = None,
            signOutHref = Some(controllers.auth.routes.AuthController.signOut().url),
            accessibilityFooterUrl = None,
            phaseFeedbackLink = Some(s"http://localhost:9250/contact/beta-feedback?service=soft-drinks-industry-levy-returns-frontend&backUrl=http%3A%2F%2Flocalhost%3A8703%2Fsoft-drinks-industry-levy-returns-frontend%2Fpackaging-site-details"),
            deskProServiceName = None,
            showPhaseBanner = Some(false),
            alphaPhase = Some(false),
            includeHMRCBranding = Some(true),
            ukMode = Some(true),
            selectPageConfig = Some(SelectPageConfig(
              proposalListLimit = Some(10),
              showSearchAgainLink = Some(true)
            )),
            showBackButtons = Some(true),
            disableTranslations = Some(true),
            allowedCountryCodes = None,
            confirmPageConfig = Some(ConfirmPageConfig(
              showSearchAgainLink = Some(true),
              showSubHeadingAndInfo = Some(true),
              showChangeLink = Some(true),
              showConfirmChangeText = Some(true)
            )),
            timeoutConfig = Some(TimeoutConfig(
              timeoutAmount = 900,
              timeoutUrl = controllers.auth.routes.AuthController.signOut().url,
              timeoutKeepAliveUrl = Some(routes.KeepAliveController.keepAlive.url)
            )),
            serviceHref = Some(routes.IndexController.onPageLoad().url),
            pageHeadingStyle = Some("govuk-heading-l")
          ),
          labels = Some(
            JourneyLabels(
              en = Some(LanguageLabels(
                appLevelLabels = Some(AppLevelLabels(
                  navTitle = Some("Soft Drinks Industry Levy"),
                  phaseBannerHtml = None
                )),
                selectPageLabels = None,
                lookupPageLabels = Some(
                  LookupPageLabels(
                    title = Some("Find UK packaging site address"),
                    heading = Some("Find UK packaging site address"),
                    postcodeLabel = Some("Postcode"))),
                editPageLabels = Some(
                  EditPageLabels(
                    title = Some("Enter the UK packaging site address"),
                    heading = Some("Enter the UK packaging site address"),
                    line1Label = Some("Address line 1"),
                    line2Label = Some("Address line 2"),
                    line3Label = Some("Address line 3 (optional)"),
                    townLabel = Some("Address line 4 (optional)"),
                    postcodeLabel= Some("Postcode"),
                    organisationLabel = Some("Packaging site name (optional)"))
                ),
                confirmPageLabels = None,
                countryPickerLabels = None
              ))
            )),
          requestedVersion = None
        )
        val expectedResultInDB: Some[JsObject] = Some(
          Json.obj(
            "HowManyBroughtIntoUk" -> Json.obj("lowBand" -> 10, "highBand" -> 10),"packagingSiteDetails" -> true
          )
        )
        val alfOnRampURL: String = "http://onramp.com"

        setUpData(UserAnswers(sdilNumber, Json.obj("HowManyBroughtIntoUk" -> Json.obj("lowBand" -> 10, "highBand" -> 10)), List.empty))
        given
          .commonPreconditionChangeSubscription(aSubscription)
          .alf.getSuccessResponseFromALFInit(alfOnRampURL)

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
            res.header(HeaderNames.LOCATION) mustBe Some(alfOnRampURL)
            getAnswers(sdilNumber).map(userAnswers => userAnswers.data) mustBe expectedResultInDB

            ALFTestHelper.requestedBodyMatchesExpected(wireMockServer, journeyConfigToBePosted) mustBe true
          }
        }
      }

      "user selected remove on one of the addresses" in {
        given
        .commonPreconditionChangeSubscription(aSubscription)
        val userAnswers = newPackerPartialAnswers
        setUpData(userAnswers)
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
            }
        }
      }
    }

    "Post the request to continue from packaging site details " when {

      "user selected no with at least one packaging site on the list" in {

        val expectedResult: Some[Map[String,Site]] =  Some(Map("4564561" -> Site(UkAddress(List("122 Dinsdale Crescent", "Romford"), "RM95 8FQ"), Some("27"),
          Some("Super Lemonade Group"), Some(LocalDate.of(2017, 4, 23)))))

        given
          .commonPreconditionChangeSubscription(aSubscription)
        val userAnswers = newPackerPartialAnswers.copy(packagingSiteList =
          Map("4564561" -> Site(UkAddress(List("122 Dinsdale Crescent", "Romford"), "RM95 8FQ"), Some("27"),
            Some("Super Lemonade Group"), Some(LocalDate.of(2017, 4, 23)))))
        setUpData(userAnswers)

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
          .commonPreconditionChangeSubscription(aSubscription)
        val userAnswers = newPackerPartialNewImporterAnswers.copy(
          packagingSiteList =
          Map("6541651568" -> Site(UkAddress(List("122 Dinsdale Crescent", "Romford"), "RM95 8FQ"), Some("27"),
            Some("Super Lemonade Group"), Some(LocalDate.of(2017, 4, 23)))))
        setUpData(userAnswers)

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
    testUnauthorisedUser(baseUrl + "/packaging-site-details")
  }
}
