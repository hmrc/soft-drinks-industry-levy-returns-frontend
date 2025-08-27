package controllers

import controllers.testSupport.helpers.ALFTestHelper
import models.alf.init._
import org.scalatest.TryValues
import play.api.libs.json._
import play.api.libs.ws.DefaultWSCookie
import play.api.test.WsTestClient
import play.mvc.Http.HeaderNames
import play.api.libs.ws.JsonBodyWritables.writeableOf_JsValue


class SecondaryWarehouseDetailsControllerIntegrationSpec extends ControllerITTestHelper with TryValues {
  "SecondaryWarehouseDetailsController" should {
    "Ask for if user wants to add more warehouses" in {

      setUpData(emptyUserAnswers)
      build
        .commonPreconditionChangeSubscription(aSubscription)

      WsTestClient.withClient { client =>
        val result1 = client.url(s"$baseUrl/secondary-warehouse-details")
          .withFollowRedirects(false)
          .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
          .get()

        whenReady(result1) { res =>
          res.status mustBe 200
        }

      }
    }
    "user selected yes, user should be taken to ALF" in {
      val journeyConfigToBePosted: JourneyConfig = JourneyConfig(
        version = 2,
        options = JourneyOptions(
          continueUrl = s"http://localhost:8703/soft-drinks-industry-levy-returns-frontend/off-ramp/secondary-warehouses/${sdilNumber}",
          homeNavHref = None,
          signOutHref = Some(controllers.auth.routes.AuthController.signOut().url),
          accessibilityFooterUrl = Some("localhost/accessibility-statement/soft-drinks-industry-levy-returns-frontend"),
          phaseFeedbackLink = Some(s"http://localhost:9250/contact/beta-feedback?service=soft-drinks-industry-levy-returns-frontend&backUrl=http%3A%2F%2Flocalhost%3A8703%2Fsoft-drinks-industry-levy-returns-frontend%2Fsecondary-warehouse-details"),
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
          serviceHref = Some(frontendAppConfig.sdilHomeUrl),
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
                  title = Some("Find UK warehouse address"),
                  heading = Some("Find UK warehouse address"),
                  postcodeLabel = Some("Postcode"))),
              editPageLabels = Some(
                EditPageLabels(
                  title = Some("Enter the UK warehouse address"),
                  heading = Some("Enter the UK warehouse address"),
                  line1Label = Some("Address line 1"),
                  line2Label = Some("Address line 2"),
                  line3Label = Some("Address line 3 (optional)"),
                  townLabel = Some("Address line 4 (optional)"),
                  postcodeLabel = Some("Postcode"),
                  organisationLabel = Some("Trading name (optional)"))
              ),
              confirmPageLabels = None,
              countryPickerLabels = None
            ))
          )),
        requestedVersion = None
      )
      val expectedResultInDB: Some[JsObject] = Some(
        Json.obj(
          "secondaryWarehouseDetails" -> true
        ))
      val alfOnRampURL: String = "http://onramp.com"

      setUpData(emptyUserAnswers)
      build
        .commonPreconditionChangeSubscription(aSubscription)
        .alf.getSuccessResponseFromALFInit(alfOnRampURL)

      WsTestClient.withClient { client =>
        val result =
          client.url(s"$baseUrl/secondary-warehouse-details")
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
            .withHttpHeaders("X-Session-ID" -> "XKSDIL000000022",
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

    "user selected no" in {
      val expectedResultInDB: Some[JsObject] = Some(
        Json.obj(
          "secondaryWarehouseDetails" -> false
        ))

      setUpData(emptyUserAnswers)
      build
        .commonPreconditionChangeSubscription(aSubscription)

      WsTestClient.withClient { client =>
        val result =
          client.url(s"$baseUrl/secondary-warehouse-details")
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
            .withHttpHeaders("X-Session-ID" -> "XKSDIL000000022",
              "Csrf-Token" -> "nocheck")
            .withFollowRedirects(false)
            .post(Json.obj("value" -> "false"))

        whenReady(result) { res =>
          res.status mustBe 303
          res.header(HeaderNames.LOCATION) mustBe Some(s"/soft-drinks-industry-levy-returns-frontend/check-your-answers")
          getAnswers(sdilNumber).map(userAnswers => userAnswers.data) mustBe expectedResultInDB
        }
      }
    }
    testUnauthorisedUser(baseUrl + "/secondary-warehouse-details")
  }

}